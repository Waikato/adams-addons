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
 * TwitterListener.java
 * Copyright (C) 2010-2016 University of Waikato, Hamilton, New Zealand
 * Copyright (c) 2007-2010, Yusuke Yamamoto
 */

package adams.flow.source;

import adams.core.License;
import adams.core.Pausable;
import adams.core.QuickInfoHelper;
import adams.core.Stoppable;
import adams.core.annotation.MixedCopyright;
import adams.core.net.TwitterHelper;
import adams.flow.core.Token;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.io.Serializable;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Uses the Twitter streaming API to retrieve tweets.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;twitter4j.Status<br>
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
 * &nbsp;&nbsp;&nbsp;default: TwitterListener
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
 * <pre>-max-updates &lt;int&gt; (property: maxStatusUpdates)
 * &nbsp;&nbsp;&nbsp;The maximum number of status updates to output; use &lt;=0 for unlimited.
 * &nbsp;&nbsp;&nbsp;default: 100
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @author  Yusuke Yamamoto
 * @version $Revision$
 */
@MixedCopyright(
        author = "Yusuke Yamamoto",
        copyright = "2007-2010 Yusuke Yamamoto",
        license = License.APACHE2,
        url = "http://twitter4j.org/en/code-examples.html"
)
public class TwitterListener
        extends AbstractSource {

  /** for serialization. */
  private static final long serialVersionUID = -7777610085728160967L;

  /**
   * Listener for a twitter stream.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision$
   */
  public static class Listener
    implements Serializable, Pausable, Stoppable, StatusListener {

    private static final long serialVersionUID = 5406360301457780558L;

    /** the owner. */
    protected TwitterListener m_Owner;

    /** for accessing the twitter streaming API. */
    protected twitter4j.TwitterStream m_Twitter;

    /** the counter for tweets. */
    protected int m_Count;

    /** the next available Status. */
    protected Status m_Next;

    /** the listener is paused. */
    protected boolean m_Paused;

    /** whether the listener is running. */
    protected boolean m_Listening;

    /**
     * Initializes the listener.
     *
     * @param owner	the owning actor
     */
    public Listener(TwitterListener owner) {
      if (owner == null)
	throw new IllegalArgumentException("Owner cannot be null!");

      m_Owner   = owner;
      m_Count   = 0;
      m_Twitter = TwitterHelper.getTwitterStreamConnection(getOwner());
    }

    /**
     * Returns the owner.
     *
     * @return		the owner
     */
    public TwitterListener getOwner() {
      return m_Owner;
    }

    /**
     * Starts the listening.
     */
    public void startExecution() {
      try {
	m_Twitter.addListener(this);
	m_Twitter.sample();
	m_Listening = true;
      }
      catch (Exception e) {
	m_Twitter.removeListener(this);
	getOwner().getLogger().log(Level.SEVERE, "Failed to start listener!", e);
      }
    }

    /**
     * Returns whether the listener is active.
     *
     * @return		true if active
     */
    public boolean isListening() {
      return m_Listening;
    }

    /**
     * Pauses the execution (if still listening).
     */
    @Override
    public void pauseExecution() {
      if (m_Listening)
	m_Paused = true;
    }

    /**
     * Returns whether the object is currently paused.
     *
     * @return		true if object is paused
     */
    @Override
    public boolean isPaused() {
      return m_Paused;
    }

    /**
     * Resumes the execution.
     */
    @Override
    public void resumeExecution() {
      m_Paused = false;
    }

    /**
     * When receiving a status.
     *
     * @param status	the status
     */
    @Override
    public void onStatus(Status status) {
      if (m_Listening && !m_Paused) {
	if ((getOwner().getMaxStatusUpdates() > 0) && (m_Count >= getOwner().getMaxStatusUpdates()))
	  stopExecution();
	else
	  m_Next = status;
      }
    }

    /**
     * Ignored.
     *
     * @param statusDeletionNotice
     */
    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    }

    /**
     * Ignored.
     *
     * @param i
     */
    @Override
    public void onTrackLimitationNotice(int i) {
    }

    /**
     * Ignored.
     *
     * @param l
     * @param l1
     */
    @Override
    public void onScrubGeo(long l, long l1) {
    }

    /**
     * Outputs a stall warning.
     *
     * @param stallWarning	the warning
     */
    @Override
    public void onStallWarning(StallWarning stallWarning) {
      getOwner().getLogger().warning(stallWarning.toString());
    }

    /**
     * Gets called if an exception is encountered.
     *
     * @param e			the exception
     */
    @Override
    public void onException(Exception e) {
      // TODO stop listening?
      getOwner().getLogger().log(Level.SEVERE, "Exception encountered while listening!", e);
    }

    /**
     * Stops the execution.
     */
    @Override
    public void stopExecution() {
      m_Listening = false;
      m_Paused    = false;
      m_Twitter.removeListener(this);
      try {
	m_Twitter.shutdown();
      }
      catch (Exception e) {
	// ignored
      }
      m_Twitter.cleanUp();
    }

    /**
     * Returns whether there is another update available.
     *
     * @return		true if another update available
     */
    public boolean hasNext() {
      return m_Listening || (m_Next != null);
    }

    /**
     * Retrieves the next status update.
     *
     * @return		the next status
     */
    public Status next() {
     Status	result;
      int	count;

      result = null;

      count = 0;
      while (result == null) {
	result = m_Next;
	count++;

	if (result == null) {
	  if (m_Listening) {
	    try {
	      synchronized(this) {
		wait(50);
	      }
	    }
	    catch (Exception e) {
	      // ignored
	    }
	  }
	  else {
	    break;
	  }
	}

	// problem with obtaining data?
	if (count == 100)
	  break;
      }

      // only increment counter when the status update was actually used
      if (result != null) {
	m_Count++;
	if (getOwner().isLoggingEnabled() && (m_Count % 100 == 0))
	  getOwner().getLogger().info("status updates: " + m_Count);
      }

      m_Next = null;

      return result;
    }
  }

  /** the maximum number of status updates to output. */
  protected int m_MaxStatusUpdates;

  /** the thread for retrieving the status updates. */
  protected transient Listener m_Listener;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Uses the Twitter streaming API to retrieve tweets.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
            "max-updates", "maxStatusUpdates",
            100, -1, null);
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "maxStatusUpdates", ((m_MaxStatusUpdates <= 0) ? "unlimited " : "" + m_MaxStatusUpdates) + " status updates");
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the classes
   */
  public Class[] generates() {
    return new Class[]{Status.class};
  }

  /**
   * Sets the maximum number of status updates to output.
   *
   * @param value	the maximum number
   */
  public void setMaxStatusUpdates(int value) {
    m_MaxStatusUpdates = value;
    reset();
  }

  /**
   * Returns the maximum number of status updates to output.
   *
   * @return		the maximum number
   */
  public int getMaxStatusUpdates() {
    return m_MaxStatusUpdates;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxStatusUpdatesTipText() {
    return "The maximum number of status updates to output; use <=0 for unlimited.";
  }

  /**
   * Initializes the sub-actors for flow execution.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  public String setUp() {
    String	result;

    result = super.setUp();

    if (result == null) {
      if (m_Listener != null)
        m_Listener.stopExecution();
      m_Listener = new Listener(this);
    }

    return result;
  }

  /**
   * Executes the flow item.
   *
   * @return		always null
   */
  @Override
  protected String doExecute() {
    String	result;
    int		count;

    result = null;

    try {
      m_Listener.startExecution();
    }
    catch (IllegalThreadStateException ie) {
      // ignored
    }
    catch (Exception e) {
      result = handleException("Failed to start listener thread!", e);
    }

    // wait for thread to start up
    count = 0;
    while (!m_Listener.isListening()) {
      count++;
      try {
        synchronized(this) {
          wait(50);
        }
      }
      catch (Exception e) {
      }

      // problem with launching thread?
      if (count == 100) {
        result = "Thread timed out??";
        break;
      }
    }

    return result;
  }

  /**
   * Stops listening to the twitter stream.
   */
  protected void stopListening() {
    if (m_Listener != null)
      m_Listener.stopExecution();
  }

  /**
   * Stops the execution.
   */
  @Override
  public void stopExecution() {
    stopListening();
    super.stopExecution();
  }

  /**
   * Returns the generated token.
   *
   * @return		the generated token
   */
  public Token output() {
    Token	result;
    Status	status;

    result = null;
    status = m_Listener.next();
    if (status != null)
      result = new Token(status);

    return result;
  }

  /**
   * Checks whether there is pending output to be collected after
   * executing the flow item.
   *
   * @return		true if there is pending output
   */
  public boolean hasPendingOutput() {
    return m_Executed && (m_Listener != null) && (m_Listener.hasNext());
  }

  /**
   * Cleans up after the execution has finished.
   */
  @Override
  public void wrapUp() {
    super.wrapUp();

    m_Listener = null;
  }
}
