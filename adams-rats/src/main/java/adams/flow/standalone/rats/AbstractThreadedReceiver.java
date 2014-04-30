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
 * AbstractThreadedReceiver.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.standalone.rats;

/**
 * Starts a thread with a {@link Worker} instance that does the heavy lifting.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractThreadedReceiver
  extends AbstractReceiver {

  /** for serialization. */
  private static final long serialVersionUID = 393993610018084327L;
  
  /** the currently running worker. */
  protected Worker m_Worker;
  
  /**
   * Returns the {@link Worker} instance that performs the actual receiving.
   * 
   * @return		the {@link Worker} instance to use
   */
  protected abstract Worker newWorker();
  
  /**
   * Performs the actual reception of data. Starts up the {@link Worker} 
   * instance in a thread.
   * 
   * @throws Execption	if receiving of data fails
   */
  @Override
  protected void doReceive() throws Exception {
    if (isLoggingEnabled())
      getLogger().info("Starting receiver worker...");
    m_Worker = newWorker();
    m_Worker.setLoggingLevel(getLoggingLevel());
    new Thread(m_Worker).start();
  }
  
  /**
   * Stops the execution.
   */
  @Override
  public void stopExecution() {
    if (!m_Stopped) {
      if (m_Worker != null) {
	if (isLoggingEnabled())
	  getLogger().info("Stopping receiver worker...");
	m_Worker.stopExecution();
	while (m_Worker.isRunning()) {
	  if (isLoggingEnabled())
	    getLogger().info("Waiting for receiver worker to stop...");
	  try {
	    synchronized(this) {
	      wait(100);
	    }
	  }
	  catch (Exception e) {
	    e.printStackTrace();
	    // ignored
	  }
	}
	m_Worker = null;
      }
    }
    super.stopExecution();
  }
}
