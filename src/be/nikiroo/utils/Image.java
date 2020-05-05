package be.nikiroo.utils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import be.nikiroo.utils.streams.Base64InputStream;
import be.nikiroo.utils.streams.MarkableFileInputStream;

/**
 * This class represents an image data.
 * 
 * @author niki
 */
public class Image implements Closeable, Serializable {
	static private final long serialVersionUID = 1L;

	static private File tempRoot;
	static private TempFiles tmpRepository;
	static private long count = 0;
	static private Object lock = new Object();

	private Object instanceLock = new Object();
	private File data;
	private long size;

	/**
	 * Do not use -- for serialisation purposes only.
	 */
	@SuppressWarnings("unused")
	private Image() {
	}

	/**
	 * Create a new {@link Image} with the given data.
	 * 
	 * @param data
	 *            the data
	 */
	public Image(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		try {
			this.data = getTemporaryFile();
			size = IOUtils.write(in, this.data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Create an image from Base64 encoded data.
	 * 
	 * <p>
	 * Please use {@link Image#Image(InputStream)} when possible instead, with a
	 * {@link Base64InputStream}; it can be much more efficient.
	 * 
	 * @param base64EncodedData
	 *            the Base64 encoded data as a String
	 * 
	 * @throws IOException
	 *             in case of I/O error or badly formated Base64
	 */
	public Image(String base64EncodedData) throws IOException {
		this(new Base64InputStream(new ByteArrayInputStream(
				StringUtils.getBytes(base64EncodedData)), false));
	}

	/**
	 * Create a new {@link Image} from a stream.
	 * 
	 * @param in
	 *            the stream
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public Image(InputStream in) throws IOException {
		data = getTemporaryFile();
		size = IOUtils.write(in, data);
	}

	/**
	 * The size of the enclosed image in bytes.
	 * 
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Generate an {@link InputStream} that you can {@link InputStream#reset()}
	 * for this {@link Image}.
	 * <p>
	 * This {@link InputStream} will (always) be a new one, and <b>you</b> are
	 * responsible for it.
	 * <p>
	 * Note: take care that the {@link InputStream} <b>must not</b> live past
	 * the {@link Image} life time!
	 * 
	 * @return the stream
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public InputStream newInputStream() throws IOException {
		return new MarkableFileInputStream(data);
	}

	/**
	 * <b>Read</b> the actual image data, as a byte array.
	 * 
	 * @deprecated if possible, prefer the {@link Image#newInputStream()}
	 *             method, as it can be more efficient
	 * 
	 * @return the image data
	 */
	@Deprecated
	public byte[] getData() {
		try {
			InputStream in = newInputStream();
			try {
				return IOUtils.toByteArray(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Convert the given {@link Image} object into a Base64 representation of
	 * the same {@link Image} object.
	 * 
	 * @deprecated Please use {@link Image#newInputStream()} instead, it is more
	 *             efficient
	 * 
	 * @return the Base64 representation
	 */
	@Deprecated
	public String toBase64() {
		try {
			Base64InputStream stream = new Base64InputStream(newInputStream(),
					true);
			try {
				return IOUtils.readSmallStream(stream);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Closing the {@link Image} will delete the associated temporary file on
	 * disk.
	 * <p>
	 * Note that even if you don't, the program will still <b>try</b> to delete
	 * all the temporary files at JVM termination.
	 */
	@Override
	public void close() throws IOException {
		synchronized (instanceLock) {
			if (size >= 0) {
				size = -1;
				data.delete();
				data = null;

				synchronized (lock) {
					count--;
					if (count <= 0) {
						count = 0;
						tmpRepository.close();
						tmpRepository = null;
					}
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	/**
	 * Return a newly created temporary file to work on.
	 * 
	 * @return the file
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	private File getTemporaryFile() throws IOException {
		synchronized (lock) {
			if (tmpRepository == null) {
				tmpRepository = new TempFiles(tempRoot, "images");
				count = 0;
			}

			count++;

			return tmpRepository.createTempFile("image");
		}
	}

	/**
	 * Write this {@link Image} for serialization purposes; that is, write the
	 * content of the backing temporary file.
	 * 
	 * @param out
	 *            the {@link OutputStream} to write to
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		InputStream in = newInputStream();
		try {
			IOUtils.write(in, out);
		} finally {
			in.close();
		}
	}

	/**
	 * Read an {@link Image} written by
	 * {@link Image#writeObject(java.io.ObjectOutputStream)}; that is, create a
	 * new temporary file with the saved content.
	 * 
	 * @param in
	 *            the {@link InputStream} to read from
	 * @throws IOException
	 *             in case of I/O error
	 * @throws ClassNotFoundException
	 *             will not be thrown by this method
	 */
	@SuppressWarnings("unused")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		data = getTemporaryFile();
		IOUtils.write(in, data);
	}

	/**
	 * Change the temporary root directory used by the program.
	 * <p>
	 * Caution: the directory will be <b>owned</b> by the system, all its files
	 * now belong to us (and will most probably be deleted).
	 * <p>
	 * Note: it may take some time until the new temporary root is used, we
	 * first need to make sure the previous one is not used anymore (i.e., we
	 * must reach a point where no unclosed {@link Image} remains in memory) to
	 * switch the temporary root.
	 * 
	 * @param root
	 *            the new temporary root, which will be <b>owned</b> by the
	 *            system
	 */
	public static void setTemporaryFilesRoot(File root) {
		tempRoot = root;
	}
}
