package be.nikiroo.utils.resources;

import java.util.ResourceBundle;

/**
 * This class help you get UTF-8 bundles for this application.
 * 
 * @author niki
 */
public class Bundles {
	/**
	 * The configuration directory where we try to get the <tt>.properties</tt>
	 * in priority, or NULL to get the information from the compiled resources.
	 */
	static private String confDir = null;

	/**
	 * Set the primary configuration directory to look for <tt>.properties</tt>
	 * files in.
	 * 
	 * All {@link ResourceBundle}s returned by this class after that point will
	 * respect this new directory.
	 * 
	 * @param confDir
	 *            the new directory
	 */
	static public void setDirectory(String confDir) {
		Bundles.confDir = confDir;
	}

	/**
	 * Get the primary configuration directory to look for <tt>.properties</tt>
	 * files in.
	 * 
	 * @return the directory
	 */
	static public String getDirectory() {
		return Bundles.confDir;
	}
}
