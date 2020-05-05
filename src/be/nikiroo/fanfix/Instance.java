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
import be.nikiroo.utils.Image;
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
	static private Instance instance;
	static private Object instancelock = new Object();

	private ConfigBundle config;
	private UiConfigBundle uiconfig;
	private StringIdBundle trans;
	private DataLoader cache;
	private StringIdGuiBundle transGui;
	private BasicLibrary lib;
	private File coverDir;
	private File readerTmp;
	private File remoteDir;
	private String configDir;
	private TraceHandler tracer;
	private TempFiles tempFiles;

	/**
	 * Initialise the instance -- if already initialised, nothing will happen.
	 * <p>
	 * Before calling this method, you may call
	 * {@link Bundles#setDirectory(String)} if wanted.
	 * <p>
	 * Note that this method will honour some environment variables, the 3 most
	 * important ones probably being:
	 * <ul>
	 * <li><tt>DEBUG</tt>: will enable DEBUG output if set to 1 (or Y or TRUE or
	 * ON, case insensitive)</li>
	 * <li><tt>CONFIG_DIR</tt>: will use this directory as configuration
	 * directory (supports $HOME notation, defaults to $HOME/.fanfix</li>
	 * <li><tt>BOOKS_DIR</tt>: will use this directory as library directory
	 * (supports $HOME notation, defaults to $HOME/Books</li>
	 * </ul>
	 */
	static public void init() {
		init(false);
	}

	/**
	 * Initialise the instance -- if already initialised, nothing will happen unless
	 * you pass TRUE to <tt>force</tt>.
	 * <p>
	 * Before calling this method, you may call {@link Bundles#setDirectory(String)}
	 * if wanted.
	 * <p>
	 * Note: forcing the initialisation can be dangerous, so make sure to only make
	 * it under controlled circumstances -- for instance, at the start of the
	 * program, you could call {@link Instance#init()}, change some settings because
	 * you want to force those settings (it will also forbid users to change them!)
	 * and then call {@link Instance#init(boolean)} with <tt>force</tt> set to TRUE.
	 * 
	 * @param force force the initialisation even if already initialised
	 */
	static public void init(boolean force) {
		synchronized (instancelock) {
			if (instance == null || force) {
				instance = new Instance();
			}
		}

	}

	/**
	 * Force-initialise the {@link Instance} to a known value.
	 * <p>
	 * Usually for DEBUG/Test purposes.
	 * 
	 * @param instance the actual Instance to use
	 */
	static public void init(Instance instance) {
		Instance.instance = instance;
	}

	/**
	 * The (mostly unique) instance of this {@link Instance}.
	 * 
	 * @return the (mostly unique) instance
	 */
	public static Instance getInstance() {
		return instance;
	}

	/**
	 * Actually initialise the instance.
	 * <p>
	 * Before calling this method, you may call {@link Bundles#setDirectory(String)}
	 * if wanted.
	 */
	protected Instance() {
		// Before we can configure it:
		Boolean debug = checkEnv("DEBUG");
		boolean trace = debug != null && debug;
		tracer = new TraceHandler(true, trace, trace);

		// config dir:
		configDir = getConfigDir();
		if (!new File(configDir).exists()) {
			new File(configDir).mkdirs();
		}

		// Most of the rest is dependent upon this:
		createConfigs(configDir, false);

		// Proxy support
		Proxy.use(config.getString(Config.NETWORK_PROXY));

		// update tracer:
		if (debug == null) {
			debug = config.getBoolean(Config.DEBUG_ERR, false);
			trace = config.getBoolean(Config.DEBUG_TRACE, false);
		}

		tracer = new TraceHandler(true, debug, trace);

		// default Library
		remoteDir = new File(configDir, "remote");
		lib = createDefaultLibrary(remoteDir);

		// create cache and TMP
		File tmp = getFile(Config.CACHE_DIR, configDir, "tmp");
		Image.setTemporaryFilesRoot(new File(tmp.getParent(), "tmp.images"));

		String ua = config.getString(Config.NETWORK_USER_AGENT, "");
		try {
			int hours = config.getInteger(Config.CACHE_MAX_TIME_CHANGING, 0);
			int hoursLarge = config.getInteger(Config.CACHE_MAX_TIME_STABLE, 0);
			cache = new DataLoader(tmp, ua, hours, hoursLarge);
		} catch (IOException e) {
			tracer.error(new IOException("Cannot create cache (will continue without cache)", e));
			cache = new DataLoader(ua);
		}

		cache.setTraceHandler(tracer);

		// readerTmp / coverDir
		readerTmp = getFile(UiConfig.CACHE_DIR_LOCAL_READER, configDir, "tmp-reader");
		coverDir = getFile(Config.DEFAULT_COVERS_DIR, configDir, "covers");
		coverDir.mkdirs();

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
	public TraceHandler getTraceHandler() {
		return tracer;
	}

	/**
	 * The traces handler for this {@link Cache}.
	 * 
	 * @param tracer the new traces handler or NULL
	 */
	public void setTraceHandler(TraceHandler tracer) {
		if (tracer == null) {
			tracer = new TraceHandler(false, false, false);
		}

		this.tracer = tracer;
		cache.setTraceHandler(tracer);
	}

	/**
	 * Get the (unique) configuration service for the program.
	 * 
	 * @return the configuration service
	 */
	public ConfigBundle getConfig() {
		return config;
	}

	/**
	 * Get the (unique) UI configuration service for the program.
	 * 
	 * @return the configuration service
	 */
	public UiConfigBundle getUiConfig() {
		return uiconfig;
	}

	/**
	 * Reset the configuration.
	 * 
	 * @param resetTrans also reset the translation files
	 */
	public void resetConfig(boolean resetTrans) {
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
	public DataLoader getCache() {
		return cache;
	}

	/**
	 * Get the (unique) {link StringIdBundle} for the program.
	 * <p>
	 * This is used for the translations of the core parts of Fanfix.
	 * 
	 * @return the {link StringIdBundle}
	 */
	public StringIdBundle getTrans() {
		return trans;
	}

	/**
	 * Get the (unique) {link StringIdGuiBundle} for the program.
	 * <p>
	 * This is used for the translations of the GUI parts of Fanfix.
	 * 
	 * @return the {link StringIdGuiBundle}
	 */
	public StringIdGuiBundle getTransGui() {
		return transGui;
	}

	/**
	 * Get the (unique) {@link BasicLibrary} for the program.
	 * 
	 * @return the {@link BasicLibrary}
	 */
	public BasicLibrary getLibrary() {
		if (lib == null) {
			throw new NullPointerException("We don't have a library to return");
		}

		return lib;
	}

	/**
	 * Change the default {@link BasicLibrary} for this program.
	 * <p>
	 * Be careful.
	 * 
	 * @param lib
	 *            the new {@link BasicLibrary}
	 */
	public void setLibrary(BasicLibrary lib) {
		this.lib = lib;
	}

	/**
	 * Return the directory where to look for default cover pages.
	 * 
	 * @return the default covers directory
	 */
	public File getCoverDir() {
		return coverDir;
	}

	/**
	 * Return the directory where to store temporary files for the local reader.
	 * 
	 * @return the directory
	 */
	public File getReaderDir() {
		return readerTmp;
	}

	/**
	 * Return the directory where to store temporary files for the remote
	 * {@link LocalLibrary}.
	 * 
	 * @param host the remote for this host
	 * 
	 * @return the directory
	 */
	public File getRemoteDir(String host) {
		return getRemoteDir(remoteDir, host);
	}

	/**
	 * Return the directory where to store temporary files for the remote
	 * {@link LocalLibrary}.
	 * 
	 * @param remoteDir the base remote directory
	 * @param host      the remote for this host
	 * 
	 * @return the directory
	 */
	private File getRemoteDir(File remoteDir, String host) {
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
	public boolean isVersionCheckNeeded() {
		try {
			long wait = config.getInteger(Config.NETWORK_UPDATE_INTERVAL, 0) * 24 * 60 * 60 * 1000;
			if (wait >= 0) {
				String lastUpString = IOUtils.readSmallFile(new File(configDir, "LAST_UPDATE"));
				long delay = new Date().getTime() - Long.parseLong(lastUpString);
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
	public void setVersionChecked() {
		try {
			IOUtils.writeSmallFile(new File(configDir), "LAST_UPDATE", Long.toString(new Date().getTime()));
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
	public TempFiles getTempFiles() {
		return tempFiles;
	}

	/**
	 * The configuration directory (will check, in order of preference, the system
	 * properties, the environment and then defaults to
	 * {@link Instance#getHome()}/.fanfix).
	 * 
	 * @return the config directory
	 */
	private String getConfigDir() {
		String configDir = System.getProperty("CONFIG_DIR");

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
	 * @param configDir the directory where to find the configuration files
	 * @param refresh   TRUE to reset the configuration files from the default
	 *                  included ones
	 */
	private void createConfigs(String configDir, boolean refresh) {
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

		Boolean noutf = checkEnv("NOUTF");
		if (noutf != null && noutf) {
			trans.setUnicode(false);
			transGui.setUnicode(false);
		}

		Bundles.setDirectory(configDir);
	}

	/**
	 * Create the default library as specified by the config.
	 * 
	 * @param remoteDir the base remote directory if needed
	 * 
	 * @return the default {@link BasicLibrary}
	 */
	private BasicLibrary createDefaultLibrary(File remoteDir) {
		BasicLibrary lib = null;

		boolean useRemote = config.getBoolean(Config.REMOTE_LIBRARY_ENABLED, false);
		if (useRemote) {
			String host = null;
			int port = -1;
			try {
				host = config.getString(Config.REMOTE_LIBRARY_HOST);
				port = config.getInteger(Config.REMOTE_LIBRARY_PORT, -1);
				String key = config.getString(Config.REMOTE_LIBRARY_KEY);

				tracer.trace("Selecting remote library " + host + ":" + port);
				lib = new RemoteLibrary(key, host, port);
				lib = new CacheLibrary(getRemoteDir(remoteDir, host), lib, uiconfig);
			} catch (Exception e) {
				tracer.error(new IOException("Cannot create remote library for: " + host + ":" + port, e));
			}
		} else {
			String libDir = System.getenv("BOOKS_DIR");
			if (libDir == null || libDir.isEmpty()) {
				libDir = getFile(Config.LIBRARY_DIR, configDir, "$HOME/Books").getPath();
			}
			try {
				lib = new LocalLibrary(new File(libDir), config);
			} catch (Exception e) {
				tracer.error(new IOException("Cannot create library for directory: " + libDir, e));
			}
		}

		return lib;
	}

	/**
	 * Return a path, but support the special $HOME variable.
	 * 
	 * @param id  the key for the path, which may contain "$HOME"
	 * @param configDir the directory to use as base if not absolute
	 * @param def the default value if none (will be configDir-rooted if needed)
	 * @return the path, with expanded "$HOME" if needed
	 */
	protected File getFile(Config id, String configDir, String def) {
		String path = config.getString(id, def);
		return getFile(path, configDir);
	}

	/**
	 * Return a path, but support the special $HOME variable.
	 * 
	 * @param id  the key for the path, which may contain "$HOME"
	 * @param configDir the directory to use as base if not absolute
	 * @param def the default value if none (will be configDir-rooted if needed)
	 * @return the path, with expanded "$HOME" if needed
	 */
	protected File getFile(UiConfig id, String configDir, String def) {
		String path = uiconfig.getString(id, def);
		return getFile(path, configDir);
	}

	/**
	 * Return a path, but support the special $HOME variable.
	 * 
	 * @param path the path, which may contain "$HOME"
	 * @param configDir the directory to use as base if not absolute
	 * @return the path, with expanded "$HOME" if needed
	 */
	protected File getFile(String path, String configDir) {
		File file = null;
		if (path != null && !path.isEmpty()) {
			path = path.replace('/', File.separatorChar);
			if (path.contains("$HOME")) {
				path = path.replace("$HOME", getHome());
			} else if (!path.startsWith("/")) {
				path = new File(configDir, path).getPath();
			}

			file = new File(path);
		}

		return file;
	}

	/**
	 * Return the home directory from the environment (FANFIX_DIR) or the system
	 * properties.
	 * <p>
	 * The environment variable is tested first. Then, the custom property
	 * "fanfix.home" is tried, followed by the usual "user.home" then "java.io.tmp"
	 * if nothing else is found.
	 * 
	 * @return the home
	 */
	protected String getHome() {
		String home = System.getenv("FANFIX_DIR");
		if (home != null && new File(home).isFile()) {
			home = null;
		}

		if (home == null || home.trim().isEmpty()) {
			home = System.getProperty("fanfix.home");
			if (home != null && new File(home).isFile()) {
				home = null;
			}
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
	protected String getLang() {
		String lang = config.getString(Config.LANG);

		if (lang == null || lang.isEmpty()) {
			if (System.getenv("LANG") != null && !System.getenv("LANG").isEmpty()) {
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
	 * @param key the variable to check
	 * 
	 * @return TRUE if it is
	 */
	protected Boolean checkEnv(String key) {
		String value = System.getenv(key);
		if (value != null) {
			value = value.trim().toLowerCase();
			if ("yes".equals(value) || "true".equals(value) || "on".equals(value) || "1".equals(value)
					|| "y".equals(value)) {
				return true;
			}

			return false;
		}

		return null;
	}
}
