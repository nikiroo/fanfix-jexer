package be.nikiroo.utils.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * This {@link InputStream} can be separated into sub-streams (you can process
 * it as a normal {@link InputStream} but, when it is spent, you can call
 * {@link NextableInputStream#next()} on it to unlock new data).
 * <p>
 * The separation in sub-streams is done via {@link NextableInputStreamStep}.
 * 
 * @author niki
 */
public class NextableInputStream extends BufferedInputStream {
	private NextableInputStreamStep step;
	private boolean started;
	private boolean stopped;

	/**
	 * Create a new {@link NextableInputStream} that wraps the given
	 * {@link InputStream}.
	 * 
	 * @param in
	 *            the {@link InputStream} to wrap
	 * @param step
	 *            how to separate it into sub-streams (can be NULL, but in that
	 *            case it will behave as a normal {@link InputStream})
	 */
	public NextableInputStream(InputStream in, NextableInputStreamStep step) {
		super(in);
		this.step = step;
	}

	/**
	 * Create a new {@link NextableInputStream} that wraps the given bytes array
	 * as a data source.
	 * 
	 * @param in
	 *            the array to wrap, cannot be NULL
	 * @param step
	 *            how to separate it into sub-streams (can be NULL, but in that
	 *            case it will behave as a normal {@link InputStream})
	 */
	public NextableInputStream(byte[] in, NextableInputStreamStep step) {
		this(in, step, 0, in.length);
	}

	/**
	 * Create a new {@link NextableInputStream} that wraps the given bytes array
	 * as a data source.
	 * 
	 * @param in
	 *            the array to wrap, cannot be NULL
	 * @param step
	 *            how to separate it into sub-streams (can be NULL, but in that
	 *            case it will behave as a normal {@link InputStream})
	 * @param offset
	 *            the offset to start the reading at
	 * @param length
	 *            the number of bytes to take into account in the array,
	 *            starting from the offset
	 * 
	 * @throws NullPointerException
	 *             if the array is NULL
	 * @throws IndexOutOfBoundsException
	 *             if the offset and length do not correspond to the given array
	 */
	public NextableInputStream(byte[] in, NextableInputStreamStep step,
			int offset, int length) {
		super(in, offset, length);
		this.step = step;
		checkBuffer(true);
	}

	/**
	 * Unblock the processing of the next sub-stream.
	 * <p>
	 * It can only be called when the "current" stream is spent (i.e., you must
	 * first process the stream until it is spent).
	 * <p>
	 * {@link IOException}s can happen when we have no data available in the
	 * buffer; in that case, we fetch more data to know if we can have a next
	 * sub-stream or not.
	 * <p>
	 * This is can be a blocking call when data need to be fetched.
	 * 
	 * @return TRUE if we unblocked the next sub-stream, FALSE if not (i.e.,
	 *         FALSE when there are no more sub-streams to fetch)
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the stream is closed
	 */
	public boolean next() throws IOException {
		return next(false);
	}

	/**
	 * Unblock the next sub-stream as would have done
	 * {@link NextableInputStream#next()}, but disable the sub-stream systems.
	 * <p>
	 * That is, the next stream, if any, will be the last one and will not be
	 * subject to the {@link NextableInputStreamStep}.
	 * <p>
	 * This is can be a blocking call when data need to be fetched.
	 * 
	 * @return TRUE if we unblocked the next sub-stream, FALSE if not
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the stream is closed
	 */
	public boolean nextAll() throws IOException {
		return next(true);
	}

	/**
	 * Check if this stream is totally spent (no more data to read or to
	 * process).
	 * <p>
	 * Note: when the stream is divided into sub-streams, each sub-stream will
	 * report its own eof when spent.
	 * 
	 * @return TRUE if it is
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	@Override
	public boolean eof() throws IOException {
		return super.eof();
	}

	/**
	 * Check if we still have some data in the buffer and, if not, fetch some.
	 * 
	 * @return TRUE if we fetched some data, FALSE if there are still some in
	 *         the buffer
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	@Override
	protected boolean preRead() throws IOException {
		if (!stopped) {
			boolean bufferChanged = super.preRead();
			checkBuffer(bufferChanged);
			return bufferChanged;
		}

		if (start >= stop) {
			eof = true;
		}

		return false;
	}

	@Override
	protected boolean hasMoreData() {
		return started && super.hasMoreData();
	}

	/**
	 * Check that the buffer didn't overshot to the next item, and fix
	 * {@link NextableInputStream#stop} if needed.
	 * <p>
	 * If {@link NextableInputStream#stop} is fixed,
	 * {@link NextableInputStream#eof} and {@link NextableInputStream#stopped}
	 * are set to TRUE.
	 * 
	 * @param newBuffer
	 *            we changed the buffer, we need to clear some information in
	 *            the {@link NextableInputStreamStep}
	 */
	private void checkBuffer(boolean newBuffer) {
		if (step != null && stop >= 0) {
			if (newBuffer) {
				step.clearBuffer();
			}

			int stopAt = step.stop(buffer, start, stop, eof);
			if (stopAt >= 0) {
				stop = stopAt;
				eof = true;
				stopped = true;
			}
		}
	}

	/**
	 * The implementation of {@link NextableInputStream#next()} and
	 * {@link NextableInputStream#nextAll()}.
	 * <p>
	 * This is can be a blocking call when data need to be fetched.
	 * 
	 * @param all
	 *            TRUE for {@link NextableInputStream#nextAll()}, FALSE for
	 *            {@link NextableInputStream#next()}
	 * 
	 * @return TRUE if we unblocked the next sub-stream, FALSE if not (i.e.,
	 *         FALSE when there are no more sub-streams to fetch)
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the stream is closed
	 */
	private boolean next(boolean all) throws IOException {
		checkClose();

		if (!started) {
			// First call before being allowed to read
			started = true;

			if (all) {
				step = null;
			}

			return true;
		}

		// If started, must be stopped and no more data to continue
		// i.e., sub-stream must be spent
		if (!stopped || hasMoreData()) {
			return false;
		}

		if (step != null) {
			stop = step.getResumeLen();
			start += step.getResumeSkip();
			eof = step.getResumeEof();
			stopped = false;

			if (all) {
				step = null;
			}

			checkBuffer(false);

			return true;
		}

		return false;

		// // consider that if EOF, there is no next
		// if (start >= stop) {
		// // Make sure, block if necessary
		// preRead();
		//
		// return hasMoreData();
		// }
		//
		// return true;
	}

	/**
	 * Display a DEBUG {@link String} representation of this object.
	 * <p>
	 * Do <b>not</b> use for release code.
	 */
	@Override
	public String toString() {
		String data = "";
		if (stop > 0) {
			try {
				data = new String(Arrays.copyOfRange(buffer, 0, stop), "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			if (data.length() > 200) {
				data = data.substring(0, 197) + "...";
			}
		}
		String rep = String.format(
				"Nextable %s: %d -> %d [eof: %s] [more data: %s]: %s",
				(stopped ? "stopped" : "running"), start, stop, "" + eof, ""
						+ hasMoreData(), data);

		return rep;
	}
}
