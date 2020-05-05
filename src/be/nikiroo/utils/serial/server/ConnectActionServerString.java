package be.nikiroo.utils.serial.server;

import java.io.IOException;
import java.net.Socket;

/**
 * Class used for the server basic handling.
 * <p>
 * It represents a single action: a server is expected to execute one action for
 * each client action.
 * 
 * @author niki
 */
public class ConnectActionServerString extends ConnectActionServer {
	/**
	 * Create a new {@link ConnectActionServerString} as the server version.
	 * 
	 * @param s
	 *            the socket to bind to
	 * @param key
	 *            an optional key to encrypt all the communications (if NULL,
	 *            everything will be sent in clear text)
	 */
	public ConnectActionServerString(Socket s, String key) {
		super(s, key);
	}

	/**
	 * Serialise and send the given object to the client.
	 * 
	 * @param data
	 *            the data to send
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void send(String data) throws IOException {
		action.sendString(data);
	}

	/**
	 * (Flush the data to the client if needed and) retrieve its answer.
	 * 
	 * @return the answer if it is available, or NULL if not
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public String rec() throws IOException {
		return action.recString();
	}
}
