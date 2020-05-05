package be.nikiroo.fanfix.bundles;

import be.nikiroo.utils.resources.Meta;
import be.nikiroo.utils.resources.Meta.Format;

/**
 * The configuration options.
 * 
 * @author niki
 */
@SuppressWarnings("javadoc")
public enum Config {
	
	// Note: all hidden values are subject to be removed in a later version
	
	@Meta(description = "The language to use for in the program (example: en-GB, fr-BE...) or nothing for default system language (can be overwritten with the variable $LANG)",//
	format = Format.LOCALE, list = { "en-GB", "fr-BE" })
	LANG, //
	@Meta(description = "The default reader type to use to read stories:\nCLI = simple output to console\nTUI = a Text User Interface with menus and windows, based upon Jexer\nGUI = a GUI with locally stored files, based upon Swing", //
	hidden = true, format = Format.FIXED_LIST, list = { "CLI", "GUI", "TUI" }, def = "GUI")
	READER_TYPE, //

	@Meta(description = "File format options",//
	group = true)
	FILE_FORMAT, //
	@Meta(description = "How to save non-images documents in the library",//
	format = Format.FIXED_LIST, list = { "INFO_TEXT", "EPUB", "HTML", "TEXT" }, def = "INFO_TEXT")
	FILE_FORMAT_NON_IMAGES_DOCUMENT_TYPE, //
	@Meta(description = "How to save images documents in the library",//
	format = Format.FIXED_LIST, list = { "CBZ", "HTML" }, def = "CBZ")
	FILE_FORMAT_IMAGES_DOCUMENT_TYPE, //
	@Meta(description = "How to save cover images",//
	format = Format.FIXED_LIST, list = { "PNG", "JPG", "BMP" }, def = "PNG")
	FILE_FORMAT_IMAGE_FORMAT_COVER, //
	@Meta(description = "How to save content images",//
	format = Format.FIXED_LIST, list = { "PNG", "JPG", "BMP" }, def = "JPG")
	FILE_FORMAT_IMAGE_FORMAT_CONTENT, //

	@Meta(description = "Cache management",//
	group = true)
	CACHE, //
	@Meta(description = "The directory where to store temporary files; any relative path uses the applciation config directory as base, $HOME notation is supported, / is always accepted as directory separator",//
	format = Format.DIRECTORY, def = "tmp/")
	CACHE_DIR, //
	@Meta(description = "The delay in hours after which a cached resource that is thought to change ~often is considered too old and triggers a refresh delay (or 0 for no cache, or -1 for infinite time)", //
	format = Format.INT, def = "24")
	CACHE_MAX_TIME_CHANGING, //
	@Meta(description = "The delay in hours after which a cached resource that is thought to change rarely is considered too old and triggers a refresh delay (or 0 for no cache, or -1 for infinite time)", //
	format = Format.INT, def = "720")
	CACHE_MAX_TIME_STABLE, //

	@Meta(description = "The directory where to get the default story covers; any relative path uses the applciation config directory as base, $HOME notation is supported, / is always accepted as directory separator",//
	format = Format.DIRECTORY, def = "covers/")
	DEFAULT_COVERS_DIR, //
	@Meta(description = "The directory where to store the library (can be overriden by the environment variable \"BOOKS_DIR\"; any relative path uses the applciation config directory as base, $HOME notation is supported, / is always accepted as directory separator",//
	format = Format.DIRECTORY, def = "$HOME/Books/")
	LIBRARY_DIR, //

	@Meta(description = "Remote library\nA remote library can be configured to fetch the stories from a remote Fanfix server",//
	group = true)
	REMOTE_LIBRARY, //
	@Meta(description = "Use the remote Fanfix server configured here instead of the local library (if FALSE, the local library will be used instead)",//
	format = Format.BOOLEAN, def = "false")
	REMOTE_LIBRARY_ENABLED, //
	@Meta(description = "The remote Fanfix server to connect to",//
	format = Format.STRING)
	REMOTE_LIBRARY_HOST, //
	@Meta(description = "The port to use for the remote Fanfix server",//
	format = Format.INT, def = "58365")
	REMOTE_LIBRARY_PORT, //
	@Meta(description = "The key is structured: \"KEY|SUBKEY|wl|rw\"\n- \"KEY\" is the actual encryption key (it can actually be empty, which will still encrypt the messages but of course it will be easier to guess the key)\n- \"SUBKEY\" is the (optional) subkey to use to get additional privileges\n- \"wl\" is a special privilege that allows that subkey to ignore white lists\n- \"rw\" is a special privilege that allows that subkey to modify the library, even if it is not in RW (by default) mode\n\nSome examples:\n- \"super-secret\": a normal key, no special privileges\n- \"you-will-not-guess|azOpd8|wl\": a white-list ignoring key\n- \"new-password|subpass|rw\": a key that allows modifications on the library",//
	format = Format.PASSWORD)
	REMOTE_LIBRARY_KEY, //

	@Meta(description = "Network configuration",//
	group = true)
	NETWORK, //
	@Meta(description = "The user-agent to use to download files",//
	def = "Mozilla/5.0 (X11; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0 -- ELinks/0.9.3 (Linux 2.6.11 i686; 80x24) -- Fanfix (https://github.com/nikiroo/fanfix/)")
	NETWORK_USER_AGENT, //
	@Meta(description = "The proxy server to use under the format 'user:pass@proxy:port', 'user@proxy:port', 'proxy:port' or ':' alone (system proxy); an empty String means no proxy",//
	format = Format.STRING, def = "")
	NETWORK_PROXY, //
	@Meta(description = "If the last update check was done at least that many days ago, check for updates at startup (-1 for 'no checks')", //
	format = Format.INT, def = "1")
	NETWORK_UPDATE_INTERVAL, //

	@Meta(description = "Remote Server configuration\nNote that the key is structured: \"KEY|SUBKEY|wl|rw\"\n- \"KEY\" is the actual encryption key (it can actually be empty, which will still encrypt the messages but of course it will be easier to guess the key)\n- \"SUBKEY\" is the (optional) subkey to use to get additional privileges\n- \"wl\" is a special privilege that allows that subkey to ignore white lists\n- \"rw\" is a special privilege that allows that subkey to modify the library, even if it is not in RW (by default) mode\n\nSome examples:\n- \"super-secret\": a normal key, no special privileges\n- \"you-will-not-guess|azOpd8|wl\": a white-list ignoring key\n- \"new-password|subpass|rw\": a key that allows modifications on the library",//
	group = true)
	SERVER, //
	@Meta(description = "The port on which we can start the server (must be a valid port, from 1 to 65535)", //
	format = Format.INT, def = "58365")
	SERVER_PORT, //
	@Meta(description = "The encryption key for the server (NOT including a subkey), it cannot contain the pipe character \"|\" but can be empty (it is *still* encrypted, but with an empty, easy to guess key)",//
	format = Format.PASSWORD, def = "")
	SERVER_KEY, //
	@Meta(description = "Allow write access to the clients (download story, move story...) without RW subkeys", //
	format = Format.BOOLEAN, def = "true")
	SERVER_RW, //
	@Meta(description = "If not empty, only the EXACT listed sources will be available for clients without BL subkeys",//
	array = true, format = Format.STRING, def = "")
	SERVER_WHITELIST, //
	@Meta(description = "The subkeys that the server will allow, including the modes\nA subkey ", //
	array = true, format = Format.STRING, def = "")
	SERVER_ALLOWED_SUBKEYS, //

	@Meta(description = "DEBUG options",//
	group = true)
	DEBUG, //
	@Meta(description = "Show debug information on errors",//
	format = Format.BOOLEAN, def = "false")
	DEBUG_ERR, //
	@Meta(description = "Show debug trace information",//
	format = Format.BOOLEAN, def = "false")
	DEBUG_TRACE, //

	@Meta(description = "Internal configuration\nThose options are internal to the program and should probably not be changed",//
	hidden = true, group = true)
	CONF, //
	@Meta(description = "LaTeX configuration",//
	hidden = true, group = true)
	CONF_LATEX_LANG, //
	@Meta(description = "LaTeX output language (full name) for \"English\"",//
	hidden = true, format = Format.STRING, def = "english")
	CONF_LATEX_LANG_EN, //
	@Meta(description = "LaTeX output language (full name) for \"French\"",//
	hidden = true, format = Format.STRING, def = "french")
	CONF_LATEX_LANG_FR, //
	@Meta(description = "other 'by' prefixes before author name, used to identify the author",//
	hidden = true, array = true, format = Format.STRING, def = "\"by\",\"par\",\"de\",\"©\",\"(c)\"")
	CONF_BYS, //
	@Meta(description = "List of languages codes used for chapter identification (should not be changed)", //
	hidden = true, array = true, format = Format.STRING, def = "\"EN\",\"FR\"")
	CONF_CHAPTER, //
	@Meta(description = "Chapter identification string in English, used to identify a starting chapter in text mode",//
	hidden = true, format = Format.STRING, def = "Chapter")
	CONF_CHAPTER_EN, //
	@Meta(description = "Chapter identification string in French, used to identify a starting chapter in text mode",//
	hidden = true, format = Format.STRING, def = "Chapitre")
	CONF_CHAPTER_FR, //

	@Meta(description = "YiffStar/SoFurry credentials\nYou can give your YiffStar credentials here to have access to all the stories, though it should not be necessary anymore (some stories used to beblocked for anonymous viewers)",//
	group = true)
	LOGIN_YIFFSTAR, //
	@Meta(description = "Your YiffStar/SoFurry login",//
	format = Format.STRING)
	LOGIN_YIFFSTAR_USER, //
	@Meta(description = "Your YiffStar/SoFurry password",//
	format = Format.PASSWORD)
	LOGIN_YIFFSTAR_PASS, //

	@Meta(description = "FimFiction APIKEY credentials\nFimFiction can be queried via an API, but requires an API key to do that. One has been created for this program, but if you have another API key you can set it here. You can also set a login and password instead, in that case, a new API key will be generated (and stored) if you still haven't set one.",//
	group = true)
	LOGIN_FIMFICTION_APIKEY, //
	@Meta(description = "The login of the API key used to create a new token from FimFiction", //
	format = Format.STRING)
	LOGIN_FIMFICTION_APIKEY_CLIENT_ID, //
	@Meta(description = "The password of the API key used to create a new token from FimFiction", //
	format = Format.PASSWORD)
	LOGIN_FIMFICTION_APIKEY_CLIENT_SECRET, //
	@Meta(description = "Do not use the new API, even if we have a token, and force HTML scraping",//
	format = Format.BOOLEAN, def = "false")
	LOGIN_FIMFICTION_APIKEY_FORCE_HTML, //
	@Meta(description = "The token required to use the beta APIv2 from FimFiction (see APIKEY_CLIENT_* if you want to generate a new one from your own API key)", //
	format = Format.PASSWORD, def = "Bearer WnZ5oHlzQoDocv1GcgHfcoqctHkSwL-D")
	LOGIN_FIMFICTION_APIKEY_TOKEN, //
	
	@Meta(description = "e621.net credentials\nYou can give your e621.net credentials here to have access to all the comics and ignore the default blacklist",//
	group = true)
	LOGIN_E621, //
	@Meta(description = "Your e621.net login",//
	format = Format.STRING)
	LOGIN_E621_LOGIN, //
	@Meta(description = "Your e621.net API KEY",//
	format = Format.PASSWORD)
	LOGIN_E621_APIKEY, //
}
