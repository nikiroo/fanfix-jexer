package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.StringUtils;

/**
 * Helper class for {@link BasicSupport}, mostly dedicated to text formating for
 * the classes that implement {@link BasicSupport}.
 * 
 * @author niki
 */
public class BasicSupportHelper {
	/**
	 * Get the default cover related to this subject (see <tt>.info</tt> files).
	 * 
	 * @param subject
	 *            the subject
	 * 
	 * @return the cover if any, or NULL
	 */
	public Image getDefaultCover(String subject) {
		if (subject != null && !subject.isEmpty() && Instance.getInstance().getCoverDir() != null) {
			try {
				File fileCover = new File(Instance.getInstance().getCoverDir(), subject);
				return getImage(null, fileCover.toURI().toURL(), subject);
			} catch (MalformedURLException e) {
			}
		}

		return null;
	}

	/**
	 * Return the list of supported image extensions.
	 * 
	 * @param emptyAllowed
	 *            TRUE to allow an empty extension on first place, which can be
	 *            used when you may already have an extension in your input but
	 *            are not sure about it
	 * 
	 * @return the extensions
	 */
	public String[] getImageExt(boolean emptyAllowed) {
		if (emptyAllowed) {
			return new String[] { "", ".png", ".jpg", ".jpeg", ".gif", ".bmp" };
		}

		return new String[] { ".png", ".jpg", ".jpeg", ".gif", ".bmp" };
	}

	/**
	 * Check if the given resource can be a local image or a remote image, then
	 * refresh the cache with it if it is.
	 * 
	 * @param support
	 *            the linked {@link BasicSupport} (can be NULL)
	 * @param source
	 *            the source of the story (for image lookup in the same path if
	 *            the source is a file, can be NULL)
	 * @param line
	 *            the resource to check
	 * 
	 * @return the image if found, or NULL
	 * 
	 */
	public Image getImage(BasicSupport support, URL source, String line) {
		URL url = getImageUrl(support, source, line);
		if (url != null) {
			if ("file".equals(url.getProtocol())) {
				if (new File(url.getPath()).isDirectory()) {
					return null;
				}
			}
			InputStream in = null;
			try {
				in = Instance.getInstance().getCache().open(url, support, true);
				return new Image(in);
			} catch (IOException e) {
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}

		return null;
	}

	/**
	 * Check if the given resource can be a local image or a remote image, then
	 * refresh the cache with it if it is.
	 * 
	 * @param support
	 *            the linked {@link BasicSupport} (can be NULL)
	 * @param source
	 *            the source of the story (for image lookup in the same path if
	 *            the source is a file, can be NULL)
	 * @param line
	 *            the resource to check
	 * 
	 * @return the image URL if found, or NULL
	 * 
	 */
	public URL getImageUrl(BasicSupport support, URL source, String line) {
		URL url = null;

		if (line != null) {
			// try for files
			if (source != null) {
				try {

					String relPath = null;
					String absPath = null;
					try {
						String path = new File(source.getFile()).getParent();
						relPath = new File(new File(path), line.trim())
								.getAbsolutePath();
					} catch (Exception e) {
						// Cannot be converted to path (one possibility to take
						// into account: absolute path on Windows)
					}
					try {
						absPath = new File(line.trim()).getAbsolutePath();
					} catch (Exception e) {
						// Cannot be converted to path (at all)
					}

					for (String ext : getImageExt(true)) {
						File absFile = new File(absPath + ext);
						File relFile = new File(relPath + ext);
						if (absPath != null && absFile.exists()
								&& absFile.isFile()) {
							url = absFile.toURI().toURL();
						} else if (relPath != null && relFile.exists()
								&& relFile.isFile()) {
							url = relFile.toURI().toURL();
						}
					}
				} catch (Exception e) {
					// Should not happen since we control the correct arguments
				}
			}

			if (url == null) {
				// try for URLs
				try {
					for (String ext : getImageExt(true)) {
						if (Instance.getInstance().getCache().check(new URL(line + ext), true)) {
							url = new URL(line + ext);
							break;
						}
					}

					// try out of cache
					if (url == null) {
						for (String ext : getImageExt(true)) {
							try {
								url = new URL(line + ext);
								Instance.getInstance().getCache().refresh(url, support, true);
								break;
							} catch (IOException e) {
								// no image with this ext
								url = null;
							}
						}
					}
				} catch (MalformedURLException e) {
					// Not an url
				}
			}

			// refresh the cached file
			if (url != null) {
				try {
					Instance.getInstance().getCache().refresh(url, support, true);
				} catch (IOException e) {
					// woops, broken image
					url = null;
				}
			}
		}

		return url;
	}

	/**
	 * Fix the author name if it is prefixed with some "by" {@link String}.
	 * 
	 * @param author
	 *            the author with a possible prefix
	 * 
	 * @return the author without prefixes
	 */
	public String fixAuthor(String author) {
		if (author != null) {
			for (String suffix : new String[] { " ", ":" }) {
				for (String byString : Instance.getInstance().getConfig().getList(Config.CONF_BYS)) {
					byString += suffix;
					if (author.toUpperCase().startsWith(byString.toUpperCase())) {
						author = author.substring(byString.length()).trim();
					}
				}
			}

			// Special case (without suffix):
			if (author.startsWith("©")) {
				author = author.substring(1);
			}
		}

		return author;
	}
	
	/**
	 * Try to convert the date to a known, fixed format.
	 * <p>
	 * If it fails to do so, it will return the date as-is.
	 * 
	 * @param date
	 *            the date to convert
	 * 
	 * @return the converted date, or the date as-is
	 */
	public String formatDate(String date) {
		long ms = 0;

		if (date != null && !date.isEmpty()) {
			// Default Fanfix format:
			try {
				ms = StringUtils.toTime(date);
			} catch (ParseException e) {
			}

			// Second chance:
			if (ms <= 0) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ssSSS");
				try {
					ms = sdf.parse(date).getTime();
				} catch (ParseException e) {
				}
			}

			// Last chance:
			if (ms <= 0 && date.length() >= 10) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					ms = sdf.parse(date.substring(0, 10)).getTime();
				} catch (ParseException e) {
				}
			}

			// If we found something, use THIS format:
			if (ms > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				return sdf.format(new Date(ms));
			}
		}

		if (date == null) {
			date = "";
		}

		// :(
		return date;
	}
}
