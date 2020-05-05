package be.nikiroo.utils.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple {@link OutputStream} that is buffered with a bytes array.
 * <p>
 * It is mostly intended to be used as a base class to create new
 * {@link OutputStream}s with special operation modes, and to give some default
 * methods.
 * 
 * @author niki
 */
public class BufferedOutputStream extends OutputStream {
	/** The current position in the buffer. */
	protected int start;
	/** The index of the last usable position of the buffer. */
	protected int stop;
	/** The buffer itself. */
	protected byte[] buffer;
	/** An End-Of-File (or buffer, here) marker. */
	protected boolean eof;
	/** The actual under-laying stream. */
	protected OutputStream out;
	/** The number of bytes written to the under-laying stream. */
	protected long bytesWritten;
	/**
	 * Can bypass the flush process for big writes (will directly write to the
	 * under-laying buffer if the array to write is &gt; the internal buffer
	 * size).
	 * <p>
	 * By default, this is true.
	 */
	protected boolean bypassFlush = true;

	private boolean closed;
	private int openCounter;
	private byte[] b1;

	/**
	 * Create a new {@link BufferedInputStream} that wraps the given
	 * {@link InputStream}.
	 * 
	 * @param out
	 *            the {@link OutputStream} to wrap
	 */
	public BufferedOutputStream(OutputStream out) {
		this.out = out;

		this.buffer = new byte[4096];
		this.b1 = new byte[1];
		this.start = 0;
		this.stop = 0;
	}

	@Override
	public void write(int b) throws IOException {
		b1[0] = (byte) b;
		write(b1, 0, 1);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] source, int sourceOffset, int sourceLength)
			throws IOException {

		checkClose();

		if (source == null) {
			throw new NullPointerException();
		} else if ((sourceOffset < 0) || (sourceOffset > source.length)
				|| (sourceLength < 0)
				|| ((sourceOffset + sourceLength) > source.length)
				|| ((sourceOffset + sourceLength) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (sourceLength == 0) {
			return;
		}

		if (bypassFlush && sourceLength >= buffer.length) {
			/*
			 * If the request length exceeds the size of the output buffer,
			 * flush the output buffer and then write the data directly. In this
			 * way buffered streams will cascade harmlessly.
			 */
			flush(false);
			out.write(source, sourceOffset, sourceLength);
			bytesWritten += (sourceLength - sourceOffset);
			return;
		}

		int done = 0;
		while (done < sourceLength) {
			if (available() <= 0) {
				flush(false);
			}

			int now = Math.min(sourceLength - done, available());
			System.arraycopy(source, sourceOffset + done, buffer, stop, now);
			stop += now;
			done += now;
		}
	}

	/**
	 * The available space in the buffer.
	 * 
	 * @return the space in bytes
	 */
	private int available() {
		if (closed) {
			return 0;
		}

		return Math.max(0, buffer.length - stop - 1);
	}

	/**
	 * The number of bytes written to the under-laying {@link OutputStream}.
	 * 
	 * @return the number of bytes
	 */
	public long getBytesWritten() {
		return bytesWritten;
	}

	/**
	 * Return this very same {@link BufferedInputStream}, but keep a counter of
	 * how many streams were open this way. When calling
	 * {@link BufferedInputStream#close()}, decrease this counter if it is not
	 * already zero instead of actually closing the stream.
	 * <p>
	 * You are now responsible for it &mdash; you <b>must</b> close it.
	 * <p>
	 * This method allows you to use a wrapping stream around this one and still
	 * close the wrapping stream.
	 * 
	 * @return the same stream, but you are now responsible for closing it
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the stream is closed
	 */
	public synchronized OutputStream open() throws IOException {
		checkClose();
		openCounter++;
		return this;
	}

	/**
	 * Check that the stream was not closed, and throw an {@link IOException} if
	 * it was.
	 * 
	 * @throws IOException
	 *             if it was closed
	 */
	protected void checkClose() throws IOException {
		if (closed) {
			throw new IOException(
					"This BufferedInputStream was closed, you cannot use it anymore.");
		}
	}

	@Override
	public void flush() throws IOException {
		flush(true);
	}

	/**
	 * Flush the {@link BufferedOutputStream}, write the current buffered data
	 * to (and optionally also flush) the under-laying stream.
	 * <p>
	 * If {@link BufferedOutputStream#bypassFlush} is false, all writes to the
	 * under-laying stream are done in this method.
	 * <p>
	 * This can be used if you want to write some data in the under-laying
	 * stream yourself (in that case, flush this {@link BufferedOutputStream}
	 * with or without flushing the under-laying stream, then you can write to
	 * the under-laying stream).
	 * 
	 * @param includingSubStream
	 *            also flush the under-laying stream
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void flush(boolean includingSubStream) throws IOException {
		if (stop > start) {
			out.write(buffer, start, stop - start);
			bytesWritten += (stop - start);
		}
		start = 0;
		stop = 0;

		if (includingSubStream) {
			out.flush();
		}
	}

	/**
	 * Closes this stream and releases any system resources associated with the
	 * stream.
	 * <p>
	 * Including the under-laying {@link InputStream}.
	 * <p>
	 * <b>Note:</b> if you called the {@link BufferedInputStream#open()} method
	 * prior to this one, it will just decrease the internal count of how many
	 * open streams it held and do nothing else. The stream will actually be
	 * closed when you have called {@link BufferedInputStream#close()} once more
	 * than {@link BufferedInputStream#open()}.
	 * 
	 * @exception IOException
	 *                in case of I/O error
	 */
	@Override
	public synchronized void close() throws IOException {
		close(true);
	}

	/**
	 * Closes this stream and releases any system resources associated with the
	 * stream.
	 * <p>
	 * Including the under-laying {@link InputStream} if
	 * <tt>incudingSubStream</tt> is true.
	 * <p>
	 * You can call this method multiple times, it will not cause an
	 * {@link IOException} for subsequent calls.
	 * <p>
	 * <b>Note:</b> if you called the {@link BufferedInputStream#open()} method
	 * prior to this one, it will just decrease the internal count of how many
	 * open streams it held and do nothing else. The stream will actually be
	 * closed when you have called {@link BufferedInputStream#close()} once more
	 * than {@link BufferedInputStream#open()}.
	 * 
	 * @param includingSubStream
	 *            also close the under-laying stream
	 * 
	 * @exception IOException
	 *                in case of I/O error
	 */
	public synchronized void close(boolean includingSubStream)
			throws IOException {
		if (!closed) {
			if (openCounter > 0) {
				openCounter--;
			} else {
				closed = true;
				flush(includingSubStream);
				if (includingSubStream && out != null) {
					out.close();
				}
			}
		}
	}
}
