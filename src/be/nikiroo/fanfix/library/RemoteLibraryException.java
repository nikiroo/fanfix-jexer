package be.nikiroo.fanfix.library;

import java.io.IOException;

/**
 * Exceptions sent from remote to local.
 * 
 * @author niki
 */
public class RemoteLibraryException extends IOException {
	private static final long serialVersionUID = 1L;

	private boolean wrapped;

	@SuppressWarnings("unused")
	private RemoteLibraryException() {
		// for serialization purposes
	}

	/**
	 * Wrap an {@link IOException} to allow it to pass across the network.
	 * 
	 * @param cause
	 *            the exception to wrap
	 * @param remote
	 *            this exception is used to send the contained
	 *            {@link IOException} to the other end of the network
	 */
	public RemoteLibraryException(IOException cause, boolean remote) {
		this(null, cause, remote);
	}

	/**
	 * Wrap an {@link IOException} to allow it to pass across the network.
	 * 
	 * @param message
	 *            the error message
	 * @param wrapped
	 *            this exception is used to send the contained
	 *            {@link IOException} to the other end of the network
	 */
	public RemoteLibraryException(String message, boolean wrapped) {
		this(message, null, wrapped);
	}

	/**
	 * Wrap an {@link IOException} to allow it to pass across the network.
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            the exception to wrap
	 * @param wrapped
	 *            this exception is used to send the contained
	 *            {@link IOException} to the other end of the network
	 */
	public RemoteLibraryException(String message, IOException cause,
			boolean wrapped) {
		super(message, cause);
		this.wrapped = wrapped;
	}

	/**
	 * Return the actual exception we should return to the client code. It can
	 * be:
	 * <ul>
	 * <li>the <tt>cause</tt> if {@link RemoteLibraryException#isWrapped()} is
	 * TRUE</li>
	 * <li><tt>this</tt> if {@link RemoteLibraryException#isWrapped()} is FALSE
	 * (</li>
	 * <li><tt>this</tt> if the <tt>cause</tt> is NULL (so we never return NULL)
	 * </li>
	 * </ul>
	 * It is never NULL.
	 * 
	 * @return the unwrapped exception or <tt>this</tt>, never NULL
	 */
	public synchronized IOException unwrapException() {
		Throwable ex = super.getCause();
		if (!isWrapped() || !(ex instanceof IOException)) {
			ex = this;
		}

		return (IOException) ex;
	}

	/**
	 * This exception is used to send the contained {@link IOException} to the
	 * other end of the network.
	 * <p>
	 * In other words, do not use <tt>this</tt> exception in client code when it
	 * has reached the other end of the network, but use its cause instead (see
	 * {@link RemoteLibraryException#unwrapException()}).
	 * 
	 * @return TRUE if it is
	 */
	public boolean isWrapped() {
		return wrapped;
	}
}
