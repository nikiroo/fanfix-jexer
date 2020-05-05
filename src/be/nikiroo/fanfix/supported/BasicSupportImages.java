package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.utils.Image;

/**
 * Helper class for {@link BasicSupport}, mostly dedicated to images for
 * the classes that implement {@link BasicSupport}.
 * 
 * @author niki
 */
public class BasicSupportImages {
	/**
	 * Check if the given resource can be a local image or a remote image, then
	 * refresh the cache with it if it is.
	 * 
	 * @param support
	 *            the support to use to download the resource (can be NULL)
	 * @param dir
	 *            the local directory to search, if any
	 * @param line
	 *            the resource to check
	 * 
	 * @return the image if found, or NULL
	 */
	public Image getImage(BasicSupport support, File dir, String line) {
		URL url = getImageUrl(support, dir, line);
		return getImage(support,url);
	}
	
	/**
	 * Check if the given resource can be a local image or a remote image, then
	 * refresh the cache with it if it is.
	 * 
	 * @param support
	 *            the support to use to download the resource (can be NULL)
	 * @param url
	 *            the actual URL to check (file or remote, can be NULL)
	 * 
	 * @return the image if found, or NULL
	 */
	public Image getImage(BasicSupport support, URL url) {
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
	 *            the support to use to download the resource (can be NULL)
	 * @param dir
	 *            the local directory to search, if any
	 * @param line
	 *            the resource to check
	 * 
	 * @return the image URL if found, or NULL
	 * 
	 */
	public URL getImageUrl(BasicSupport support, File dir, String line) {
		URL url = null;

		if (line != null) {
			// try for files
			if (dir != null && dir.exists() && !dir.isFile()) {
				try {

					String relPath = null;
					String absPath = null;
					try {
						relPath = new File(dir, line.trim()).getAbsolutePath();
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
						if (Instance.getInstance().getCache()
								.check(new URL(line + ext), true)) {
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
}
