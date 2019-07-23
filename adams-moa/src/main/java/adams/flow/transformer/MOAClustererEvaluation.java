/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * MOAClustererEvaluation.java
 * Copyright (C) 2011-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.flow.core.CallableActorHelper;
import adams.flow.core.CallableActorReference;
import adams.flow.core.Token;
import moa.clusterers.AbstractClusterer;
import moa.core.Measurement;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Evaluates a MOA clusterer using prequential evaluation. With each incoming instance, the clusterer is first evaluated, then trained.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instances<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;moa.core.Measurement[]<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: MOAClustererEvaluation
 * </pre>
 * 
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded 
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-clusterer &lt;adams.flow.core.CallableActorReference&gt; (property: clusterer)
 * &nbsp;&nbsp;&nbsp;The name of the callable MOA clusterer to train&#47;evaluate.
 * &nbsp;&nbsp;&nbsp;default: MOAClusterer
 * </pre>
 * 
 * <pre>-evaluator &lt;moa.options.ClassOption&gt; (property: evaluator)
 * &nbsp;&nbsp;&nbsp;The MOA evaluator to use for evaluating a trained MOA clusterer.
 * &nbsp;&nbsp;&nbsp;default: moa.evaluation.BasicClusteringPerformanceEvaluator
 * </pre>
 * 
 * <pre>-output-interval &lt;int&gt; (property: outputInterval)
 * &nbsp;&nbsp;&nbsp;The number of tokens to skip before evaluating the clusterer stored in the 
 * &nbsp;&nbsp;&nbsp;token (only used when receiving Instance objects).
 * &nbsp;&nbsp;&nbsp;default: 1
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MOAClustererEvaluation
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = 1410487605033307517L;

  /** the key for storing the current clusterer in the backup. */
  public final static String BACKUP_CLUSTERER = "clusterer";

  /** the name of the callable clusterer to use. */
  protected CallableActorReference m_Clusterer;

  /** the model to use for prediction/training. */
  protected AbstractClusterer m_ActualClusterer;

  /** the output interval. */
  protected int m_OutputInterval;

  /** the current count of tokens that have passed through this actor. */
  protected int m_Count;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Evaluates a MOA clusterer using prequential evaluation. With each "
      + "incoming instance, the clusterer is first evaluated, then trained.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "clusterer", "clusterer",
	    new CallableActorReference("MOAClusterer"));

    m_OptionManager.add(
	    "output-interval", "outputInterval",
	    1, 1, null);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Count           = 0;
    m_ActualClusterer = null;
  }

  /**
   * Sets the callable clusterer to use.
   *
   * @param value	the clusterer name
   */
  public void setClusterer(CallableActorReference value) {
    m_Clusterer = value;
    reset();
  }

  /**
   * Returns the callable clusterer to use.
   *
   * @return		the clusterer name
   */
  public CallableActorReference getClusterer() {
    return m_Clusterer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String clustererTipText() {
    return "The name of the callable MOA clusterer to train/evaluate.";
  }

  /**
   * Sets the number of tokens after which to evaluate the clusterer.
   *
   * @param value	the interval
   */
  public void setOutputInterval(int value) {
    m_OutputInterval = value;
    reset();
  }

  /**
   * Returns the number of tokens after which to evaluate the clusterer.
   *
   * @return		the interval
   */
  public int getOutputInterval() {
    return m_OutputInterval;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outputIntervalTipText() {
    return "The number of tokens to skip before evaluating the clusterer stored in the token (only used when receiving Instance objects).";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result  = QuickInfoHelper.toString(this, "clusterer", m_Clusterer);
    result += QuickInfoHelper.toString(this, "outputInterval", ((m_OutputInterval == 1) ? "always" : m_OutputInterval), "/");

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->weka.core.Instance.class, weka.core.Instances.class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{Instance.class, Instances.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->moa.core.Measurement[].class<!-- flow-generates-end -->
   */
  public Class[] generates() {
    return new Class[]{Measurement[].class};
  }

  /**
   * Removes entries from the backup.
   */
  @Override
  protected void pruneBackup() {
    super.pruneBackup();

    pruneBackup(BACKUP_CLUSTERER);
  }

  /**
   * Backs up the current state of the actor before update the variables.
   *
   * @return		the backup
   */
  @Override
  protected Hashtable<String,Object> backupState() {
    Hashtable<String,Object>	result;

    result = super.backupState();

    if (m_ActualClusterer != null)
      result.put(BACKUP_CLUSTERER, m_ActualClusterer);

    return result;
  }

  /**
   * Restores the state of the actor before the variables got updated.
   *
   * @param state	the backup of the state to restore from
   */
  @Override
  protected void restoreState(Hashtable<String,Object> state) {
    if (state.containsKey(BACKUP_CLUSTERER)) {
      m_ActualClusterer = (AbstractClusterer) state.get(BACKUP_CLUSTERER);
      state.remove(BACKUP_CLUSTERER);
    }

    super.restoreState(state);
  }

  /**
   * Returns an instance of the callable clusterer.
   *
   * @return		the clusterer
   */
  protected AbstractClusterer getClustererInstance() {
    AbstractClusterer	result;
    MessageCollection	errors;

    errors = new MessageCollection();
    result = (AbstractClusterer) CallableActorHelper.getSetup(AbstractClusterer.class, m_Clusterer, this, errors);
    if (result == null) {
      if (!errors.isEmpty())
	getLogger().severe(errors.toString());
    }

    return result;
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String		result;
    Instance		testInst;
    double[] 		prediction;
    List<Instance>	data;
    boolean		single;

    result = null;

    if (m_ActualClusterer == null) {
      m_ActualClusterer = getClustererInstance();
      if (m_ActualClusterer == null) {
	result = "Failed to located clusterer '" + m_Clusterer + "'!";
	return result;
      }
    }

    data = new ArrayList<>();
    if (m_InputToken.getPayload() instanceof Instances) {
      Instances instances = (Instances) m_InputToken.getPayload();
      for (int i = 0; i < instances.numInstances(); i++)
        data.add(instances.get(i));
      single = false;
    }
    else {
      data.add((Instance) m_InputToken.getPayload());
      single = true;
    }

    for (Instance inst: data) {
      // train
      m_ActualClusterer.trainOnInstance(inst);
    }

    if (single) {
      m_Count++;
      if (m_Count % m_OutputInterval == 0) {
	m_Count = 0;
	m_OutputToken = new Token(m_ActualClusterer.getModelMeasurements());
      }
    }
    else {
      m_OutputToken = new Token(m_ActualClusterer.getModelMeasurements());
    }

    return result;
  }

  /**
   * Cleans up after the execution has finished.
   */
  @Override
  public void wrapUp() {
    super.wrapUp();

    m_ActualClusterer = null;
  }
}
