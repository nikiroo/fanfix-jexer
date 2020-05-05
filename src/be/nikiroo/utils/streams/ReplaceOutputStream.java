package be.nikiroo.utils.streams;

import java.io.IOException;
import java.io.OutputStream;

import be.nikiroo.utils.StringUtils;

/**
 * This {@link OutputStream} will change some of its content by replacing it
 * with something else.
 * 
 * @author niki
 */
public class ReplaceOutputStream extends BufferedOutputStream {
	private byte[][] froms;
	private byte[][] tos;

	/**
	 * Create a {@link ReplaceOutputStream} that will replace <tt>from</tt> with
	 * <tt>to</tt>.
	 * 
	 * @param out
	 *            the under-laying {@link OutputStream}
	 * @param from
	 *            the {@link String} to replace
	 * @param to
	 *            the {@link String} to replace with
	 */
	public ReplaceOutputStream(OutputStream out, String from, String to) {
		this(out, StringUtils.getBytes(from), StringUtils.getBytes(to));
	}

	/**
	 * Create a {@link ReplaceOutputStream} that will replace <tt>from</tt> with
	 * <tt>to</tt>.
	 * 
	 * @param out
	 *            the under-laying {@link OutputStream}
	 * @param from
	 *            the value to replace
	 * @param to
	 *            the value to replace with
	 */
	public ReplaceOutputStream(OutputStream out, byte[] from, byte[] to) {
		this(out, new byte[][] { from }, new byte[][] { to });
	}

	/**
	 * Create a {@link ReplaceOutputStream} that will replace all <tt>froms</tt>
	 * with <tt>tos</tt>.
	 * <p>
	 * Note that they will be replaced in order, and that for each <tt>from</tt>
	 * a <tt>to</tt> must correspond.
	 * 
	 * @param out
	 *            the under-laying {@link OutputStream}
	 * @param froms
	 *            the values to replace
	 * @param tos
	 *            the values to replace with
	 */
	public ReplaceOutputStream(OutputStream out, String[] froms, String[] tos) {
		this(out, StreamUtils.getBytes(froms), StreamUtils.getBytes(tos));
	}

	/**
	 * Create a {@link ReplaceOutputStream} that will replace all <tt>froms</tt>
	 * with <tt>tos</tt>.
	 * <p>
	 * Note that they will be replaced in order, and that for each <tt>from</tt>
	 * a <tt>to</tt> must correspond.
	 * 
	 * @param out
	 *            the under-laying {@link OutputStream}
	 * @param froms
	 *            the values to replace
	 * @param tos
	 *            the values to replace with
	 */
	public ReplaceOutputStream(OutputStream out, byte[][] froms, byte[][] tos) {
		super(out);
		bypassFlush = false;

		if (froms.length != tos.length) {
			throw new IllegalArgumentException(
					"For replacing, each FROM must have a corresponding TO");
		}

		this.froms = froms;
		this.tos = tos;
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
	 * <p>
	 * <b>But be careful!</b> If a replacement could be done with the end o the
	 * currently buffered data and the start of the data to come, we obviously
	 * will not be able to do it.
	 * 
	 * @param includingSubStream
	 *            also flush the under-laying stream
	 * @throws IOException
	 *             in case of I/O error
	 */
	@Override
	public void flush(boolean includingSubStream) throws IOException {
		// Note: very simple, not efficient implementation; sorry.
		while (start < stop) {
			boolean replaced = false;
			for (int i = 0; i < froms.length; i++) {
				if (froms[i] != null
						&& froms[i].length > 0
						&& StreamUtils
								.startsWith(froms[i], buffer, start, stop)) {
					if (tos[i] != null && tos[i].length > 0) {
						out.write(tos[i]);
						bytesWritten += tos[i].length;
					}

					start += froms[i].length;
					replaced = true;
					break;
				}
			}

			if (!replaced) {
				out.write(buffer[start++]);
				bytesWritten++;
			}
		}

		start = 0;
		stop = 0;

		if (includingSubStream) {
			out.flush();
		}
	}
}
