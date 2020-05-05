package be.nikiroo.utils.test_code;

import be.nikiroo.utils.Cache;
import be.nikiroo.utils.CacheMemory;
import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.Proxy;
import be.nikiroo.utils.main.bridge;
import be.nikiroo.utils.main.img2aa;
import be.nikiroo.utils.main.justify;
import be.nikiroo.utils.test.TestLauncher;
import be.nikiroo.utils.ui.UIUtils;

/**
 * Tests for nikiroo-utils.
 * 
 * @author niki
 */
public class Test extends TestLauncher {
	/**
	 * Start the tests.
	 * 
	 * @param args
	 *            the arguments (which are passed as-is to the other test
	 *            classes)
	 */
	public Test(String[] args) {
		super("Nikiroo-utils", args);

		// setDetails(true);

		addSeries(new ProgressTest(args));
		addSeries(new BundleTest(args));
		addSeries(new IOUtilsTest(args));
		addSeries(new VersionTest(args));
		addSeries(new SerialTest(args));
		addSeries(new SerialServerTest(args));
		addSeries(new StringUtilsTest(args));
		addSeries(new TempFilesTest(args));
		addSeries(new CryptUtilsTest(args));
		addSeries(new BufferedInputStreamTest(args));
		addSeries(new NextableInputStreamTest(args));
		addSeries(new ReplaceInputStreamTest(args));
		addSeries(new BufferedOutputStreamTest(args));
		addSeries(new ReplaceOutputStreamTest(args));

		// TODO: test cache and downloader
		Cache cache = null;
		CacheMemory memcache = null;
		Downloader downloader = null;
		
		// To include the sources:
		img2aa siu;
		justify ssu;
		bridge aa;
		Proxy proxy;
		UIUtils uiUtils;
	}

	/**
	 * Main entry point of the program.
	 * 
	 * @param args
	 *            the arguments passed to the {@link TestLauncher}s.
	 */
	static public void main(String[] args) {
		System.exit(new Test(args).launch());
	}
}
