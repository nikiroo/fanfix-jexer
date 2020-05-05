package be.nikiroo.utils.serial.server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import be.nikiroo.utils.Version;

/**
 * This class implements a simple server that can listen for connections and
 * send/receive Strings.
 * <p>
 * Note: this {@link ServerString} has to be discarded after use (cannot be
 * started twice).
 * 
 * @author niki
 */
abstract public class ServerString extends Server {
	/**
	 * Create a new server that will start listening on the network when
	 * {@link ServerString#start()} is called.
	 * 
	 * @param port
	 *            the port to listen on, or 0 to assign any unallocated port
	 *            found (which can later on be queried via
	 *            {@link ServerString#getPort()}
	 * @param key
	 *            an optional key to encrypt all the communications (if NULL,
	 *            everything will be sent in clear text)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws UnknownHostException
	 *             if the IP address of the host could not be determined
	 * @throws IllegalArgumentException
	 *             if the port parameter is outside the specified range of valid
	 *             port values, which is between 0 and 65535, inclusive
	 */
	public ServerString(int port, String key) throws IOException {
		super(port, key);
	}

	/**
	 * Create a new server that will start listening on the network when
	 * {@link ServerString#start()} is called.
	 * 
	 * @param name
	 *            the server name (only used for debug info and traces)
	 * @param port
	 *            the port to listen on
	 * @param key
	 *            an optional key to encrypt all the communications (if NULL,
	 *            everything will be sent in clear text)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws UnknownHostException
	 *             if the IP address of the host could not be determined
	 * @throws IllegalArgumentException
	 *             if the port parameter is outside the specified range of valid
	 *             port values, which is between 0 and 65535, inclusive
	 */
	public ServerString(String name, int port, String key) throws IOException {
		super(name, port, key);
	}

	@Override
	protected ConnectActionServer createConnectActionServer(Socket s) {
		return new ConnectActionServerString(s, key) {
			@Override
			public void action(Version clientVersion) throws Exception {
				long id = getNextId();
				for (String data = rec(); data != null; data = rec()) {
					String rep = null;
					try {
						rep = onRequest(this, clientVersion, data, id);
						if (isClosing()) {
							return;
						}
					} catch (Exception e) {
						onError(e);
					}

					if (rep == null) {
						rep = "";
					}
					send(rep);
				}

				onRequestDone(id, getBytesReceived(), getBytesSent());
			}

			@Override
			protected void onError(Exception e) {
				ServerString.this.onError(e);
			}
		};
	}

	@Override
	protected ConnectActionClient getConnectionToMe()
			throws UnknownHostException, IOException {
		return new ConnectActionClientString(new Socket((String) null,
				getPort()), key);
	}

	/**
	 * This is the method that is called on each client request.
	 * <p>
	 * You are expected to react to it and return an answer (NULL will be
	 * converted to an empty {@link String}).
	 * 
	 * @param action
	 *            the client action
	 * @param clientVersion
	 *            the client version
	 * @param data
	 *            the data sent by the client
	 * @param id
	 *            an ID to identify this request (will also be re-used for
	 *            {@link ServerObject#onRequestDone(long, long, long)}.
	 * 
	 * @return the answer to return to the client
	 * 
	 * @throws Exception
	 *             in case of an exception, the error will only be logged
	 */
	protected String onRequest(ConnectActionServerString action,
			Version clientVersion, String data,
			@SuppressWarnings("unused") long id) throws Exception {
		// TODO: change to abstract when deprecated method is removed
		// Default implementation for compat
		return onRequest(action, clientVersion, data);
	}

	// Deprecated //

	/**
	 * @deprecated SSL support has been replaced by key-based encryption.
	 *             <p>
	 *             Please use the version with key encryption (this deprecated
	 *             version uses an empty key when <tt>ssl</tt> is TRUE and no
	 *             key (NULL) when <tt>ssl</tt> is FALSE).
	 */
	@Deprecated
	public ServerString(int port, boolean ssl) throws IOException {
		this(port, ssl ? "" : null);
	}

	/**
	 * @deprecated SSL support has been replaced by key-based encryption.
	 *             <p>
	 *             Please use the version with key encryption (this deprecated
	 *             version uses an empty key when <tt>ssl</tt> is TRUE and no
	 *             key (NULL) when <tt>ssl</tt> is FALSE).
	 */
	@Deprecated
	public ServerString(String name, int port, boolean ssl) throws IOException {
		this(name, port, ssl ? "" : null);
	}

	/**
	 * Will be called if the correct version is not overrided.
	 * 
	 * @deprecated use the version with the id.
	 * 
	 * @param action
	 *            the client action
	 * @param data
	 *            the data sent by the client
	 * 
	 * @return the answer to return to the client
	 * 
	 * @throws Exception
	 *             in case of an exception, the error will only be logged
	 */
	@Deprecated
	@SuppressWarnings("unused")
	protected String onRequest(ConnectActionServerString action,
			Version version, String data) throws Exception {
		return null;
	}
}
