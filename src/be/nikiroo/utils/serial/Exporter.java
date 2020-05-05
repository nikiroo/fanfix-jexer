package be.nikiroo.utils.serial;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple class to serialise objects to {@link String}.
 * <p>
 * This class does not support inner classes (it does support nested classes,
 * though).
 * 
 * @author niki
 */
public class Exporter {
	private Map<Integer, Object> map;
	private OutputStream out;

	/**
	 * Create a new {@link Exporter}.
	 * 
	 * @param out
	 *            export the data to this stream
	 */
	public Exporter(OutputStream out) {
		if (out == null) {
			throw new NullPointerException(
					"Cannot create an be.nikiroo.utils.serials.Exporter that will export to NULL");
		}

		this.out = out;
		map = new HashMap<Integer, Object>();
	}

	/**
	 * Serialise the given object and add it to the list.
	 * <p>
	 * <b>Important: </b>If the operation fails (with a
	 * {@link NotSerializableException}), the {@link Exporter} will be corrupted
	 * (will contain bad, most probably not importable data).
	 * 
	 * @param o
	 *            the object to serialise
	 * @return this (for easier appending of multiple values)
	 * 
	 * @throws NotSerializableException
	 *             if the object cannot be serialised (in this case, the
	 *             {@link Exporter} can contain bad, most probably not
	 *             importable data)
	 * @throws IOException
	 *             in case of I/O error
	 */
	public Exporter append(Object o) throws NotSerializableException,
			IOException {
		SerialUtils.append(out, o, map);
		return this;
	}
}