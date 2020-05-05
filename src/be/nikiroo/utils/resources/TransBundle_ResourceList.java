package be.nikiroo.utils.resources;

// code copied from from:
// 		http://forums.devx.com/showthread.php?t=153784,
// via:
// 		http://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * list resources available from the classpath @ *
 */
class TransBundle_ResourceList {

	/**
	 * for all elements of java.class.path get a Collection of resources Pattern
	 * pattern = Pattern.compile(".*"); gets all resources
	 * 
	 * @param pattern
	 *            the pattern to match
	 * @return the resources in the order they are found
	 */
	public static Collection<String> getResources(final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		final String classPath = System.getProperty("java.class.path", ".");
		final String[] classPathElements = classPath.split(System
				.getProperty("path.separator"));
		for (final String element : classPathElements) {
			retval.addAll(getResources(element, pattern));
		}

		return retval;
	}

	private static Collection<String> getResources(final String element,
			final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		final File file = new File(element);
		if (file.isDirectory()) {
			retval.addAll(getResourcesFromDirectory(file, pattern));
		} else {
			retval.addAll(getResourcesFromJarFile(file, pattern));
		}

		return retval;
	}

	private static Collection<String> getResourcesFromJarFile(final File file,
			final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (final ZipException e) {
			throw new Error(e);
		} catch (final IOException e) {
			throw new Error(e);
		}
		final Enumeration<? extends ZipEntry> e = zf.entries();
		while (e.hasMoreElements()) {
			final ZipEntry ze = e.nextElement();
			final String fileName = ze.getName();
			final boolean accept = pattern.matcher(fileName).matches();
			if (accept) {
				retval.add(fileName);
			}
		}
		try {
			zf.close();
		} catch (final IOException e1) {
			throw new Error(e1);
		}

		return retval;
	}

	private static Collection<String> getResourcesFromDirectory(
			final File directory, final Pattern pattern) {
		List<String> acc = new ArrayList<String>();
		List<File> dirs = new ArrayList<File>();
		getResourcesFromDirectory(acc, dirs, directory, pattern);

		List<String> rep = new ArrayList<String>();
		for (String value : acc) {
			if (pattern.matcher(value).matches()) {
				rep.add(value);
			}
		}

		return rep;
	}

	private static void getResourcesFromDirectory(List<String> acc,
			List<File> dirs, final File directory, final Pattern pattern) {
		final File[] fileList = directory.listFiles();
		if (fileList != null) {
			for (final File file : fileList) {
				if (!dirs.contains(file)) {
					try {
						String key = file.getCanonicalPath();
						if (!acc.contains(key)) {
							if (file.isDirectory()) {
								dirs.add(file);
								getResourcesFromDirectory(acc, dirs, file,
										pattern);
							} else {
								acc.add(key);
							}
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}
}
