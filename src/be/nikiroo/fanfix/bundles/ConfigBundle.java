package be.nikiroo.fanfix.bundles;

import java.io.File;
import java.io.IOException;

import be.nikiroo.utils.resources.Bundle;

/**
 * This class manages the configuration of the application.
 * 
 * @author niki
 */
public class ConfigBundle extends Bundle<Config> {
	/**
	 * Create a new {@link ConfigBundle}.
	 */
	public ConfigBundle() {
		super(Config.class, Target.config5, null);
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
		new ConfigBundle().updateFile(path);
		System.out.println("Path updated: " + path);
	}

	@Override
	protected String getBundleDisplayName() {
		return "Configuration options";
	}
}
