package be.nikiroo.fanfix.bundles;

import java.io.File;
import java.io.IOException;

import be.nikiroo.utils.resources.TransBundle;

/**
 * This class manages the translation resources of the application (Core).
 * 
 * @author niki
 */
public class StringIdBundle extends TransBundle<StringId> {
	/**
	 * Create a translation service for the given language (will fall back to
	 * the default one if not found).
	 * 
	 * @param lang
	 *            the language to use
	 */
	public StringIdBundle(String lang) {
		super(StringId.class, Target.resources_core, lang);
	}

	/**
	 * Update resource file.
	 * 
	 * @param args
	 *            not used
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void main(String[] args) throws IOException {
		String path = new File(".").getAbsolutePath()
				+ "/src/be/nikiroo/fanfix/bundles/";
		new StringIdBundle(null).updateFile(path);
		System.out.println("Path updated: " + path);
	}
}
