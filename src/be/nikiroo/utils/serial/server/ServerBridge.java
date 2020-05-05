package be.nikiroo.utils.serial.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;

import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.TraceHandler;
import be.nikiroo.utils.Version;
import be.nikiroo.utils.serial.Importer;

/**
 * This class implements a simple server that can bridge two other
 * {@link Server}s.
 * <p>
 * It can, of course, inspect the data that goes through it (by default, it
 * prints traces of the data).
 * <p>
 * Note: this {@link ServerBridge} has to be discarded after use (cannot be
 * started twice).
 * 
 * @author niki
 */
public class ServerBridge extends Server {
	private final String forwardToHost;
	private final int forwardToPort;
	private final String forwardToKey;

	/**
	 * Create a new server that will start listening on the network when
	 * {@link ServerBridge#start()} is called.
	 * 
	 * @param port
	 *            the port to listen on, or 0 to assign any unallocated port
	 *            found (which can later on be queried via
	 *            {@link ServerBridge#getPort()}
	 * @param key
	 *            an optional key to encrypt all the communications (if NULL,
	 *            everything will be sent in clear text)
	 * @param forwardToHost
	 *            the host server to forward the calls to
	 * @param forwardToPort
	 *            the host port to forward the calls to
	 * @param forwardToKey
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
	public ServerBridge(int port, String key, String forwardToHost,
			int forwardToPort, String forwardToKey) throws IOException {
		super(port, key);
		this.forwardToHost = forwardToHost;
		this.forwardToPort = forwardToPort;
		this.forwardToKey = forwardToKey;
	}

	/**
	 * Create a new server that will start listening on the network when
	 * {@link ServerBridge#start()} is called.
	 * 
	 * @param name
	 *            the server name (only used for debug info and traces)
	 * @param port
	 *            the port to listen on
	 * @param key
	 *            an optional key to encrypt all the communications (if NULL,
	 *            everything will be sent in clear text)
	 * @param forwardToHost
	 *            the host server to forward the calls to
	 * @param forwardToPort
	 *            the host port to forward the calls to
	 * @param forwardToKey
	 *            an optional key to encrypt all the communications (if NULL,
	 *            everything will be sent in clear text) use an SSL connection
	 *            for the forward server or not
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws UnknownHostException
	 *             if the IP address of the host could not be determined
	 * @throws IllegalArgumentException
	 *             if the port parameter is outside the specified range of valid
	 *             port values, which is between 0 and 65535, inclusive
	 */
	public ServerBridge(String name, int port, String key,
			String forwardToHost, int forwardToPort, String forwardToKey)
			throws IOException {
		super(name, port, key);
		this.forwardToHost = forwardToHost;
		this.forwardToPort = forwardToPort;
		this.forwardToKey = forwardToKey;
	}

	/**
	 * The traces handler for this {@link Server}.
	 * <p>
	 * The trace levels are handled as follow:
	 * <ul>
	 * <li>1: it will only print basic IN/OUT messages with length</li>
	 * <li>2: it will try to interpret it as an object (SLOW) and print the
	 * object class if possible</li>
	 * <li>3: it will try to print the {@link Object#toString()} value, or the
	 * data if it is not an object</li>
	 * <li>4: it will also print the unzipped serialised value if it is an
	 * object</li>
	 * </ul>
	 * 
	 * @param tracer
	 *            the new traces handler
	 */
	@Override
	public void setTraceHandler(TraceHandler tracer) {
		super.setTraceHandler(tracer);
	}

	@Override
	protected ConnectActionServer createConnectActionServer(Socket s) {
		// Bad impl, not up to date (should work, but not efficient)
		return new ConnectActionServerString(s, key) {
			@Override
			public void action(Version clientVersion) throws Exception {
				onClientContact(clientVersion);
				final ConnectActionServerString bridge = this;

				try {
					new ConnectActionClientString(forwardToHost, forwardToPort,
							forwardToKey) {
						@Override
						public void action(Version serverVersion)
								throws Exception {
							onServerContact(serverVersion);

							for (String fromClient = bridge.rec(); fromClient != null; fromClient = bridge
									.rec()) {
								onRec(fromClient);
								String fromServer = send(fromClient);
								onSend(fromServer);
								bridge.send(fromServer);
							}

							getTraceHandler().trace("=== DONE", 1);
							getTraceHandler().trace("", 1);
						}

						@Override
						protected void onError(Exception e) {
							ServerBridge.this.onError(e);
						}
					}.connect();
				} catch (Exception e) {
					ServerBridge.this.onError(e);
				}
			}
		};
	}

	/**
	 * This is the method that is called each time a client contact us.
	 */
	protected void onClientContact(Version clientVersion) {
		getTraceHandler().trace(">>> CLIENT " + clientVersion);
	}

	/**
	 * This is the method that is called each time a client contact us.
	 */
	protected void onServerContact(Version serverVersion) {
		getTraceHandler().trace("<<< SERVER " + serverVersion);
		getTraceHandler().trace("");
	}

	/**
	 * This is the method that is called each time a client contact us.
	 * 
	 * @param data
	 *            the data sent by the client
	 */
	protected void onRec(String data) {
		trace(">>> CLIENT", data);
	}

	/**
	 * This is the method that is called each time the forwarded server contact
	 * us.
	 * 
	 * @param data
	 *            the data sent by the client
	 */
	protected void onSend(String data) {
		trace("<<< SERVER", data);
	}

	@Override
	protected ConnectActionClient getConnectionToMe()
			throws UnknownHostException, IOException {
		return new ConnectActionClientString(new Socket((String) null,
				getPort()), key);
	}

	@Override
	public void run() {
		getTraceHandler().trace(
				getName() + ": will forward to " + forwardToHost + ":"
						+ forwardToPort + " ("
						+ (forwardToKey != null ? "encrypted" : "plain text")
						+ ")");
		super.run();
	}

	/**
	 * Trace the data with the given prefix.
	 * 
	 * @param prefix
	 *            the prefix (client, server, version...)
	 * @param data
	 *            the data to trace
	 */
	private void trace(String prefix, String data) {
		int size = data == null ? 0 : data.length();
		String ssize = StringUtils.formatNumber(size) + "bytes";

		getTraceHandler().trace(prefix + ": " + ssize, 1);

		if (getTraceHandler().getTraceLevel() >= 2) {
			try {
				while (data.startsWith("ZIP:") || data.startsWith("B64:")) {
					if (data.startsWith("ZIP:")) {
						data = StringUtils.unzip64s(data.substring(4));
					} else if (data.startsWith("B64:")) {
						data = StringUtils.unzip64s(data.substring(4));
					}
				}

				InputStream stream = new ByteArrayInputStream(
						StringUtils.getBytes(data));
				try {
					Object obj = new Importer().read(stream).getValue();
					if (obj == null) {
						getTraceHandler().trace("NULL", 2);
						getTraceHandler().trace("NULL", 3);
						getTraceHandler().trace("NULL", 4);
					} else {
						if (obj.getClass().isArray()) {
							getTraceHandler().trace(
									"(" + obj.getClass() + ") with "
											+ Array.getLength(obj)
											+ "element(s)", 3);
						} else {
							getTraceHandler().trace("(" + obj.getClass() + ")",
									2);
						}
						getTraceHandler().trace("" + obj.toString(), 3);
						getTraceHandler().trace(data, 4);
					}
				} finally {
					stream.close();
				}
			} catch (NoSuchMethodException e) {
				getTraceHandler().trace("(not an object)", 2);
				getTraceHandler().trace(data, 3);
				getTraceHandler().trace("", 4);
			} catch (NoSuchFieldException e) {
				getTraceHandler().trace(
						"(incompatible: " + e.getMessage() + ")", 2);
				getTraceHandler().trace(data, 3);
				getTraceHandler().trace("", 4);
			} catch (ClassNotFoundException e) {
				getTraceHandler().trace(
						"(unknown object: " + e.getMessage() + ")", 2);
				getTraceHandler().trace(data, 3);
				getTraceHandler().trace("", 4);
			} catch (Exception e) {
				getTraceHandler().trace(
						"(decode error: " + e.getMessage() + ")", 2);
				getTraceHandler().trace(data, 3);
				getTraceHandler().trace("", 4);
			}

			getTraceHandler().trace("", 2);
		}
	}
}
