package be.nikiroo.utils.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Fixed ResourceBundle.Control class. It will use UTF-8 for the files to load.
 * 
 * Also support an option to first check into the given path before looking into
 * the resources.
 * 
 * @author niki
 * 
 */
class FixedResourceBundleControl extends Control {
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale,
			String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		// The below is a copy of the default implementation.
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");

		ResourceBundle bundle = null;
		InputStream stream = null;
		if (reload) {
			URL url = loader.getResource(resourceName);
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					connection.setUseCaches(false);
					stream = connection.getInputStream();
				}
			}
		} else {
			stream = loader.getResourceAsStream(resourceName);
		}

		if (stream != null) {
			try {
				// This line is changed to make it to read properties files
				// as UTF-8.
				// How can someone use an archaic encoding such as ISO 8859-1 by
				// *DEFAULT* is beyond me...
				bundle = new PropertyResourceBundle(new InputStreamReader(
						stream, "UTF-8"));
			} finally {
				stream.close();
			}
		}
		return bundle;
	}
}