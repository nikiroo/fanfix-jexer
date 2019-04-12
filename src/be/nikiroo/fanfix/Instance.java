package be.nikiroo.fanfix;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.bundles.ConfigBundle;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.bundles.StringIdBundle;
import be.nikiroo.fanfix.bundles.StringIdGuiBundle;
import be.nikiroo.fanfix.bundles.UiConfig;
import be.nikiroo.fanfix.bundles.UiConfigBundle;
import be.nikiroo.fanfix.library.BasicLibrary;
import be.nikiroo.fanfix.library.CacheLibrary;
import be.nikiroo.fanfix.library.LocalLibrary;
import be.nikiroo.fanfix.library.RemoteLibrary;
import be.nikiroo.utils.Cache;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Proxy;
import be.nikiroo.utils.TempFiles;
import be.nikiroo.utils.TraceHandler;
import be.nikiroo.utils.resources.Bundles;

/**
 * Global state for the program (services and singletons).
 * 
 * @author niki
 */
public class Instance {
	private static ConfigBundle config;
	private static UiConfigBundle uiconfig;
	private static StringIdBundle trans;
	private static DataLoader cache;
	private static StringIdGuiBundle transGui;
	private static BasicLibrary lib;
	private static File coverDir;
	private static File readerTmp;
	private static File remoteDir;
	private static String configDir;
	private static TraceHandler tracer;
	private static TempFiles tempFiles;

	static {
		// Before we can configure it:
		tracer = new TraceHandler(true, checkEnv("DEBUG"), checkEnv("DEBUG"));

		// config dir:
		configDir = getConfigDir();
		if (!new File(configDir).exists()) {
			new File(configDir).mkdirs();
		}

		// Most of the rest is dependent upon this:
		createConfigs(configDir, false);
		
		// Proxy support
		Proxy.use(Instance.getConfig().getString(Config.USE_PROXY));

		// update tracer:
		boolean debug = Instance.getConfig()
				.getBoolean(Config.DEBUG_ERR, false);
		boolean trace = Instance.getConfig().getBoolean(Config.DEBUG_TRACE,
				false);

		if (checkEnv("DEBUG")) {
			debug = true;
			trace = true;
		}

		tracer = new TraceHandler(true, debug, trace);

		// default Library
		remoteDir = new File(configDir, "remote");
		lib = createDefaultLibrary(remoteDir);

		// create cache
		File tmp = getFile(Config.CACHE_DIR);
		if (tmp == null) {
			// Could have used: System.getProperty("java.io.tmpdir")
			tmp = new File(configDir, "tmp");
		}
		String ua = config.getString(Config.USER_AGENT);
		try {
			int hours = config.getInteger(Config.CACHE_MAX_TIME_CHANGING, -1);
			int hoursLarge = config
					.getInteger(Config.CACHE_MAX_TIME_STABLE, -1);
			cache = new DataLoader(tmp, ua, hours, hoursLarge);
		} catch (IOException e) {
			tracer.error(new IOException(
					"Cannot create cache (will continue without cache)", e));
			cache = new DataLoader(ua);
		}

		cache.setTraceHandler(tracer);

		// readerTmp / coverDir
		readerTmp = getFile(UiConfig.CACHE_DIR_LOCAL_READER);
		if (readerTmp == null) {
			readerTmp = new File(configDir, "tmp-reader");
		}

		coverDir = getFile(Config.DEFAULT_COVERS_DIR);
		if (coverDir != null && !coverDir.exists()) {
			tracer.error(new IOException(
					"The 'default covers' directory does not exists: "
							+ coverDir));
			coverDir = null;
		}

		try {
			tempFiles = new TempFiles("fanfix");
		} catch (IOException e) {
			tracer.error(new IOException("Cannot create temporary directory", e));
		}
	}

	/**
	 * The traces handler for this {@link Cache}.
	 * <p>
	 * It is never NULL.
	 * 
	 * @return the traces handler (never NULL)
	 */
	public static TraceHandler getTraceHandler() {
		return tracer;
	}

	/**
	 * The traces handler for this {@link Cache}.
	 * 
	 * @param tracer
	 *            the new traces handler or NULL
	 */
	public static void setTraceHandler(TraceHandler tracer) {
		if (tracer == null) {
			tracer = new TraceHandler(false, false, false);
		}

		Instance.tracer = tracer;
		cache.setTraceHandler(tracer);
	}

	/**
	 * Get the (unique) configuration service for the program.
	 * 
	 * @return the configuration service
	 */
	public static ConfigBundle getConfig() {
		return config;
	}

	/**
	 * Get the (unique) UI configuration service for the program.
	 * 
	 * @return the configuration service
	 */
	public static UiConfigBundle getUiConfig() {
		return uiconfig;
	}

	/**
	 * Reset the configuration.
	 * 
	 * @param resetTrans
	 *            also reset the translation files
	 */
	public static void resetConfig(boolean resetTrans) {
		String dir = Bundles.getDirectory();
		Bundles.setDirectory(null);
		try {
			try {
				ConfigBundle config = new ConfigBundle();
				config.updateFile(configDir);
			} catch (IOException e) {
				tracer.error(e);
			}
			try {
				UiConfigBundle uiconfig = new UiConfigBundle();
				uiconfig.updateFile(configDir);
			} catch (IOException e) {
				tracer.error(e);
			}

			if (resetTrans) {
				try {
					StringIdBundle trans = new StringIdBundle(null);
					trans.updateFile(configDir);
				} catch (IOException e) {
					tracer.error(e);
				}
			}
		} finally {
			Bundles.setDirectory(dir);
		}
	}

	/**
	 * Get the (unique) {@link DataLoader} for the program.
	 * 
	 * @return the {@link DataLoader}
	 */
	public static DataLoader getCache() {
		return cache;
	}

	/**
	 * Get the (unique) {link StringIdBundle} for the program.
	 * <p>
	 * This is used for the translations of the core parts of Fanfix.
	 * 
	 * @return the {link StringIdBundle}
	 */
	public static StringIdBundle getTrans() {
		return trans;
	}

	/**
	 * Get the (unique) {link StringIdGuiBundle} for the program.
	 * <p>
	 * This is used for the translations of the GUI parts of Fanfix.
	 * 
	 * @return the {link StringIdGuiBundle}
	 */
	public static StringIdGuiBundle getTransGui() {
		return transGui;
	}

	/**
	 * Get the (unique) {@link LocalLibrary} for the program.
	 * 
	 * @return the {@link LocalLibrary}
	 */
	public static BasicLibrary getLibrary() {
		if (lib == null) {
			throw new NullPointerException("We don't have a library to return");
		}

		return lib;
	}

	/**
	 * Return the directory where to look for default cover pages.
	 * 
	 * @return the default covers directory
	 */
	public static File getCoverDir() {
		return coverDir;
	}

	/**
	 * Return the directory where to store temporary files for the local reader.
	 * 
	 * @return the directory
	 */
	public static File getReaderDir() {
		return readerTmp;
	}

	/**
	 * Return the directory where to store temporary files for the remote
	 * {@link LocalLibrary}.
	 * 
	 * @param host
	 *            the remote for this host
	 * 
	 * @return the directory
	 */
	public static File getRemoteDir(String host) {
		return getRemoteDir(remoteDir, host);
	}

	/**
	 * Return the directory where to store temporary files for the remote
	 * {@link LocalLibrary}.
	 * 
	 * @param remoteDir
	 *            the base remote directory
	 * @param host
	 *            the remote for this host
	 * 
	 * @return the directory
	 */
	private static File getRemoteDir(File remoteDir, String host) {
		remoteDir.mkdirs();

		if (host != null) {
			return new File(remoteDir, host);
		}

		return remoteDir;
	}

	/**
	 * Check if we need to check that a new version of Fanfix is available.
	 * 
	 * @return TRUE if we need to
	 */
	public static boolean isVersionCheckNeeded() {
		try {
			long wait = config.getInteger(Config.UPDATE_INTERVAL, 1) * 24 * 60
					* 60 * 1000;
			if (wait >= 0) {
				String lastUpString = IOUtils.readSmallFile(new File(configDir,
						"LAST_UPDATE"));
				long delay = new Date().getTime()
						- Long.parseLong(lastUpString);
				if (delay > wait) {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			// No file or bad file:
			return true;
		}

		return false;
	}

	/**
	 * Notify that we checked for a new version of Fanfix.
	 */
	public static void setVersionChecked() {
		try {
			IOUtils.writeSmallFile(new File(configDir), "LAST_UPDATE",
					Long.toString(new Date().getTime()));
		} catch (IOException e) {
			tracer.error(e);
		}
	}

	/**
	 * The facility to use temporary files in this program.
	 * <p>
	 * <b>MUST</b> be closed at end of program.
	 * 
	 * @return the facility
	 */
	public static TempFiles getTempFiles() {
		return tempFiles;
	}

	/**
	 * The configuration directory (will check, in order of preference,
	 * {@link Bundles#getDirectory()}, the system properties, the environment
	 * and then defaults to $HOME/.fanfix).
	 * 
	 * @return the config directory
	 */
	private static String getConfigDir() {
		String configDir = Bundles.getDirectory();

		if (configDir == null) {
			configDir = System.getProperty("CONFIG_DIR");
		}

		if (configDir == null) {
			configDir = System.getenv("CONFIG_DIR");
		}

		if (configDir == null) {
			configDir = new File(getHome(), ".fanfix").getPath();
		}

		return configDir;
	}

	/**
	 * Create the config variables ({@link Instance#config},
	 * {@link Instance#uiconfig}, {@link Instance#trans} and
	 * {@link Instance#transGui}).
	 * 
	 * @param configDir
	 *            the directory where to find the configuration files
	 * @param refresh
	 *            TRUE to reset the configuration files from the default
	 *            included ones
	 */
	private static void createConfigs(String configDir, boolean refresh) {
		if (!refresh) {
			Bundles.setDirectory(configDir);
		}

		try {
			config = new ConfigBundle();
			config.updateFile(configDir);
		} catch (IOException e) {
			tracer.error(e);
		}

		try {
			uiconfig = new UiConfigBundle();
			uiconfig.updateFile(configDir);
		} catch (IOException e) {
			tracer.error(e);
		}

		// No updateFile for this one! (we do not want the user to have custom
		// translations that won't accept updates from newer versions)
		trans = new StringIdBundle(getLang());
		transGui = new StringIdGuiBundle(getLang());

		// Fix an old bug (we used to store custom translation files by
		// default):
		if (trans.getString(StringId.INPUT_DESC_CBZ) == null) {
			trans.deleteFile(configDir);
		}

		if (checkEnv("NOUTF")) {
			trans.setUnicode(false);
			transGui.setUnicode(false);
		}

		Bundles.setDirectory(configDir);
	}

	/**
	 * Create the default library as specified by the config.
	 * 
	 * @param remoteDir
	 *            the base remote directory if needed
	 * 
	 * @return the default {@link BasicLibrary}
	 */
	private static BasicLibrary createDefaultLibrary(File remoteDir) {
		BasicLibrary lib = null;

		String remoteLib = config.getString(Config.DEFAULT_LIBRARY);
		if (remoteLib == null || remoteLib.trim().isEmpty()) {
			String libDir = System.getProperty("fanfix.libdir");
			if (libDir == null || libDir.isEmpty()) {
				libDir = config.getString(Config.LIBRARY_DIR);
			}
			try {
				lib = new LocalLibrary(getFile(libDir));
			} catch (Exception e) {
				tracer.error(new IOException(
						"Cannot create library for directory: "
								+ getFile(libDir), e));
			}
		} else {
			int pos = remoteLib.lastIndexOf(":");
			if (pos >= 0) {
				String port = remoteLib.substring(pos + 1).trim();
				remoteLib = remoteLib.substring(0, pos);
				pos = remoteLib.lastIndexOf(":");
				if (pos >= 0) {
					String host = remoteLib.substring(pos + 1).trim();
					String key = remoteLib.substring(0, pos).trim();

					try {
						tracer.trace("Selecting remote library " + host + ":"
								+ port);
						lib = new RemoteLibrary(key, host,
								Integer.parseInt(port));
						lib = new CacheLibrary(getRemoteDir(remoteDir, host),
								lib);

					} catch (Exception e) {
					}
				}
			}

			if (lib == null) {
				tracer.error(new IOException(
						"Cannot create remote library for: " + remoteLib));
			}
		}

		return lib;
	}

	/**
	 * Return a path, but support the special $HOME variable.
	 * 
	 * @return the path
	 */
	private static File getFile(Config id) {
		return getFile(config.getString(id));
	}

	/**
	 * Return a path, but support the special $HOME variable.
	 * 
	 * @return the path
	 */
	private static File getFile(UiConfig id) {
		return getFile(uiconfig.getString(id));
	}

	/**
	 * Return a path, but support the special $HOME variable.
	 * 
	 * @return the path
	 */
	private static File getFile(String path) {
		File file = null;
		if (path != null && !path.isEmpty()) {
			path = path.replace('/', File.separatorChar);
			if (path.contains("$HOME")) {
				path = path.replace("$HOME", getHome());
			}

			file = new File(path);
		}

		return file;
	}

	/**
	 * Return the home directory from the system properties.
	 * 
	 * @return the home
	 */
	private static String getHome() {
		String home = System.getProperty("fanfix.home");
		if (home != null && new File(home).isFile()) {
			home = null;
		}

		if (home == null || home.trim().isEmpty()) {
			home = System.getProperty("user.home");
			if (!new File(home).isDirectory()) {
				home = null;
			}
		}

		if (home == null || home.trim().isEmpty()) {
			home = System.getProperty("java.io.tmpdir");
			if (!new File(home).isDirectory()) {
				home = null;
			}
		}

		if (home == null) {
			home = "";
		}

		return home;
	}

	/**
	 * The language to use for the application (NULL = default system language).
	 * 
	 * @return the language
	 */
	private static String getLang() {
		String lang = config.getString(Config.LANG);

		if (lang == null | lang.isEmpty()) {
			if (System.getenv("LANG") != null
					&& !System.getenv("LANG").isEmpty()) {
				lang = System.getenv("LANG");
			}
		}

		if (lang != null && lang.isEmpty()) {
			lang = null;
		}

		return lang;
	}

	/**
	 * Check that the given environment variable is "enabled".
	 * 
	 * @param key
	 *            the variable to check
	 * 
	 * @return TRUE if it is
	 */
	private static boolean checkEnv(String key) {
		String value = System.getenv(key);
		if (value != null) {
			value = value.trim().toLowerCase();
			if ("yes".equals(value) || "true".equals(value)
					|| "on".equals(value) || "1".equals(value)
					|| "y".equals(value)) {
				return true;
			}
		}

		return false;
	}
}
