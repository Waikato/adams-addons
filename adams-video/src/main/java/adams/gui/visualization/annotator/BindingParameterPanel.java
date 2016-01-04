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

/**
 * BindingParameterPanel.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.annotator;

import adams.gui.core.GUIHelper;
import adams.gui.core.ParameterPanel;

import javax.swing.*;

/**
 * A Parameter Panel that does the work of taking user input and turning it into a binding
 *
 * @author sjb90
 * @version $Revision$
 */
public class BindingParameterPanel extends ParameterPanel {

  JTextField m_NameField;
  JTextField m_BindingField;
  JCheckBox m_Toggleable;
  JCheckBox m_Inverted;

  @Override
  protected void initGUI() {
    super.initGUI();

    m_NameField = new JTextField();
    m_BindingField = new JTextField();
    m_Toggleable = new JCheckBox();
    m_Inverted = new JCheckBox();

    addParameter(false, "Name", m_NameField);
    addParameter(false, "Binding", m_BindingField);
    addParameter(false, "Toggleable", m_Toggleable);
    addParameter(false, "Inverted", m_Inverted);
  }

  /**
   * Clears all fields so we can enter fresh data.
   */
  public void clearFields() {
    m_NameField.setText("");
    m_BindingField.setText("");
    m_Toggleable.setSelected(false);
    m_Inverted.setSelected(false);

  }

  /**
   * Returns the binding based on the currently entered info
   */
  public Binding getBinding() {
    Binding b = new Binding(m_NameField.getText(), m_BindingField.getText(),m_Toggleable.isSelected(),
      m_Inverted.isSelected());
    clearFields();
    return b;
  }
}