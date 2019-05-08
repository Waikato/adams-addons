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
 * RabbitMQHelper.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.core.net.rabbitmq;

import adams.core.Utils;
import adams.core.logging.LoggingSupporter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.util.List;

/**
 * Helper class for RabbitMQ operations.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class RabbitMQHelper {

  /**
   * Creates a new channel and returns it.
   *
   * @param connection 	the connection to use
   * @return		the channel, null if failed to create or no connection available
   */
  public static Channel createChannel(Connection connection) {
    return createChannel(null, connection);
  }

  /**
   * Creates a new channel and returns it.
   *
   * @param logging 	for logging errors
   * @param connection 	the connection to use
   * @return		the channel, null if failed to create or no connection available
   */
  public static Channel createChannel(LoggingSupporter logging, Connection connection) {
    try {
      if (connection != null)
	return connection.createChannel();
    }
    catch (Exception e) {
      if (logging != null)
	Utils.handleException(logging, "Failed to create channel!", e);
    }
    return null;
  }

  /**
   * Closes the channel.
   *
   * @param channel	the channel to close, can be null
   */
  public static void closeQuietly(Channel channel) {
    if (channel != null) {
      try {
	channel.close();
      }
      catch (Exception e) {
	// ignored
      }
    }
  }

  /**
   * Closes the connection and deletes any auto-created queues.
   *
   * @param connection	the connection to close
   * @param queues	the auto-created queues to close
   */
  public static void closeQuietly(Connection connection, List<String> queues) {
    Channel	channel;

    if (connection != null) {
      if (connection.isOpen()) {
        // delete auto-created queues
        if (queues.size() > 0) {
          channel = createChannel(null, connection);
          if (channel != null) {
	    for (String queue : queues) {
	      try {
		channel.queueDelete(queue);
	      }
	      catch (Exception e) {
	        // ignored
	      }
	    }
	    try {
	      channel.close();
	    }
	    catch (Exception e) {
	      // ignored
	    }
	  }
        }

        // close connection
	try {
	  connection.close();
	}
	catch (Exception e) {
	  // ignored
	}
      }
    }
  }
}
