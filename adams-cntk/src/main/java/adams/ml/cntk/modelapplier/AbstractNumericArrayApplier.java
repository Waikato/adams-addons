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
 * AbstractNumericArrayApplier.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.ml.cntk.modelapplier;

import adams.core.License;
import adams.core.annotation.MixedCopyright;

import java.util.logging.Level;

/**
 * Ancestor for scoring numeric arrays.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 * @param <I> the input data
 */
@MixedCopyright(
  author = "CNTK",
  copyright = "Microsoft",
  license = License.MIT,
  url = "https://github.com/Microsoft/CNTK/blob/v2.0/Tests/EndToEndTests/EvalClientTests/JavaEvalTest/src/Main.java"
)
public abstract class AbstractNumericArrayApplier<I>
  extends AbstractModelApplier<I, float[]>{

  private static final long serialVersionUID = 7933924670965842681L;

  /**
   * Returns the class that the applier generates.
   *
   * @return		the class
   */
  public Class generates() {
    return float[].class;
  }

  /**
   * Performs the actual application of the model.
   *
   * @param input	the input
   * @return		the score
   */
  protected float[] applyModel(double[] input) {
    float[]	values;
    int		i;

    values = new float[input.length];
    for (i = 0; i < input.length; i++)
      values[i] = (float) input[i];

    return applyModel(values);
  }

  /**
   * Performs the actual application of the model.
   *
   * @param input	the input
   * @return		the score
   */
  protected float[] applyModel(float[] input) {
    try {
      return m_Wrapper.predict(input);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to make prediction!", e);
      return new float[0];
    }
  }
}
