package be.nikiroo.utils.serial.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLException;

import be.nikiroo.utils.CryptUtils;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.Version;
import be.nikiroo.utils.serial.Exporter;
import be.nikiroo.utils.serial.Importer;
import be.nikiroo.utils.streams.BufferedOutputStream;
import be.nikiroo.utils.streams.NextableInputStream;
import be.nikiroo.utils.streams.NextableInputStreamStep;
import be.nikiroo.utils.streams.ReplaceInputStream;
import be.nikiroo.utils.streams.ReplaceOutputStream;

/**
 * Base class used for the client/server basic handling.
 * <p>
 * It represents a single action: a client is expected to only execute one
 * action, while a server is expected to execute one action for each client
 * action.
 * 
 * @author niki
 */
abstract class ConnectAction {
	// We separate each "packet" we send with this character and make sure it
	// does not occurs in the message itself.
	static private char STREAM_SEP = '\b';
	static private String[] STREAM_RAW = new String[] { "\\", "\b" };
	static private String[] STREAM_CODED = new String[] { "\\\\", "\\b" };

	private Socket s;
	private boolean server;

	private Version clientVersion;
	private Version serverVersion;

	private CryptUtils crypt;

	private Object lock = new Object();
	private NextableInputStream in;
	private BufferedOutputStream out;
	private boolean contentToSend;

	/**
	 * Method that will be called when an action is performed on either the
	 * client or server this {@link ConnectAction} represent.
	 * 
	 * @param version
	 *            the version on the other side of the communication (client or
	 *            server)
	 * 
	 * @throws Exception
	 *             in case of I/O error
	 */
	abstract protected void action(Version version) throws Exception;

	/**
	 * Method called when we negotiate the version with the client.
	 * <p>
	 * Thus, it is only called on the server.
	 * <p>
	 * Will return the actual server version by default.
	 * 
	 * @param clientVersion
	 *            the client version
	 * 
	 * @return the version to send to the client
	 */
	abstract protected Version negotiateVersion(Version clientVersion);

	/**
	 * Handler called when an unexpected error occurs in the code.
	 * 
	 * @param e
	 *            the exception that occurred, SSLException usually denotes a
	 *            crypt error
	 */
	abstract protected void onError(Exception e);

	/**
	 * Create a new {@link ConnectAction}.
	 * 
	 * @param s
	 *            the socket to bind to
	 * @param server
	 *            TRUE for a server action, FALSE for a client action (will
	 *            impact the process)
	 * @param key
	 *            an optional key to encrypt all the communications (if NULL,
	 *            everything will be sent in clear text)
	 * @param version
	 *            the client-or-server version (depending upon the boolean
	 *            parameter <tt>server</tt>)
	 */
	protected ConnectAction(Socket s, boolean server, String key,
			Version version) {
		this.s = s;
		this.server = server;
		if (key != null) {
			crypt = new CryptUtils(key);
		}

		if (version == null) {
			version = new Version();
		}

		if (server) {
			serverVersion = version;
		} else {
			clientVersion = version;
		}
	}

	/**
	 * The version of this client-or-server.
	 * 
	 * @return the version
	 */
	public Version getVersion() {
		if (server) {
			return serverVersion;
		}

		return clientVersion;
	}

	/**
	 * The total amount of bytes received.
	 * 
	 * @return the amount of bytes received
	 */
	public long getBytesReceived() {
		return in.getBytesRead();
	}

	/**
	 * The total amount of bytes sent.
	 * 
	 * @return the amount of bytes sent
	 */
	public long getBytesWritten() {
		return out.getBytesWritten();
	}

	/**
	 * Actually start the process (this is synchronous).
	 */
	public void connect() {
		try {
			in = new NextableInputStream(s.getInputStream(),
					new NextableInputStreamStep(STREAM_SEP));
			try {
				out = new BufferedOutputStream(s.getOutputStream());
				try {
					// Negotiate version
					Version version;
					if (server) {
						String HELLO = recString();
						if (HELLO == null || !HELLO.startsWith("VERSION ")) {
							throw new SSLException(
									"Client used bad encryption key");
						}
						version = negotiateVersion(new Version(
								HELLO.substring("VERSION ".length())));
						sendString("VERSION " + version);
					} else {
						String HELLO = sendString("VERSION " + clientVersion);
						if (HELLO == null || !HELLO.startsWith("VERSION ")) {
							throw new SSLException(
									"Server did not accept the encryption key");
						}
						version = new Version(HELLO.substring("VERSION "
								.length()));
					}

					// Actual code
					action(version);
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
		} catch (Exception e) {
			onError(e);
		} finally {
			try {
				s.close();
			} catch (Exception e) {
				onError(e);
			}
		}
	}

	/**
	 * Serialise and send the given object to the counter part (and, only for
	 * client, return the deserialised answer -- the server will always receive
	 * NULL).
	 * 
	 * @param data
	 *            the data to send
	 * 
	 * @return the answer (which can be NULL if no answer, or NULL for an answer
	 *         which is NULL) if this action is a client, always NULL if it is a
	 *         server
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws NoSuchFieldException
	 *             if the serialised data contains information about a field
	 *             which does actually not exist in the class we know of
	 * @throws NoSuchMethodException
	 *             if a class described in the serialised data cannot be created
	 *             because it is not compatible with this code
	 * @throws ClassNotFoundException
	 *             if a class described in the serialised data cannot be found
	 */
	protected Object sendObject(Object data) throws IOException,
			NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
		return send(out, data, false);
	}

	/**
	 * Reserved for the server: flush the data to the client and retrieve its
	 * answer.
	 * <p>
	 * Also used internally for the client (only do something if there is
	 * contentToSend).
	 * <p>
	 * Will only flush the data if there is contentToSend.
	 * 
	 * @return the deserialised answer (which can actually be NULL)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws NoSuchFieldException
	 *             if the serialised data contains information about a field
	 *             which does actually not exist in the class we know of
	 * @throws NoSuchMethodException
	 *             if a class described in the serialised data cannot be created
	 *             because it is not compatible with this code
	 * @throws ClassNotFoundException
	 *             if a class described in the serialised data cannot be found
	 * @throws java.lang.NullPointerException
	 *             if the counter part has no data to send
	 */
	protected Object recObject() throws IOException, NoSuchFieldException,
			NoSuchMethodException, ClassNotFoundException,
			java.lang.NullPointerException {
		return rec(false);
	}

	/**
	 * Send the given string to the counter part (and, only for client, return
	 * the answer -- the server will always receive NULL).
	 * 
	 * @param line
	 *            the data to send (we will add a line feed)
	 * 
	 * @return the answer if this action is a client (without the added line
	 *         feed), NULL if it is a server
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws SSLException
	 *             in case of crypt error
	 */
	protected String sendString(String line) throws IOException {
		try {
			return (String) send(out, line, true);
		} catch (NoSuchFieldException e) {
			// Cannot happen
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// Cannot happen
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// Cannot happen
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Reserved for the server (externally): flush the data to the client and
	 * retrieve its answer.
	 * <p>
	 * Also used internally for the client (only do something if there is
	 * contentToSend).
	 * <p>
	 * Will only flush the data if there is contentToSend.
	 * 
	 * @return the answer (which can be NULL if no more content)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws SSLException
	 *             in case of crypt error
	 */
	protected String recString() throws IOException {
		try {
			return (String) rec(true);
		} catch (NoSuchFieldException e) {
			// Cannot happen
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// Cannot happen
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// Cannot happen
			e.printStackTrace();
		} catch (NullPointerException e) {
			// Should happen
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Serialise and send the given object to the counter part (and, only for
	 * client, return the deserialised answer -- the server will always receive
	 * NULL).
	 * 
	 * @param out
	 *            the stream to write to
	 * @param data
	 *            the data to write
	 * @param asString
	 *            TRUE to write it as a String, FALSE to write it as an Object
	 * 
	 * @return the answer (which can be NULL if no answer, or NULL for an answer
	 *         which is NULL) if this action is a client, always NULL if it is a
	 *         server
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws SSLException
	 *             in case of crypt error
	 * @throws IOException
	 *             in case of I/O error
	 * @throws NoSuchFieldException
	 *             if the serialised data contains information about a field
	 *             which does actually not exist in the class we know of
	 * @throws NoSuchMethodException
	 *             if a class described in the serialised data cannot be created
	 *             because it is not compatible with this code
	 * @throws ClassNotFoundException
	 *             if a class described in the serialised data cannot be found
	 */
	private Object send(BufferedOutputStream out, Object data, boolean asString)
			throws IOException, NoSuchFieldException, NoSuchMethodException,
			ClassNotFoundException, java.lang.NullPointerException {

		synchronized (lock) {
			OutputStream sub;
			if (crypt != null) {
				sub = crypt.encrypt64(out.open());
			} else {
				sub = out.open();
			}

			sub = new ReplaceOutputStream(sub, STREAM_RAW, STREAM_CODED);
			try {
				if (asString) {
					sub.write(StringUtils.getBytes(data.toString()));
				} else {
					new Exporter(sub).append(data);
				}
			} finally {
				sub.close();
			}

			out.write(STREAM_SEP);

			if (server) {
				out.flush();
				return null;
			}

			contentToSend = true;
			try {
				return rec(asString);
			} catch (NullPointerException e) {
				// We accept no data here for Objects
			}

			return null;
		}
	}

	/**
	 * Reserved for the server: flush the data to the client and retrieve its
	 * answer.
	 * <p>
	 * Also used internally for the client (only do something if there is
	 * contentToSend).
	 * <p>
	 * Will only flush the data if there is contentToSend.
	 * <p>
	 * Note that the behaviour is slightly different for String and Object
	 * reading regarding exceptions:
	 * <ul>
	 * <li>NULL means that the counter part has no more data to send</li>
	 * <li>All the exceptions except {@link IOException} are there for Object
	 * conversion</li>
	 * </ul>
	 * 
	 * @param asString
	 *            TRUE for String reading, FALSE for Object reading (which can
	 *            still be a String)
	 * 
	 * @return the deserialised answer (which can actually be NULL)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws NoSuchFieldException
	 *             if the serialised data contains information about a field
	 *             which does actually not exist in the class we know of
	 * @throws NoSuchMethodException
	 *             if a class described in the serialised data cannot be created
	 *             because it is not compatible with this code
	 * @throws ClassNotFoundException
	 *             if a class described in the serialised data cannot be found
	 * @throws java.lang.NullPointerException
	 *             for Objects only: if the counter part has no data to send
	 */
	@SuppressWarnings("resource")
	private Object rec(boolean asString) throws IOException,
			NoSuchFieldException, NoSuchMethodException,
			ClassNotFoundException, java.lang.NullPointerException {

		synchronized (lock) {
			if (server || contentToSend) {
				if (contentToSend) {
					out.flush();
					contentToSend = false;
				}

				if (in.next() && !in.eof()) {
					InputStream read = new ReplaceInputStream(in.open(),
							STREAM_CODED, STREAM_RAW);
					try {
						if (crypt != null) {
							read = crypt.decrypt64(read);
						}

						if (asString) {
							return IOUtils.readSmallStream(read);
						}

						return new Importer().read(read).getValue();
					} finally {
						read.close();
					}
				}

				if (!asString) {
					throw new NullPointerException();
				}
			}

			return null;
		}
	}
}