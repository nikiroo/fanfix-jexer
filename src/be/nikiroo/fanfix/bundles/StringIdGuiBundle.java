package be.nikiroo.fanfix.bundles;

import java.io.File;
import java.io.IOException;

import be.nikiroo.utils.resources.TransBundle;

/**
 * This class manages the translation resources of the application (GUI).
 * 
 * @author niki
 */
public class StringIdGuiBundle extends TransBundle<StringIdGui> {
	/**
	 * Create a translation service for the given language (will fall back to
	 * the default one if not found).
	 * 
	 * @param lang
	 *            the language to use
	 */
	public StringIdGuiBundle(String lang) {
		super(StringIdGui.class, Target.resources_gui, lang);
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
		new StringIdGuiBundle(null).updateFile(path);
		System.out.println("Path updated: " + path);
	}
}
