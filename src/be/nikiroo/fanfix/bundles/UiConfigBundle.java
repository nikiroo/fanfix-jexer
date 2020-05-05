package be.nikiroo.fanfix.bundles;

import java.io.File;
import java.io.IOException;

import be.nikiroo.utils.resources.Bundle;

/**
 * This class manages the configuration of UI of the application (colours and
 * behaviour)
 * 
 * @author niki
 */
public class UiConfigBundle extends Bundle<UiConfig> {
	public UiConfigBundle() {
		super(UiConfig.class, Target.ui, new UiConfigBundleDesc());
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
		new UiConfigBundle().updateFile(path);
		System.out.println("Path updated: " + path);
	}

	@Override
	protected String getBundleDisplayName() {
		return "UI configuration options";
	}
}
