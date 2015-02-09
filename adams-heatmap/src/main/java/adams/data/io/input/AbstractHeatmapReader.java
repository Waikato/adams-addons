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
 * AbstractHeatmapReader.java
 * Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.core.ClassLister;
import adams.data.heatmap.Heatmap;
import adams.data.report.DataType;
import adams.data.report.Field;

/**
 * Ancestor for readers that read heatmaps.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractHeatmapReader
  extends AbstractDataContainerReader<Heatmap> {

  /** for serialization. */
  private static final long serialVersionUID = -2206748744422806213L;

  /**
   * For performing post-processing.
   */
  protected void postProcessData() {
    super.postProcessData();

    for (Heatmap map: m_ReadData) {
      // set filename
      if (map.hasReport()) {
	map.getReport().addField(new Field(Heatmap.FIELD_FILENAME, DataType.NUMERIC));
	map.getReport().setStringValue(Heatmap.FIELD_FILENAME, m_Input.getAbsolutePath());
      }
      // fix ID
      if (map.getID().trim().length() == 0)
	map.setID(m_Input.getName());
    }
  }

  /**
   * Returns a list with classnames of readers.
   *
   * @return		the reader classnames
   */
  public static String[] getReaders() {
    return ClassLister.getSingleton().getClassnames(AbstractHeatmapReader.class);
  }
}
