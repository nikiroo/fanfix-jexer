package be.nikiroo.utils.streams;

import java.io.InputStream;

/**
 * Divide an {@link InputStream} into sub-streams.
 * 
 * @author niki
 */
public class NextableInputStreamStep {
	private int stopAt;
	private int last = -1;
	private int resumeLen;
	private int resumeSkip;
	private boolean resumeEof;

	/**
	 * Create a new divider that will separate the sub-streams each time it sees
	 * this byte.
	 * <p>
	 * Note that the byte will be bypassed by the {@link InputStream} as far as
	 * the consumers will be aware.
	 * 
	 * @param byt
	 *            the byte at which to separate two sub-streams
	 */
	public NextableInputStreamStep(int byt) {
		stopAt = byt;
	}

	/**
	 * Check if we need to stop the {@link InputStream} reading at some point in
	 * the current buffer.
	 * <p>
	 * If we do, return the index at which to stop; if not, return -1.
	 * <p>
	 * This method will <b>not</b> return the same index a second time (unless
	 * we cleared the buffer).
	 * 
	 * @param buffer
	 *            the buffer to check
	 * @param pos
	 *            the current position of what was read in the buffer
	 * @param len
	 *            the maximum index to use in the buffer (anything above that is
	 *            not to be used)
	 * @param eof
	 *            the current state of the under-laying stream
	 * 
	 * @return the index at which to stop, or -1
	 */
	public int stop(byte[] buffer, int pos, int len, boolean eof) {
		for (int i = pos; i < len; i++) {
			if (buffer[i] == stopAt) {
				if (i > this.last) {
					// we skip the sep
					this.resumeSkip = 1;

					this.resumeLen = len;
					this.resumeEof = eof;
					this.last = i;
					return i;
				}
			}
		}

		return -1;
	}

	/**
	 * Get the maximum index to use in the buffer used in
	 * {@link NextableInputStreamStep#stop(byte[], int, int, boolean)} at resume
	 * time.
	 * 
	 * @return the index
	 */
	public int getResumeLen() {
		return resumeLen;
	}

	/**
	 * Get the number of bytes to skip at resume time.
	 * 
	 * @return the number of bytes to skip
	 */
	public int getResumeSkip() {
		return resumeSkip;
	}

	/**
	 * Get the under-laying stream state at resume time.
	 * 
	 * @return the EOF state
	 */
	public boolean getResumeEof() {
		return resumeEof;
	}

	/**
	 * Clear the information we may have kept about the current buffer
	 * <p>
	 * You should call this method each time you change the content of the
	 * buffer used in
	 * {@link NextableInputStreamStep#stop(byte[], int, int, boolean)}.
	 */
	public void clearBuffer() {
		this.last = -1;
		this.resumeSkip = 0;
		this.resumeLen = 0;
		this.resumeEof = false;
	}
}
