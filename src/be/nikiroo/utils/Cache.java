package be.nikiroo.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import be.nikiroo.utils.streams.MarkableFileInputStream;

/**
 * A generic cache system, with special support for {@link URL}s.
 * <p>
 * This cache also manages timeout information.
 * 
 * @author niki
 */
public class Cache {
	private File dir;
	private long tooOldChanging;
	private long tooOldStable;
	private TraceHandler tracer = new TraceHandler();

	/**
	 * Only for inheritance.
	 */
	protected Cache() {
	}

	/**
	 * Create a new {@link Cache} object.
	 * 
	 * @param dir
	 *            the directory to use as cache
	 * @param hoursChanging
	 *            the number of hours after which a cached file that is thought
	 *            to change ~often is considered too old (or -1 for
	 *            "never too old")
	 * @param hoursStable
	 *            the number of hours after which a cached file that is thought
	 *            to change rarely is considered too old (or -1 for
	 *            "never too old")
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public Cache(File dir, int hoursChanging, int hoursStable)
			throws IOException {
		this.dir = dir;
		this.tooOldChanging = 1000L * 60 * 60 * hoursChanging;
		this.tooOldStable = 1000L * 60 * 60 * hoursStable;

		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}

		if (dir == null || !dir.exists()) {
			throw new IOException("Cannot create the cache directory: "
					+ (dir == null ? "null" : dir.getAbsolutePath()));
		}
	}

	/**
	 * The traces handler for this {@link Cache}.
	 * 
	 * @return the traces handler
	 */
	public TraceHandler getTraceHandler() {
		return tracer;
	}

	/**
	 * The traces handler for this {@link Cache}.
	 * 
	 * @param tracer
	 *            the new traces handler
	 */
	public void setTraceHandler(TraceHandler tracer) {
		if (tracer == null) {
			tracer = new TraceHandler(false, false, false);
		}

		this.tracer = tracer;
	}

	/**
	 * Check the resource to see if it is in the cache.
	 * 
	 * @param uniqueID
	 *            the resource to check
	 * @param allowTooOld
	 *            allow files even if they are considered too old
	 * @param stable
	 *            a stable file (that dones't change too often) -- parameter
	 *            used to check if the file is too old to keep or not
	 * 
	 * @return TRUE if it is
	 * 
	 */
	public boolean check(String uniqueID, boolean allowTooOld, boolean stable) {
		return check(getCached(uniqueID), allowTooOld, stable);
	}

	/**
	 * Check the resource to see if it is in the cache.
	 * 
	 * @param url
	 *            the resource to check
	 * @param allowTooOld
	 *            allow files even if they are considered too old
	 * @param stable
	 *            a stable file (that dones't change too often) -- parameter
	 *            used to check if the file is too old to keep or not
	 * 
	 * @return TRUE if it is
	 * 
	 */
	public boolean check(URL url, boolean allowTooOld, boolean stable) {
		return check(getCached(url), allowTooOld, stable);
	}

	/**
	 * Check the resource to see if it is in the cache.
	 * 
	 * @param cached
	 *            the resource to check
	 * @param allowTooOld
	 *            allow files even if they are considered too old
	 * @param stable
	 *            a stable file (that dones't change too often) -- parameter
	 *            used to check if the file is too old to keep or not
	 * 
	 * @return TRUE if it is
	 * 
	 */
	private boolean check(File cached, boolean allowTooOld, boolean stable) {
		if (cached.exists() && cached.isFile()) {
			if (!allowTooOld && isOld(cached, stable)) {
				if (!cached.delete()) {
					tracer.error("Cannot delete temporary file: "
							+ cached.getAbsolutePath());
				}
			} else {
				return true;
			}
		}

		return false;
	}

	/**
	 * Clean the cache (delete the cached items).
	 * 
	 * @param onlyOld
	 *            only clean the files that are considered too old for a stable
	 *            resource
	 * 
	 * @return the number of cleaned items
	 */
	public int clean(boolean onlyOld) {
		long ms = System.currentTimeMillis();

		tracer.trace("Cleaning cache from old files...");

		int num = clean(onlyOld, dir, -1);

		tracer.trace(num + "cache items cleaned in "
				+ (System.currentTimeMillis() - ms) + " ms");

		return num;
	}

	/**
	 * Clean the cache (delete the cached items) in the given cache directory.
	 * 
	 * @param onlyOld
	 *            only clean the files that are considered too old for stable
	 *            resources
	 * @param cacheDir
	 *            the cache directory to clean
	 * @param limit
	 *            stop after limit files deleted, or -1 for unlimited
	 * 
	 * @return the number of cleaned items
	 */
	private int clean(boolean onlyOld, File cacheDir, int limit) {
		int num = 0;
		File[] files = cacheDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (limit >= 0 && num >= limit) {
					return num;
				}

				if (file.isDirectory()) {
					num += clean(onlyOld, file, limit);
					file.delete(); // only if empty
				} else {
					if (!onlyOld || isOld(file, true)) {
						if (file.delete()) {
							num++;
						} else {
							tracer.error("Cannot delete temporary file: "
									+ file.getAbsolutePath());
						}
					}
				}
			}
		}

		return num;
	}

	/**
	 * Open a resource from the cache if it exists.
	 * 
	 * @param uniqueID
	 *            the unique ID
	 * @param allowTooOld
	 *            allow files even if they are considered too old
	 * @param stable
	 *            a stable file (that dones't change too often) -- parameter
	 *            used to check if the file is too old to keep or not
	 * 
	 * @return the opened resource if found, NULL if not
	 */
	public InputStream load(String uniqueID, boolean allowTooOld, boolean stable) {
		return load(getCached(uniqueID), allowTooOld, stable);
	}

	/**
	 * Open a resource from the cache if it exists.
	 * 
	 * @param url
	 *            the resource to open
	 * @param allowTooOld
	 *            allow files even if they are considered too old
	 * @param stable
	 *            a stable file (that doesn't change too often) -- parameter
	 *            used to check if the file is too old to keep or not in the
	 *            cache
	 * 
	 * @return the opened resource if found, NULL if not
	 */
	public InputStream load(URL url, boolean allowTooOld, boolean stable) {
		return load(getCached(url), allowTooOld, stable);
	}

	/**
	 * Open a resource from the cache if it exists.
	 * 
	 * @param cached
	 *            the resource to open
	 * @param allowTooOld
	 *            allow files even if they are considered too old
	 * @param stable
	 *            a stable file (that dones't change too often) -- parameter
	 *            used to check if the file is too old to keep or not
	 * 
	 * @return the opened resource if found, NULL if not
	 */
	private InputStream load(File cached, boolean allowTooOld, boolean stable) {
		if (cached.exists() && cached.isFile()
				&& (allowTooOld || !isOld(cached, stable))) {
			try {
				return new MarkableFileInputStream(cached);
			} catch (FileNotFoundException e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Save the given resource to the cache.
	 * 
	 * @param in
	 *            the input data
	 * @param uniqueID
	 *            a unique ID used to locate the cached resource
	 * 
	 * @return the number of bytes written
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public long save(InputStream in, String uniqueID) throws IOException {
		File cached = getCached(uniqueID);
		cached.getParentFile().mkdirs();
		return save(in, cached);
	}

	/**
	 * Save the given resource to the cache.
	 * 
	 * @param in
	 *            the input data
	 * @param url
	 *            the {@link URL} used to locate the cached resource
	 * 
	 * @return the number of bytes written
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public long save(InputStream in, URL url) throws IOException {
		File cached = getCached(url);
		return save(in, cached);
	}

	/**
	 * Save the given resource to the cache.
	 * <p>
	 * Will also clean the {@link Cache} from old files.
	 * 
	 * @param in
	 *            the input data
	 * @param cached
	 *            the cached {@link File} to save to
	 * 
	 * @return the number of bytes written
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	private long save(InputStream in, File cached) throws IOException {
		// We want to force at least an immediate SAVE/LOAD to work for some
		// workflows, even if we don't accept cached files (times set to "0"
		// -- and not "-1" or a positive value)
		clean(true, dir, 10);
		cached.getParentFile().mkdirs(); // in case we deleted our own parent
		long bytes = IOUtils.write(in, cached);
		return bytes;
	}

	/**
	 * Remove the given resource from the cache.
	 * 
	 * @param uniqueID
	 *            a unique ID used to locate the cached resource
	 * 
	 * @return TRUE if it was removed
	 */
	public boolean remove(String uniqueID) {
		File cached = getCached(uniqueID);
		return cached.delete();
	}

	/**
	 * Remove the given resource from the cache.
	 * 
	 * @param url
	 *            the {@link URL} used to locate the cached resource
	 * 
	 * @return TRUE if it was removed
	 */
	public boolean remove(URL url) {
		File cached = getCached(url);
		return cached.delete();
	}

	/**
	 * Check if the {@link File} is too old according to
	 * {@link Cache#tooOldChanging}.
	 * 
	 * @param file
	 *            the file to check
	 * @param stable
	 *            TRUE to denote stable files, that are not supposed to change
	 *            too often
	 * 
	 * @return TRUE if it is
	 */
	private boolean isOld(File file, boolean stable) {
		long max = tooOldChanging;
		if (stable) {
			max = tooOldStable;
		}

		if (max < 0) {
			return false;
		}

		long time = new Date().getTime() - file.lastModified();
		if (time < 0) {
			tracer.error("Timestamp in the future for file: "
					+ file.getAbsolutePath());
		}

		return time < 0 || time > max;
	}

	/**
	 * Return the associated cache {@link File} from this {@link URL}.
	 * 
	 * @param url
	 *            the {@link URL}
	 * 
	 * @return the cached {@link File} version of this {@link URL}
	 */
	private File getCached(URL url) {
		File subdir;

		String name = url.getHost();
		if (name == null || name.isEmpty()) {
			// File
			File file = new File(url.getFile());
			if (file.getParent() == null) {
				subdir = new File("+");
			} else {
				subdir = new File(file.getParent().replace("..", "__"));
			}
			subdir = new File(dir, allowedChars(subdir.getPath()));
			name = allowedChars(url.getFile());
		} else {
			// URL
			File subsubDir = new File(dir, allowedChars(url.getHost()));
			subdir = new File(subsubDir, "_" + allowedChars(url.getPath()));
			name = allowedChars("_" + url.getQuery());
		}

		File cacheFile = new File(subdir, name);
		subdir.mkdirs();

		return cacheFile;
	}

	/**
	 * Get the basic cache resource file corresponding to this unique ID.
	 * <p>
	 * Note that you may need to add a sub-directory in some cases.
	 * 
	 * @param uniqueID
	 *            the id
	 * 
	 * @return the cached version if present, NULL if not
	 */
	private File getCached(String uniqueID) {
		File file = new File(dir, allowedChars(uniqueID));
		File subdir = new File(file.getParentFile(), "_");
		return new File(subdir, file.getName());
	}

	/**
	 * Replace not allowed chars (in a {@link File}) by "_".
	 * 
	 * @param raw
	 *            the raw {@link String}
	 * 
	 * @return the sanitised {@link String}
	 */
	private String allowedChars(String raw) {
		return raw.replace('/', '_').replace(':', '_').replace("\\", "_");
	}
}