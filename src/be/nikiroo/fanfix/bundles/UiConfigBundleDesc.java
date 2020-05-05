package be.nikiroo.fanfix.bundles;

import java.io.File;
import java.io.IOException;

import be.nikiroo.utils.resources.TransBundle;

/**
 * This class manages the configuration of UI of the application (colours and
 * behaviour)
 * 
 * @author niki
 */
public class UiConfigBundleDesc extends TransBundle<UiConfig> {
	public UiConfigBundleDesc() {
		super(UiConfig.class, Target.ui_description);
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
		new UiConfigBundleDesc().updateFile(path);
		System.out.println("Path updated: " + path);
	}

	@Override
	protected String getBundleDisplayName() {
		return "UI configuration options description";
	}
}
