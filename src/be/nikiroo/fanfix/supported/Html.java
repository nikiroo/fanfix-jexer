package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import be.nikiroo.fanfix.Instance;

/**
 * Support class for HTML files created with this program (as we need some
 * metadata available in those we create).
 * 
 * @author niki
 */
class Html extends InfoText {
	@Override
	protected boolean supports(URL url) {
		try {
			File txt = getTxt(url);
			if (txt != null) {
				return super.supports(txt.toURI().toURL());
			}
		} catch (MalformedURLException e) {
		}

		return false;
	}

	@Override
	protected File getInfoFile() {
		File source = getSourceFile();
		if ("index.html".equals(source.getName())) {
			source = source.getParentFile();
		}

		String src = source.getPath();
		File infoFile = new File(src + ".info");
		if (!infoFile.exists() && src.endsWith(".txt")) {
			infoFile = new File(
					src.substring(0, src.length() - ".txt".length()) + ".info");
		}

		return infoFile;
	}

	@Override
	public URL getCanonicalUrl(URL source) {
		File txt = getTxt(source);
		if (txt != null) {
			try {
				source = txt.toURI().toURL();
			} catch (MalformedURLException e) {
				Instance.getInstance().getTraceHandler()
						.error(new IOException("Cannot convert the right URL for " + source, e));
			}
		}

		return source;
	}

	/**
	 * Return the associated TXT source file if it can be found.
	 * 
	 * @param source
	 *            the source URL
	 * 
	 * @return the supported source text file or NULL
	 */
	private static File getTxt(URL source) {
		try {
			File fakeFile = new File(source.toURI());
			if (fakeFile.getName().equals("index.html")) { // "story/index.html"
				fakeFile = new File(fakeFile.getParent()); // -> "story/"
			}

			if (fakeFile.isDirectory()) { // "story/"
				fakeFile = new File(fakeFile, fakeFile.getName() + ".txt"); // "story/story.txt"
			}

			if (fakeFile.getName().endsWith(".txt")) {
				return fakeFile;
			}
		} catch (Exception e) {
		}

		return null;
	}
}
