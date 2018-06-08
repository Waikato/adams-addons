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
 * ExecuteRats.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.control;

import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.flow.core.Actor;
import adams.flow.core.ControlActor;
import adams.flow.core.RatHelper;
import adams.flow.core.RatMode;
import adams.flow.core.RatReference;
import adams.flow.core.Unknown;
import adams.flow.standalone.Rat;
import adams.flow.transformer.AbstractTransformer;

/**
 <!-- globalinfo-start -->
 * Executes the specified Rat actors sequentially when a token passes through.<br>
 * The Rat actors need to be in MANUAL mode.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.core.Unknown<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.core.Unknown<br>
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
 * &nbsp;&nbsp;&nbsp;default: ExecuteRats
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseAnnotation&gt; (property: annotations)
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
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-rat &lt;adams.flow.core.RatReference&gt; [-rat ...] (property: rats)
 * &nbsp;&nbsp;&nbsp;The Rat actors to execute.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ExecuteRats
  extends AbstractTransformer
  implements ControlActor {

  private static final long serialVersionUID = 7078570350728159543L;

  /** the rats to execute. */
  protected RatReference[] m_Rats;

  /** the helper for the rat actors. */
  protected RatHelper m_RatHelper;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Executes the specified Rat actors sequentially when a token passes through.\n"
      + "The Rat actors need to be in " + RatMode.MANUAL + " mode.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "rat", "rats",
      new RatReference[0]);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_RatHelper = new RatHelper();
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "rats", m_Rats, "rats: ");
  }

  /**
   * Sets the rats to change.
   *
   * @param value	the rats
   */
  public void setRats(RatReference[] value) {
    m_Rats = value;
    reset();
  }

  /**
   * Returns the rats to change.
   *
   * @return		the condition
   */
  public RatReference[] getRats() {
    return m_Rats;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String ratsTipText() {
    return "The Rat actors to execute.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Unknown.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{Unknown.class};
  }

  /**
   * Initializes the item for flow execution.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  public String setUp() {
    String	result;
    int		i;
    Actor	rat;

    result = super.setUp();

    if (result == null) {
      if (!getOptionManager().hasVariableForProperty("rats")) {
	if (m_Rats.length == 0) {
	  result = "No Rat actor references defined!";
	}
	else {
	  for (i = 0; i < m_Rats.length; i++) {
	    rat = m_RatHelper.findRatRecursive(this, m_Rats[i]);
	    if (rat == null) {
	      result = "Failed to locate Rat #" + (i + 1) + ": " + m_Rats[i];
	      break;
	    }
	    else if (!(rat instanceof Rat)) {
	      result = "Rat #" + (i + 1) + " '" + m_Rats[i] + "' is not of type " + Rat.class.getName();
	      break;
	    }
	    else if (((Rat) rat).getMode() != RatMode.MANUAL) {
	      result = "Rat #" + (i + 1) + " '" + m_Rats[i] + "' is not is not in " + RatMode.MANUAL + ", but " + ((Rat) rat).getMode();
	      break;
	    }
	  }
	}
      }
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
    String	result;
    int		i;
    Actor 	actor;
    Rat		rat;

    result = null;

    for (i = 0; i < m_Rats.length; i++) {
      actor = m_RatHelper.findRatRecursive(this, m_Rats[i]);
      if (actor == null) {
	result = "Failed to locate Rat #" + (i+1) + ": " + m_Rats[i];
      }
      else {
        if (isLoggingEnabled())
          getLogger().info("Executing Rat #" + (i+1) + "...");
        rat = (Rat) actor;

        // wait to become available
	while (rat.isRunnableActive() && !isStopped())
	  Utils.wait(this, 1000, 100);

	// execute
	if (!isStopped()) {
	  result = rat.startRunnable();
	  if (result != null) {
	    result = "Executing Rat #" + (i+1) + ": " + result;
	    getLogger().severe(result);
	  }
	}

	// wait for rat to finish
	if (result == null) {
	  while (rat.isRunnableActive() && !isStopped())
	    Utils.wait(this, 1000, 100);
	}
      }
      if (result != null)
	break;
    }

    if (result == null)
      m_OutputToken = m_InputToken;

    return result;
  }
}
