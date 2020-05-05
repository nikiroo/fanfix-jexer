package be.nikiroo.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import be.nikiroo.utils.streams.MarkableFileInputStream;

/**
 * This class offer some utilities based around Streams and Files.
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
	 * @return the number of bytes written
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static long write(InputStream in, File target) throws IOException {
		OutputStream out = new FileOutputStream(target);
		try {
			return write(in, out);
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
	 * @return the number of bytes written
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static long write(InputStream in, OutputStream out)
			throws IOException {
		long written = 0;
		byte buffer[] = new byte[4096];
		int len = in.read(buffer);
		while (len > -1) {
			out.write(buffer, 0, len);
			written += len;
			len = in.read(buffer);
		}

		return written;
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
	 * Unzip the given ZIP file into the target directory.
	 * 
	 * @param zipFile
	 *            the ZIP file
	 * @param targetDirectory
	 *            the target directory
	 * 
	 * @return the number of extracted files (not directories)
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static long unzip(File zipFile, File targetDirectory)
			throws IOException {
		long count = 0;

		if (targetDirectory.exists() && targetDirectory.isFile()) {
			throw new IOException("Cannot unzip " + zipFile + " into "
					+ targetDirectory + ": it is not a directory");
		}

		targetDirectory.mkdir();
		if (!targetDirectory.exists()) {
			throw new IOException("Cannot create target directory "
					+ targetDirectory);
		}

		FileInputStream in = new FileInputStream(zipFile);
		try {
			ZipInputStream zipStream = new ZipInputStream(in);
			try {
				for (ZipEntry entry = zipStream.getNextEntry(); entry != null; entry = zipStream
						.getNextEntry()) {
					File file = new File(targetDirectory, entry.getName());
					if (entry.isDirectory()) {
						file.mkdirs();
					} else {
						IOUtils.write(zipStream, file);
						count++;
					}
				}
			} finally {
				zipStream.close();
			}
		} finally {
			in.close();
		}

		return count;
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

		writeSmallFile(new File(dir, filename), content);
	}

	/**
	 * Write the {@link String} content to {@link File}.
	 * 
	 * @param file
	 *            the {@link File} to write
	 * @param content
	 *            the content
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void writeSmallFile(File file, String content)
			throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			out.write(StringUtils.getBytes(content));
		} finally {
			out.close();
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
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			write(stream, out);
			return out.toString("UTF-8");
		} finally {
			out.close();
		}
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
	 * Open the resource next to the given {@link Class}.
	 * 
	 * @param location
	 *            the location where to look for the resource
	 * @param name
	 *            the resource name (only the filename, no path)
	 * 
	 * @return the opened resource if found, NULL if not
	 */
	public static InputStream openResource(
			@SuppressWarnings("rawtypes") Class location, String name) {
		String loc = location.getName().replace(".", "/")
				.replaceAll("/[^/]*$", "/");
		return openResource(loc + name);
	}

	/**
	 * Open the given /-separated resource (from the binary root).
	 * 
	 * @param name
	 *            the resource name (the full path, with "/" as separator)
	 * 
	 * @return the opened resource if found, NULL if not
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

		final File tmp = File.createTempFile(".tmp-stream.", ".tmp");
		try {
			write(in, tmp);
			in.close();

			return new MarkableFileInputStream(tmp) {
				@Override
				public void close() throws IOException {
					try {
						super.close();
					} finally {
						tmp.delete();
					}
				}
			};
		} catch (IOException e) {
			tmp.delete();
			throw e;
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
		try {
			write(in, out);
			return out.toByteArray();
		} finally {
			out.close();
		}
	}

	/**
	 * Convert the {@link File} into a byte array.
	 * 
	 * @param file
	 *            the input {@link File}
	 * 
	 * @return the array
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static byte[] toByteArray(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return toByteArray(fis);
		} finally {
			fis.close();
		}
	}
}
