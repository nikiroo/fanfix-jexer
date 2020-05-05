package be.nikiroo.utils.streams;

import be.nikiroo.utils.StringUtils;

/**
 * Some non-public utilities used in the stream classes.
 * 
 * @author niki
 */
class StreamUtils {
	/**
	 * Check if the buffer starts with the given search term (given as an array,
	 * a start position and an end position).
	 * <p>
	 * Note: the parameter <tt>stop</tt> is the <b>index</b> of the last
	 * position, <b>not</b> the length.
	 * <p>
	 * Note: the search term size <b>must</b> be smaller or equal the internal
	 * buffer size.
	 * 
	 * @param search
	 *            the term to search for
	 * @param buffer
	 *            the buffer to look into
	 * @param start
	 *            the offset at which to start the search
	 * @param stop
	 *            the maximum index of the data to check (this is <b>not</b> a
	 *            length, but an index)
	 * 
	 * @return TRUE if the search content is present at the given location and
	 *         does not exceed the <tt>len</tt> index
	 */
	static public boolean startsWith(byte[] search, byte[] buffer, int start,
			int stop) {

		// Check if there even is enough space for it
		if (search.length > (stop - start)) {
			return false;
		}

		boolean same = true;
		for (int i = 0; i < search.length; i++) {
			if (search[i] != buffer[start + i]) {
				same = false;
				break;
			}
		}

		return same;
	}

	/**
	 * Return the bytes array representation of the given {@link String} in
	 * UTF-8.
	 * 
	 * @param strs
	 *            the {@link String}s to transform into bytes
	 * @return the content in bytes
	 */
	static public byte[][] getBytes(String[] strs) {
		byte[][] bytes = new byte[strs.length][];
		for (int i = 0; i < strs.length; i++) {
			bytes[i] = StringUtils.getBytes(strs[i]);
		}

		return bytes;
	}
}
