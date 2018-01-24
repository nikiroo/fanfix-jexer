package be.nikiroo.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class offer some utilities based around Streams.
 * 
 * @author niki
 */
public class IOUtils {
	/**
	 * Write the data to the given {@link File}.
	 * 
	 * @param in
	 *            the data source
	 * @param target
	 *            the target {@link File}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void write(InputStream in, File target) throws IOException {
		OutputStream out = new FileOutputStream(target);
		try {
			write(in, out);
		} finally {
			out.close();
		}
	}

	/**
	 * Write the data to the given {@link OutputStream}.
	 * 
	 * @param in
	 *            the data source
	 * @param out
	 *            the target {@link OutputStream}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void write(InputStream in, OutputStream out)
			throws IOException {
		byte buffer[] = new byte[4069];
		for (int len = 0; (len = in.read(buffer)) > 0;) {
			out.write(buffer, 0, len);
		}
	}

	/**
	 * Recursively Add a {@link File} (which can thus be a directory, too) to a
	 * {@link ZipOutputStream}.
	 * 
	 * @param zip
	 *            the stream
	 * @param base
	 *            the path to prepend to the ZIP info before the actual
	 *            {@link File} path
	 * @param target
	 *            the source {@link File} (which can be a directory)
	 * @param targetIsRoot
	 *            FALSE if we need to add a {@link ZipEntry} for base/target,
	 *            TRUE to add it at the root of the ZIP
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void zip(ZipOutputStream zip, String base, File target,
			boolean targetIsRoot) throws IOException {
		if (target.isDirectory()) {
			if (!targetIsRoot) {
				if (base == null || base.isEmpty()) {
					base = target.getName();
				} else {
					base += "/" + target.getName();
				}
				zip.putNextEntry(new ZipEntry(base + "/"));
			}

			File[] files = target.listFiles();
			if (files != null) {
				for (File file : files) {
					zip(zip, base, file, false);
				}
			}
		} else {
			if (base == null || base.isEmpty()) {
				base = target.getName();
			} else {
				base += "/" + target.getName();
			}
			zip.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(target);
			try {
				IOUtils.write(in, zip);
			} finally {
				in.close();
			}
		}
	}

	/**
	 * Zip the given source into dest.
	 * 
	 * @param src
	 *            the source {@link File} (which can be a directory)
	 * @param dest
	 *            the destination <tt>.zip</tt> file
	 * @param srcIsRoot
	 *            FALSE if we need to add a {@link ZipEntry} for src, TRUE to
	 *            add it at the root of the ZIP
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void zip(File src, File dest, boolean srcIsRoot)
			throws IOException {
		OutputStream out = new FileOutputStream(dest);
		try {
			ZipOutputStream zip = new ZipOutputStream(out);
			try {
				IOUtils.zip(zip, "", src, srcIsRoot);
			} finally {
				zip.close();
			}
		} finally {
			out.close();
		}
	}

	/**
	 * Write the {@link String} content to {@link File}.
	 * 
	 * @param dir
	 *            the directory where to write the {@link File}
	 * @param filename
	 *            the {@link File} name
	 * @param content
	 *            the content
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void writeSmallFile(File dir, String filename, String content)
			throws IOException {
		if (!dir.exists()) {
			dir.mkdirs();
		}

		FileWriter writerVersion = new FileWriter(new File(dir, filename));
		try {
			writerVersion.write(content);
		} finally {
			writerVersion.close();
		}
	}

	/**
	 * Read the whole {@link File} content into a {@link String}.
	 * 
	 * @param file
	 *            the {@link File}
	 * 
	 * @return the content
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static String readSmallFile(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		try {
			return readSmallStream(stream);
		} finally {
			stream.close();
		}
	}

	/**
	 * Read the whole {@link InputStream} content into a {@link String}.
	 * 
	 * @param stream
	 *            the {@link InputStream}
	 * 
	 * @return the content
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static String readSmallStream(InputStream stream) throws IOException {
		// do NOT close the reader, or the related stream will be closed, too
		// reader.close();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));

		StringBuilder builder = new StringBuilder();
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			builder.append(line);
			builder.append("\n");
		}

		return builder.toString();
	}

	/**
	 * Recursively delete the given {@link File}, which may of course also be a
	 * directory.
	 * <p>
	 * Will either silently continue or throw an exception in case of error,
	 * depending upon the parameters.
	 * 
	 * @param target
	 *            the target to delete
	 * @param exception
	 *            TRUE to throw an {@link IOException} in case of error, FALSE
	 *            to silently continue
	 * 
	 * @return TRUE if all files were deleted, FALSE if an error occurred
	 * 
	 * @throws IOException
	 *             if an error occurred and the parameters allow an exception to
	 *             be thrown
	 */
	public static boolean deltree(File target, boolean exception)
			throws IOException {
		List<File> list = deltree(target, null);
		if (exception && !list.isEmpty()) {
			StringBuilder slist = new StringBuilder();
			for (File file : list) {
				slist.append("\n").append(file.getPath());
			}

			throw new IOException("Cannot delete all the files from: <" //
					+ target + ">:" + slist.toString());
		}

		return list.isEmpty();
	}

	/**
	 * Recursively delete the given {@link File}, which may of course also be a
	 * directory.
	 * <p>
	 * Will silently continue in case of error.
	 * 
	 * @param target
	 *            the target to delete
	 * 
	 * @return TRUE if all files were deleted, FALSE if an error occurred
	 */
	public static boolean deltree(File target) {
		return deltree(target, null).isEmpty();
	}

	/**
	 * Recursively delete the given {@link File}, which may of course also be a
	 * directory.
	 * <p>
	 * Will collect all {@link File} that cannot be deleted in the given
	 * accumulator.
	 * 
	 * @param target
	 *            the target to delete
	 * @param errorAcc
	 *            the accumulator to use for errors, or NULL to create a new one
	 * 
	 * @return the errors accumulator
	 */
	public static List<File> deltree(File target, List<File> errorAcc) {
		if (errorAcc == null) {
			errorAcc = new ArrayList<File>();
		}

		File[] files = target.listFiles();
		if (files != null) {
			for (File file : files) {
				errorAcc = deltree(file, errorAcc);
			}
		}

		if (!target.delete()) {
			errorAcc.add(target);
		}

		return errorAcc;
	}

	/**
	 * Open the given /-separated resource (from the binary root).
	 * 
	 * @param name
	 *            the resource name
	 * 
	 * @return the opened resource if found, NLL if not
	 */
	public static InputStream openResource(String name) {
		ClassLoader loader = IOUtils.class.getClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
		}

		return loader.getResourceAsStream(name);
	}

	/**
	 * Return a resetable {@link InputStream} from this stream, and reset it.
	 * 
	 * @param in
	 *            the input stream
	 * @return the resetable stream, which <b>may</b> be the same
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static InputStream forceResetableStream(InputStream in)
			throws IOException {
		MarkableFileInputStream tmpIn = null;
		File tmp = null;

		boolean resetable = in.markSupported();
		if (resetable) {
			try {
				in.reset();
			} catch (IOException e) {
				resetable = false;
			}
		}

		if (resetable) {
			return in;
		}

		tmp = File.createTempFile(".tmp-stream", ".tmp");
		try {
			write(in, tmp);
			tmpIn = new MarkableFileInputStream(new FileInputStream(tmp));
			return tmpIn;
		} finally {
			try {
				if (tmpIn != null) {
					tmpIn.close();
				}
			} finally {
				tmp.delete();
			}
		}
	}

	/**
	 * Convert the {@link InputStream} into a byte array.
	 * 
	 * @param in
	 *            the input stream
	 * 
	 * @return the array
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(in, out);

		byte[] array = out.toByteArray();
		out.close();

		return array;
	}
}
