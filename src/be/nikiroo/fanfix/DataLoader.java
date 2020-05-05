package be.nikiroo.fanfix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.supported.BasicSupport;
import be.nikiroo.utils.Cache;
import be.nikiroo.utils.CacheMemory;
import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.ImageUtils;
import be.nikiroo.utils.TraceHandler;

/**
 * This cache will manage Internet (and local) downloads, as well as put the
 * downloaded files into a cache.
 * <p>
 * As long the cached resource is not too old, it will use it instead of
 * retrieving the file again.
 * 
 * @author niki
 */
public class DataLoader {
	private Downloader downloader;
	private Downloader downloaderNoCache;
	private Cache cache;
	private boolean offline;

	/**
	 * Create a new {@link DataLoader} object.
	 * 
	 * @param dir
	 *            the directory to use as cache
	 * @param UA
	 *            the User-Agent to use to download the resources
	 * @param hoursChanging
	 *            the number of hours after which a cached file that is thought
	 *            to change ~often is considered too old (or -1 for
	 *            "never too old")
	 * @param hoursStable
	 *            the number of hours after which a LARGE cached file that is
	 *            thought to change rarely is considered too old (or -1 for
	 *            "never too old")
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public DataLoader(File dir, String UA, int hoursChanging, int hoursStable)
			throws IOException {
		downloader = new Downloader(UA, new Cache(dir, hoursChanging,
				hoursStable));
		downloaderNoCache = new Downloader(UA);

		cache = downloader.getCache();
	}

	/**
	 * Create a new {@link DataLoader} object without disk cache (will keep a
	 * memory cache for manual cache operations).
	 * 
	 * @param UA
	 *            the User-Agent to use to download the resources
	 */
	public DataLoader(String UA) {
		downloader = new Downloader(UA);
		downloaderNoCache = downloader;
		cache = new CacheMemory();
	}
	
	/**
	 * This {@link Downloader} is forbidden to try and connect to the network.
	 * <p>
	 * If TRUE, it will only check the cache (even in no-cache mode!).
	 * <p>
	 * Default is FALSE.
	 * 
	 * @return TRUE if offline
	 */
	public boolean isOffline() {
		return offline;
	}
	
	/**
	 * This {@link Downloader} is forbidden to try and connect to the network.
	 * <p>
	 * If TRUE, it will only check the cache (even in no-cache mode!).
	 * <p>
	 * Default is FALSE.
	 * 
	 * @param offline TRUE for offline, FALSE for online
	 */
	public void setOffline(boolean offline) {
		this.offline = offline;
		downloader.setOffline(offline);
		downloaderNoCache.setOffline(offline);
		
		// If we don't, we cannot support no-cache using code in OFFLINE mode
		if (offline) {
			downloaderNoCache.setCache(cache);
		} else {
			downloaderNoCache.setCache(null);
		}
	}

	/**
	 * The traces handler for this {@link Cache}.
	 * 
	 * @param tracer
	 *            the new traces handler
	 */
	public void setTraceHandler(TraceHandler tracer) {
		downloader.setTraceHandler(tracer);
		downloaderNoCache.setTraceHandler(tracer);
		cache.setTraceHandler(tracer);
		if (downloader.getCache() != null) {
			downloader.getCache().setTraceHandler(tracer);
		}

	}

	/**
	 * Open a resource (will load it from the cache if possible, or save it into
	 * the cache after downloading if not).
	 * <p>
	 * The cached resource will be assimilated to the given original {@link URL}
	 * 
	 * @param url
	 *            the resource to open
	 * @param support
	 *            the support to use to download the resource (can be NULL)
	 * @param stable
	 *            TRUE for more stable resources, FALSE when they often change
	 * 
	 * @return the opened resource, NOT NULL
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public InputStream open(URL url, BasicSupport support, boolean stable)
			throws IOException {
		return open(url, url, support, stable, null, null, null);
	}

	/**
	 * Open a resource (will load it from the cache if possible, or save it into
	 * the cache after downloading if not).
	 * <p>
	 * The cached resource will be assimilated to the given original {@link URL}
	 * 
	 * @param url
	 *            the resource to open
	 * @param originalUrl
	 *            the original {@link URL} before any redirection occurs, which
	 *            is also used for the cache ID if needed (so we can retrieve
	 *            the content with this URL if needed)
	 * @param support
	 *            the support to use to download the resource
	 * @param stable
	 *            TRUE for more stable resources, FALSE when they often change
	 * 
	 * @return the opened resource, NOT NULL
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public InputStream open(URL url, URL originalUrl, BasicSupport support,
			boolean stable) throws IOException {
		return open(url, originalUrl, support, stable, null, null, null);
	}

	/**
	 * Open a resource (will load it from the cache if possible, or save it into
	 * the cache after downloading if not).
	 * <p>
	 * The cached resource will be assimilated to the given original {@link URL}
	 * 
	 * @param url
	 *            the resource to open
	 * @param originalUrl
	 *            the original {@link URL} before any redirection occurs, which
	 *            is also used for the cache ID if needed (so we can retrieve
	 *            the content with this URL if needed)
	 * @param support
	 *            the support to use to download the resource (can be NULL)
	 * @param stable
	 *            TRUE for more stable resources, FALSE when they often change
	 * @param postParams
	 *            the POST parameters
	 * @param getParams
	 *            the GET parameters (priority over POST)
	 * @param oauth
	 *            OAuth authorization (aka, "bearer XXXXXXX")
	 * 
	 * @return the opened resource, NOT NULL
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public InputStream open(URL url, URL originalUrl, BasicSupport support,
			boolean stable, Map<String, String> postParams,
			Map<String, String> getParams, String oauth) throws IOException {

		Map<String, String> cookiesValues = null;
		URL currentReferer = url;

		if (support != null) {
			cookiesValues = support.getCookies();
			currentReferer = support.getCurrentReferer();
			// priority: arguments
			if (oauth == null) {
				oauth = support.getOAuth();
			}
		}

		return downloader.open(url, originalUrl, currentReferer, cookiesValues,
				postParams, getParams, oauth, stable);
	}

	/**
	 * Open the given {@link URL} without using the cache, but still using and
	 * updating the cookies.
	 * 
	 * @param url
	 *            the {@link URL} to open
	 * @param support
	 *            the {@link BasicSupport} used for the cookies
	 * @param postParams
	 *            the POST parameters
	 * @param getParams
	 *            the GET parameters (priority over POST)
	 * @param oauth
	 *            OAuth authorization (aka, "bearer XXXXXXX")
	 * 
	 * @return the {@link InputStream} of the opened page
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public InputStream openNoCache(URL url, BasicSupport support,
			Map<String, String> postParams, Map<String, String> getParams,
			String oauth) throws IOException {

		Map<String, String> cookiesValues = null;
		URL currentReferer = url;
		if (support != null) {
			cookiesValues = support.getCookies();
			currentReferer = support.getCurrentReferer();
			// priority: arguments
			if (oauth == null) {
				oauth = support.getOAuth();
			}
		}

		return downloaderNoCache.open(url, currentReferer, cookiesValues,
				postParams, getParams, oauth);
	}

	/**
	 * Refresh the resource into cache if needed.
	 * 
	 * @param url
	 *            the resource to open
	 * @param support
	 *            the support to use to download the resource (can be NULL)
	 * @param stable
	 *            TRUE for more stable resources, FALSE when they often change
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void refresh(URL url, BasicSupport support, boolean stable)
			throws IOException {
		if (!check(url, stable)) {
			open(url, url, support, stable, null, null, null).close();
		}
	}

	/**
	 * Check the resource to see if it is in the cache.
	 * 
	 * @param url
	 *            the resource to check
	 * @param stable
	 *            a stable file (that dones't change too often) -- parameter
	 *            used to check if the file is too old to keep or not
	 * 
	 * @return TRUE if it is
	 * 
	 */
	public boolean check(URL url, boolean stable) {
		return downloader.getCache() != null
				&& downloader.getCache().check(url, false, stable);
	}

	/**
	 * Save the given resource as an image on disk using the default image
	 * format for content or cover -- will automatically add the extension, too.
	 * 
	 * @param img
	 *            the resource
	 * @param target
	 *            the target file without extension
	 * @param cover
	 *            use the cover image format instead of the content image format
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void saveAsImage(Image img, File target, boolean cover)
			throws IOException {
		String format;
		if (cover) {
			format = Instance.getInstance().getConfig().getString(Config.FILE_FORMAT_IMAGE_FORMAT_COVER).toLowerCase();
		} else {
			format = Instance.getInstance().getConfig().getString(Config.FILE_FORMAT_IMAGE_FORMAT_CONTENT)
					.toLowerCase();
		}
		saveAsImage(img, new File(target.toString() + "." + format), format);
	}

	/**
	 * Save the given resource as an image on disk using the given image format
	 * for content, or with "png" format if it fails.
	 * 
	 * @param img
	 *            the resource
	 * @param target
	 *            the target file
	 * @param format
	 *            the file format ("png", "jpeg", "bmp"...)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void saveAsImage(Image img, File target, String format)
			throws IOException {
		ImageUtils.getInstance().saveAsImage(img, target, format);
	}

	/**
	 * Manually add this item to the cache.
	 * 
	 * @param in
	 *            the input data
	 * @param uniqueID
	 *            a unique ID for this resource
	 * 
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void addToCache(InputStream in, String uniqueID) throws IOException {
		cache.save(in, uniqueID);
	}

	/**
	 * Return the {@link InputStream} corresponding to the given unique ID, or
	 * NULL if none found.
	 * 
	 * @param uniqueID
	 *            the unique ID
	 * 
	 * @return the content or NULL
	 */
	public InputStream getFromCache(String uniqueID) {
		return cache.load(uniqueID, true, true);
	}

	/**
	 * Remove the given resource from the cache.
	 * 
	 * @param uniqueID
	 *            a unique ID used to locate the cached resource
	 * 
	 * @return TRUE if it was removed
	 */
	public boolean removeFromCache(String uniqueID) {
		return cache.remove(uniqueID);
	}

	/**
	 * Clean the cache (delete the cached items).
	 * 
	 * @param onlyOld
	 *            only clean the files that are considered too old
	 * 
	 * @return the number of cleaned items
	 */
	public int cleanCache(boolean onlyOld) {
		return cache.clean(onlyOld);
	}
}
