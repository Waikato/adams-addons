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
 * RSink.java
 * Copyright (C) 2012-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.sink;

import org.rosuda.REngine.Rserve.RConnection;

import adams.core.Placeholders;
import adams.core.QuickInfoHelper;
import adams.core.RDataHelper;
import adams.core.Utils;
import adams.core.scripting.RScript;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.statistics.StatUtils;
import adams.flow.core.ActorUtils;
import adams.flow.standalone.Rserve;

/**
 <!-- globalinfo-start -->
 * Carries out an r command on the token passed in. The input can be accessed via 'X'.<br>
 * Variables are supported as well, e.g.: pow(X,&#64;{exp}) with '&#64;{exp}' being a variable available at execution time.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Integer<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Double<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Double[]<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Double[][]<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spreadsheet.SpreadSheet<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to 
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 * 
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: RSink
 * </pre>
 * 
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-skip (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded 
 * &nbsp;&nbsp;&nbsp;as it is.
 * </pre>
 * 
 * <pre>-stop-flow-on-error (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * </pre>
 * 
 * <pre>-script &lt;adams.core.scripting.RScript&gt; (property: script)
 * &nbsp;&nbsp;&nbsp;Script to pass into r. The input value can be accessed via 'X'.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author rsmith
 * @version $Revision$
 */
public class RSink
  extends AbstractSink {

  /** for serialization */
  private static final long serialVersionUID = 6150602242914328836L;

  /** the placeholder for the input value. */
  public final static String INPUT = "X";

  /** Script to pass to r */
  protected RScript m_Script;
  
  /** Connection to Rserve */
  protected RConnection m_RConn;
  
  /** the Rserve actor. */
  protected Rserve m_Rserve;

  /**
   * Adds to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"script", "script", 
	new RScript());
  }

  /**
   * Ressets the members.
   */
  @Override
  protected void reset() {
    super.reset();
    
    m_RConn = null;
  }

  /**
   * Sets the script.
   * 
   * @param val
   *          the script
   */
  public void setScript(RScript val) {
    m_Script = val;
    reset();
  }

  /**
   * Gets the script.
   * 
   * @return the script
   */
  public RScript getScript() {
    return m_Script;
  }

  /**
   * Tool tip about the script.
   * 
   * @return tool tip message
   */
  public String scriptTipText() {
    return "Script to pass into r. The input value can be accessed via '"
	+ INPUT + "'.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "script", Utils.shorten((m_Script.stringValue().length() == 0 ? "-none-" : m_Script.stringValue()), 40), "script: ");
  }

  /**
   * List of classes that can be used as input.
   */
  public Class[] accepts() {
    return new Class[] {
	Integer.class, 
	String.class, 
	Double.class,
	Double[].class, 
	Double[][].class,
	SpreadSheet.class
    };
  }

  /**
   * Sets up the connection to Rserve.
   */
  @Override
  public String setUp() {
    String 	result;
    
    result = super.setUp();

    if (result == null) {
      m_Rserve = (Rserve) ActorUtils.findClosestType(this, Rserve.class, true);
      if (m_Rserve == null)
	result = "Failed to find " + Rserve.class.getName() + " standalone with Rserve configuration!";
    }

    return result;
  }

  /**
   * Executes the flow, including reading the input and returning R's output.
   */
  @Override
  protected String doExecute() {
    if (m_RConn == null) {
      m_RConn = m_Rserve.newConnection();
      if (m_RConn == null)
	return "Could not connect to Rserve!";
    }
    
    String expr = getVariables().expand(m_Script.getValue());
    expr = Placeholders.expandStr(expr);

    try {
      if (m_InputToken.getPayload() instanceof Integer)
	m_RConn.assign(INPUT,
	    new int[] {((Integer) m_InputToken.getPayload()).intValue()});
      else if (m_InputToken.getPayload() instanceof String)
	m_RConn.assign(INPUT, (String) m_InputToken.getPayload());
      else if (m_InputToken.getPayload() instanceof Double)
	m_RConn.assign(INPUT,
	    new double[] {((Double) m_InputToken.getPayload()).doubleValue()});
      else if (m_InputToken.getPayload() instanceof Double[])
	m_RConn.assign(INPUT,
	    StatUtils.toDoubleArray((Double[]) m_InputToken.getPayload()));
      else if (m_InputToken.getPayload() instanceof Double[][]) {
	Double[][] temp = (Double[][]) m_InputToken.getPayload();
	double[][] dubMat = new double[temp.length][];
	for (int i = 0; i < temp.length; i++) {
	  dubMat[i] = StatUtils.toDoubleArray(temp[i]);
	}
	m_RConn.assign(INPUT, dubMat[0]);
	for (int i = 1; i < dubMat.length; i++) {
	  m_RConn.assign("tmp", dubMat[i]);
	  m_RConn.eval(INPUT + "<-rbind(" + INPUT + ",tmp)");
	}
      }
      else if (m_InputToken.getPayload() instanceof SpreadSheet) {
	m_RConn.assign(INPUT, 
	    RDataHelper.spreadsheetToDataframe((SpreadSheet) m_InputToken.getPayload()));
      }
      else {
	throw new IllegalStateException("Unhandled class: " + m_InputToken.getPayload().getClass());
      }

      String[] lines = expr.split("\r?\n");
      for (String line: lines) {
	try {
	  m_RConn.eval(line);
	}
	catch (Exception ex) {
	  return handleException("Error occurred evaluating: " + line, ex);
	}
      }
    }
    catch (Exception e) {// could handle REXPMismatchException separately
      return handleException("Error occurred calling Rserve:", e);
    }
    return null;
  }

  /**
   * Info about this flow.
   */
  @Override
  public String globalInfo() {
    return "Carries out an r command on the token passed in. "
	+ "The input can be accessed via '" + INPUT + "'.\n"
	+ "Variables are supported as well, e.g.: pow(X,@{exp}) with '@{exp}' "
	+ "being a variable available at execution time.";
  }

  /**
   * Closes the Rserve connection as the flow finishes.
   */
  @Override
  public void wrapUp() {
    if (m_Rserve != null) {
      m_Rserve.closeConnection(m_RConn);
      m_RConn  = null;
      m_Rserve = null;
    }
    
    super.wrapUp();
  }
}
