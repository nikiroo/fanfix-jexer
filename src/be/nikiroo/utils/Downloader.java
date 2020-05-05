package be.nikiroo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * This class will help you download content from Internet Sites ({@link URL}
 * based).
 * <p>
 * It allows you to control some options often required on web sites that do not
 * want to simply serve HTML, but actively makes your life difficult with stupid
 * checks.
 * 
 * @author niki
 */
public class Downloader {
	private String UA;
	private CookieManager cookies;
	private TraceHandler tracer = new TraceHandler();
	private Cache cache;
	private boolean offline;

	/**
	 * Create a new {@link Downloader}.
	 * 
	 * @param UA
	 *            the User-Agent to use to download the resources -- note that
	 *            some websites require one, some actively blacklist real UAs
	 *            like the one from wget, some whitelist a couple of browsers
	 *            only (!)
	 */
	public Downloader(String UA) {
		this(UA, null);
	}

	/**
	 * Create a new {@link Downloader}.
	 * 
	 * @param UA
	 *            the User-Agent to use to download the resources -- note that
	 *            some websites require one, some actively blacklist real UAs
	 *            like the one from wget, some whitelist a couple of browsers
	 *            only (!)
	 * @param cache
	 *            the {@link Cache} to use for all access (can be NULL)
	 */
	public Downloader(String UA, Cache cache) {
		this.UA = UA;

		cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookies);

		setCache(cache);
	}
	
	/**
	 * This {@link Downloader} is forbidden to try and connect to the network.
	 * <p>
	 * If TRUE, it will only check the cache if any.
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
	 * If TRUE, it will only check the cache if any.
	 * <p>
	 * Default is FALSE.
	 * 
	 * @param offline TRUE for offline, FALSE for online
	 */
	public void setOffline(boolean offline) {
		this.offline = offline;
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
	 * The {@link Cache} to use for all access (can be NULL).
	 * 
	 * @return the cache
	 */
	public Cache getCache() {
		return cache;
	}

	/**
	 * The {@link Cache} to use for all access (can be NULL).
	 * 
	 * @param cache
	 *            the new cache
	 */
	public void setCache(Cache cache) {
		this.cache = cache;
	}

	/**
	 * Clear all the cookies currently in the jar.
	 * <p>
	 * As long as you don't, the cookies are kept.
	 */
	public void clearCookies() {
		cookies.getCookieStore().removeAll();
	}

	/**
	 * Open the given {@link URL} and update the cookies.
	 * 
	 * @param url
	 *            the {@link URL} to open
	 * @return the {@link InputStream} of the opened page
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 **/
	public InputStream open(URL url) throws IOException {
		return open(url, false);
	}

	/**
	 * Open the given {@link URL} and update the cookies.
	 * 
	 * @param url
	 *            the {@link URL} to open
	 * @param stable
	 *            stable a stable file (that doesn't change too often) --
	 *            parameter used to check if the file is too old to keep or not
	 *            in the cache (default is false)
	 * 
	 * @return the {@link InputStream} of the opened page
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 **/
	public InputStream open(URL url, boolean stable) throws IOException {
		return open(url, url, url, null, null, null, null, stable);
	}

	/**
	 * Open the given {@link URL} and update the cookies.
	 * 
	 * @param url
	 *            the {@link URL} to open
	 * @param currentReferer
	 *            the current referer, for websites that needs this info
	 * @param cookiesValues
	 *            the cookies
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
	 *             in case of I/O error (including offline mode + not in cache)
	 */
	public InputStream open(URL url, URL currentReferer,
			Map<String, String> cookiesValues, Map<String, String> postParams,
			Map<String, String> getParams, String oauth) throws IOException {
		return open(url, currentReferer, cookiesValues, postParams, getParams,
				oauth, false);
	}

	/**
	 * Open the given {@link URL} and update the cookies.
	 * 
	 * @param url
	 *            the {@link URL} to open
	 * @param currentReferer
	 *            the current referer, for websites that needs this info
	 * @param cookiesValues
	 *            the cookies
	 * @param postParams
	 *            the POST parameters
	 * @param getParams
	 *            the GET parameters (priority over POST)
	 * @param oauth
	 *            OAuth authorization (aka, "bearer XXXXXXX")
	 * @param stable
	 *            stable a stable file (that doesn't change too often) --
	 *            parameter used to check if the file is too old to keep or not
	 *            in the cache (default is false)
	 * 
	 * @return the {@link InputStream} of the opened page
	 * 
	 * @throws IOException
	 *             in case of I/O error (including offline mode + not in cache)
	 */
	public InputStream open(URL url, URL currentReferer,
			Map<String, String> cookiesValues, Map<String, String> postParams,
			Map<String, String> getParams, String oauth, boolean stable)
			throws IOException {
		return open(url, url, currentReferer, cookiesValues, postParams,
				getParams, oauth, stable);
	}

	/**
	 * Open the given {@link URL} and update the cookies.
	 * 
	 * @param url
	 *            the {@link URL} to open
	 * @param originalUrl
	 *            the original {@link URL} before any redirection occurs, which
	 *            is also used for the cache ID if needed (so we can retrieve
	 *            the content with this URL if needed)
	 * @param currentReferer
	 *            the current referer, for websites that needs this info
	 * @param cookiesValues
	 *            the cookies
	 * @param postParams
	 *            the POST parameters
	 * @param getParams
	 *            the GET parameters (priority over POST)
	 * @param oauth
	 *            OAuth authorisation (aka, "bearer XXXXXXX")
	 * @param stable
	 *            a stable file (that doesn't change too often) -- parameter
	 *            used to check if the file is too old to keep or not in the
	 *            cache
	 * 
	 * @return the {@link InputStream} of the opened page
	 * 
	 * @throws IOException
	 *             in case of I/O error (including offline mode + not in cache)
	 */
	public InputStream open(URL url, final URL originalUrl, URL currentReferer,
			Map<String, String> cookiesValues, Map<String, String> postParams,
			Map<String, String> getParams, String oauth, boolean stable)
			throws IOException {

		tracer.trace("Request: " + url);

		if (cache != null) {
			InputStream in = cache.load(originalUrl, false, stable);
			if (in != null) {
				tracer.trace("Use the cache: " + url);
				tracer.trace("Original URL : " + originalUrl);
				return in;
			}
		}

		String protocol = originalUrl == null ? null : originalUrl
				.getProtocol();
		if (isOffline() && !"file".equalsIgnoreCase(protocol)) {
			tracer.error("Downloader OFFLINE, cannot proceed to URL: " + url);
			throw new IOException("Downloader is currently OFFLINE, cannot download: " + url);
		}
		
		tracer.trace("Download: " + url);

		URLConnection conn = openConnectionWithCookies(url, currentReferer,
				cookiesValues);

		// Priority: GET over POST
		Map<String, String> params = getParams;
		if (getParams == null) {
			params = postParams;
		}

		StringBuilder requestData = null;
		if ((params != null || oauth != null)
				&& conn instanceof HttpURLConnection) {
			if (params != null) {
				requestData = new StringBuilder();
				for (Map.Entry<String, String> param : params.entrySet()) {
					if (requestData.length() != 0)
						requestData.append('&');
					requestData.append(URLEncoder.encode(param.getKey(),
							"UTF-8"));
					requestData.append('=');
					requestData.append(URLEncoder.encode(
							String.valueOf(param.getValue()), "UTF-8"));
				}

				if (getParams == null && postParams != null) {
					((HttpURLConnection) conn).setRequestMethod("POST");
				}

				conn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length",
						Integer.toString(requestData.length()));
			}

			if (oauth != null) {
				conn.setRequestProperty("Authorization", oauth);
			}

			if (requestData != null) {
				conn.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(
						conn.getOutputStream());
				try {
					writer.write(requestData.toString());
					writer.flush();
				} finally {
					writer.close();
				}
			}
		}

		// Manual redirection, much better for POST data
		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).setInstanceFollowRedirects(false);
		}

		conn.connect();

		// Check if redirect
		// BEWARE! POST data cannot be redirected (some webservers complain) for
		// HTTP codes 302 and 303
		if (conn instanceof HttpURLConnection) {
			int repCode = 0;
			try {
				// Can fail in some circumstances
				repCode = ((HttpURLConnection) conn).getResponseCode();
			} catch (IOException e) {
			}

			if (repCode / 100 == 3) {
				String newUrl = conn.getHeaderField("Location");
				return open(new URL(newUrl), originalUrl, currentReferer,
						cookiesValues, //
						(repCode == 302 || repCode == 303) ? null : postParams, //
						getParams, oauth, stable);
			}
		}

		try {
			InputStream in = conn.getInputStream();
			if ("gzip".equals(conn.getContentEncoding())) {
				in = new GZIPInputStream(in);
			}

			if (in == null) {
				throw new IOException("No InputStream!");
			}

			if (cache != null) {
				String size = conn.getContentLength() < 0 ? "unknown size"
						: StringUtils.formatNumber(conn.getContentLength())
								+ "bytes";
				tracer.trace("Save to cache (" + size + "): " + originalUrl);
				try {
					try {
						long bytes = cache.save(in, originalUrl);
						tracer.trace("Saved to cache: "
								+ StringUtils.formatNumber(bytes) + "bytes");
					} finally {
						in.close();
					}
					in = cache.load(originalUrl, true, true);
				} catch (IOException e) {
					tracer.error(new IOException(
							"Cannot save URL to cache, will ignore cache: "
									+ url, e));
				}
			}

			if (in == null) {
				throw new IOException(
						"Cannot retrieve the file after storing it in the cache (??)");
			}
			
			return in;
		} catch (IOException e) {
			throw new IOException(String.format(
					"Cannot find %s (current URL: %s)", originalUrl, url), e);
		}
	}

	/**
	 * Open a connection on the given {@link URL}, and manage the cookies that
	 * come with it.
	 * 
	 * @param url
	 *            the {@link URL} to open
	 * 
	 * @return the connection
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	private URLConnection openConnectionWithCookies(URL url,
			URL currentReferer, Map<String, String> cookiesValues)
			throws IOException {
		URLConnection conn = url.openConnection();

		String cookies = generateCookies(cookiesValues);
		if (cookies != null && !cookies.isEmpty()) {
			conn.setRequestProperty("Cookie", cookies);
		}

		conn.setRequestProperty("User-Agent", UA);
		conn.setRequestProperty("Accept-Encoding", "gzip");
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Charset", "utf-8");

		if (currentReferer != null) {
			conn.setRequestProperty("Referer", currentReferer.toString());
			conn.setRequestProperty("Host", currentReferer.getHost());
		}

		return conn;
	}

	/**
	 * Generate the cookie {@link String} from the local {@link CookieStore} so
	 * it is ready to be passed.
	 * 
	 * @return the cookie
	 */
	private String generateCookies(Map<String, String> cookiesValues) {
		StringBuilder builder = new StringBuilder();
		for (HttpCookie cookie : cookies.getCookieStore().getCookies()) {
			if (builder.length() > 0) {
				builder.append(';');
			}

			builder.append(cookie.toString());
		}

		if (cookiesValues != null) {
			for (Map.Entry<String, String> set : cookiesValues.entrySet()) {
				if (builder.length() > 0) {
					builder.append(';');
				}
				builder.append(set.getKey());
				builder.append('=');
				builder.append(set.getValue());
			}
		}

		return builder.toString();
	}
}
