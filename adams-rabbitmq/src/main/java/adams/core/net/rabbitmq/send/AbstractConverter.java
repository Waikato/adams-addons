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
 * AbstractConverter.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.core.net.rabbitmq.send;

import adams.core.MessageCollection;
import adams.core.QuickInfoSupporter;
import adams.core.Utils;
import adams.core.option.AbstractOptionHandler;
import adams.flow.core.Actor;
import adams.flow.core.Compatibility;
import adams.flow.core.FlowContextHandler;

/**
 * Ancestor for converters that convert the data to be sent.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractConverter
  extends AbstractOptionHandler
  implements QuickInfoSupporter, FlowContextHandler {

  private static final long serialVersionUID = 6503474005673475838L;

  /** the flow context. */
  protected transient Actor m_FlowContext;

  /**
   * Sets the flow context.
   *
   * @param value	the context
   */
  public void setFlowContext(Actor value) {
    m_FlowContext = value;
  }

  /**
   * Returns the flow context.
   *
   * @return		the context, null if not available
   */
  public Actor getFlowContext() {
    return m_FlowContext;
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   * <br>
   * Default implementation returns null.
   *
   * @return		null if no info available, otherwise short string
   */
  public String getQuickInfo() {
    return null;
  }

  /**
   * Returns the classes that the converter accepts.
   *
   * @return		the classes
   */
  public abstract Class[] accepts();

  /**
   * Hook method for checks.
   *
   * @param payload	the payload to check
   * @return		null if sucessfully checked, otherwise error message
   */
  public String check(Object payload) {
    Compatibility	comp;

    if (payload == null)
      return "No payload provided!";

    comp = new Compatibility();
    if (!comp.isCompatible(new Class[]{payload.getClass()}, accepts()))
      return "Payload is " + Utils.classToString(payload) + ", but converter only accepts: " + Utils.classesToString(accepts());

    return null;
  }

  /**
   * Converts the payload.
   *
   * @param payload	the payload
   * @param errors	for recording errors
   * @return		null if failed to convert, otherwise byte array
   */
  protected abstract byte[] doConvert(Object payload, MessageCollection errors);

  /**
   * Converts the payload.
   *
   * @param payload	the payload
   * @param errors	for recording errors
   * @return		null if failed to convert, otherwise byte array
   */
  public byte[] convert(Object payload, MessageCollection errors) {
    byte[]	result;
    String	msg;

    msg = check(payload);
    if (msg != null) {
      errors.add(msg);
      return null;
    }

    result = doConvert(payload, errors);
    if (!errors.isEmpty())
      result = null;

    return result;
  }
}
