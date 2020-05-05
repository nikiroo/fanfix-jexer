package be.nikiroo.utils.streams;

import java.io.IOException;
import java.io.InputStream;

import be.nikiroo.utils.StringUtils;

/**
 * This {@link InputStream} will change some of its content by replacing it with
 * something else.
 * 
 * @author niki
 */
public class ReplaceInputStream extends BufferedInputStream {
	/**
	 * The minimum size of the internal buffer (could be more if at least one of
	 * the 'FROM' bytes arrays is &gt; 2048 bytes &mdash; in that case the
	 * buffer will be twice the largest size of the 'FROM' bytes arrays).
	 * <p>
	 * This is a different buffer than the one from the inherited class.
	 */
	static private final int MIN_BUFFER_SIZE = 4096;

	private byte[][] froms;
	private byte[][] tos;
	private int maxFromSize;
	private int maxToSize;

	private byte[] source;
	private int spos;
	private int slen;

	/**
	 * Create a {@link ReplaceInputStream} that will replace <tt>from</tt> with
	 * <tt>to</tt>.
	 * 
	 * @param in
	 *            the under-laying {@link InputStream}
	 * @param from
	 *            the {@link String} to replace
	 * @param to
	 *            the {@link String} to replace with
	 */
	public ReplaceInputStream(InputStream in, String from, String to) {
		this(in, StringUtils.getBytes(from), StringUtils.getBytes(to));
	}

	/**
	 * Create a {@link ReplaceInputStream} that will replace <tt>from</tt> with
	 * <tt>to</tt>.
	 * 
	 * @param in
	 *            the under-laying {@link InputStream}
	 * @param from
	 *            the value to replace
	 * @param to
	 *            the value to replace with
	 */
	public ReplaceInputStream(InputStream in, byte[] from, byte[] to) {
		this(in, new byte[][] { from }, new byte[][] { to });
	}

	/**
	 * Create a {@link ReplaceInputStream} that will replace all <tt>froms</tt>
	 * with <tt>tos</tt>.
	 * <p>
	 * Note that they will be replaced in order, and that for each <tt>from</tt>
	 * a <tt>to</tt> must correspond.
	 * 
	 * @param in
	 *            the under-laying {@link InputStream}
	 * @param froms
	 *            the values to replace
	 * @param tos
	 *            the values to replace with
	 */
	public ReplaceInputStream(InputStream in, String[] froms, String[] tos) {
		this(in, StreamUtils.getBytes(froms), StreamUtils.getBytes(tos));
	}

	/**
	 * Create a {@link ReplaceInputStream} that will replace all <tt>froms</tt>
	 * with <tt>tos</tt>.
	 * <p>
	 * Note that they will be replaced in order, and that for each <tt>from</tt>
	 * a <tt>to</tt> must correspond.
	 * 
	 * @param in
	 *            the under-laying {@link InputStream}
	 * @param froms
	 *            the values to replace
	 * @param tos
	 *            the values to replace with
	 */
	public ReplaceInputStream(InputStream in, byte[][] froms, byte[][] tos) {
		super(in);

		if (froms.length != tos.length) {
			throw new IllegalArgumentException(
					"For replacing, each FROM must have a corresponding TO");
		}

		this.froms = froms;
		this.tos = tos;

		maxFromSize = 0;
		for (int i = 0; i < froms.length; i++) {
			maxFromSize = Math.max(maxFromSize, froms[i].length);
		}

		maxToSize = 0;
		for (int i = 0; i < tos.length; i++) {
			maxToSize = Math.max(maxToSize, tos[i].length);
		}

		// We need at least maxFromSize so we can iterate and replace
		source = new byte[Math.max(2 * maxFromSize, MIN_BUFFER_SIZE)];
		spos = 0;
		slen = 0;
	}

	@Override
	protected int read(InputStream in, byte[] buffer, int off, int len)
			throws IOException {
		if (len < maxToSize || source.length < maxToSize * 2) {
			throw new IOException(
					"An underlaying buffer is too small for these replace values");
		}

		// We need at least one byte of data to process
		if (available() < Math.max(maxFromSize, 1) && !eof) {
			spos = 0;
			slen = in.read(source);
		}

		// Note: very simple, not efficient implementation; sorry.
		int count = 0;
		while (spos < slen && count < len - maxToSize) {
			boolean replaced = false;
			for (int i = 0; i < froms.length; i++) {
				if (froms[i] != null && froms[i].length > 0
						&& StreamUtils.startsWith(froms[i], source, spos, slen)) {
					if (tos[i] != null && tos[i].length > 0) {
						System.arraycopy(tos[i], 0, buffer, off + spos,
								tos[i].length);
						count += tos[i].length;
					}

					spos += froms[i].length;
					replaced = true;
					break;
				}
			}

			if (!replaced) {
				buffer[off + count++] = source[spos++];
			}
		}

		return count;
	}
}
