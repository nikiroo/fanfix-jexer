package be.nikiroo.fanfix;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.library.BasicLibrary;
import be.nikiroo.fanfix.library.CacheLibrary;
import be.nikiroo.fanfix.library.LocalLibrary;
import be.nikiroo.fanfix.library.RemoteLibrary;
import be.nikiroo.fanfix.library.RemoteLibraryServer;
import be.nikiroo.fanfix.output.BasicOutput;
import be.nikiroo.fanfix.output.BasicOutput.OutputType;
import be.nikiroo.fanfix.reader.BasicReader;
import be.nikiroo.fanfix.reader.Reader;
import be.nikiroo.fanfix.reader.Reader.ReaderType;
import be.nikiroo.fanfix.searchable.BasicSearchable;
import be.nikiroo.fanfix.supported.BasicSupport;
import be.nikiroo.fanfix.supported.SupportType;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.Version;
import be.nikiroo.utils.serial.server.ServerObject;

/**
 * Main program entry point.
 * 
 * @author niki
 */
public class Main {
	private enum MainAction {
		IMPORT, EXPORT, CONVERT, READ, READ_URL, LIST, HELP, SET_READER, START, VERSION, SERVER, STOP_SERVER, REMOTE, SET_SOURCE, SET_TITLE, SET_AUTHOR, SEARCH, SEARCH_TAG
	}

	/**
	 * Main program entry point.
	 * <p>
	 * Known environment variables:
	 * <ul>
	 * <li>NOUTF: if set to 1 or 'true', the program will prefer non-unicode
	 * {@link String}s when possible</li>
	 * <li>CONFIG_DIR: a path where to look for the <tt>.properties</tt> files
	 * before taking the usual ones; they will also be saved/updated into this
	 * path when the program starts</li>
	 * <li>DEBUG: if set to 1 or 'true', the program will override the DEBUG_ERR
	 * configuration value with 'true'</li>
	 * </ul>
	 * <p>
	 * <ul>
	 * <li>--import [URL]: import into library</li>
	 * <li>--export [id] [output_type] [target]: export story to target</li>
	 * <li>--convert [URL] [output_type] [target] (+info): convert URL into
	 * target</li>
	 * <li>--read [id] ([chapter number]): read the given story from the library
	 * </li>
	 * <li>--read-url [URL] ([chapter number]): convert on the fly and read the
	 * story, without saving it</li>
	 * <li>--search: list the supported websites (where)</li>
	 * <li>--search [where] [keywords] (page [page]) (item [item]): search on
	 * the supported website and display the given results page of stories it
	 * found, or the story details if asked</li>
	 * <li>--search-tag [where]: list all the tags supported by this website</li>
	 * <li>--search-tag [index 1]... (page [page]) (item [item]): search for the
	 * given stories or subtags, tag by tag, and display information about a
	 * specific page of results or about a specific item if requested</li>
	 * <li>--list ([type]): list the stories present in the library</li>
	 * <li>--set-source [id] [new source]: change the source of the given story</li>
	 * <li>--set-title [id] [new title]: change the title of the given story</li>
	 * <li>--set-author [id] [new author]: change the author of the given story</li>
	 * <li>--set-reader [reader type]: set the reader type to CLI, TUI or LOCAL
	 * for this command</li>
	 * <li>--version: get the version of the program</li>
	 * <li>--server: start the server mode (see config file for parameters)</li>
	 * <li>--stop-server: stop the running server on this port if any</li>
	 * <li>--remote [key] [host] [port]: use a the given remote library</li>
	 * </ul>
	 * 
	 * @param args
	 *            see method description
	 */
	public static void main(String[] args) {
		// Only one line, but very important:
		Instance.init();

		String urlString = null;
		String luid = null;
		String sourceString = null;
		String titleString = null;
		String authorString = null;
		String chapString = null;
		String target = null;
		String key = null;
		MainAction action = MainAction.START;
		Boolean plusInfo = null;
		String host = null;
		Integer port = null;
		SupportType searchOn = null;
		String search = null;
		List<Integer> tags = new ArrayList<Integer>();
		Integer page = null;
		Integer item = null;

		boolean noMoreActions = false;

		int exitCode = 0;
		for (int i = 0; exitCode == 0 && i < args.length; i++) {
			// Action (--) handling:
			if (!noMoreActions && args[i].startsWith("--")) {
				if (args[i].equals("--")) {
					noMoreActions = true;
				} else {
					try {
						action = MainAction.valueOf(args[i].substring(2)
								.toUpperCase().replace("-", "_"));
					} catch (Exception e) {
						Instance.getInstance().getTraceHandler()
								.error(new IllegalArgumentException("Unknown action: " + args[i], e));
						exitCode = 255;
					}
				}

				continue;
			}

			switch (action) {
			case IMPORT:
				if (urlString == null) {
					urlString = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case EXPORT:
				if (luid == null) {
					luid = args[i];
				} else if (sourceString == null) {
					sourceString = args[i];
				} else if (target == null) {
					target = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case CONVERT:
				if (urlString == null) {
					urlString = args[i];
				} else if (sourceString == null) {
					sourceString = args[i];
				} else if (target == null) {
					target = args[i];
				} else if (plusInfo == null) {
					if ("+info".equals(args[i])) {
						plusInfo = true;
					} else {
						exitCode = 255;
					}
				} else {
					exitCode = 255;
				}
				break;
			case LIST:
				if (sourceString == null) {
					sourceString = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case SET_SOURCE:
				if (luid == null) {
					luid = args[i];
				} else if (sourceString == null) {
					sourceString = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case SET_TITLE:
				if (luid == null) {
					luid = args[i];
				} else if (sourceString == null) {
					titleString = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case SET_AUTHOR:
				if (luid == null) {
					luid = args[i];
				} else if (sourceString == null) {
					authorString = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case READ:
				if (luid == null) {
					luid = args[i];
				} else if (chapString == null) {
					chapString = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case READ_URL:
				if (urlString == null) {
					urlString = args[i];
				} else if (chapString == null) {
					chapString = args[i];
				} else {
					exitCode = 255;
				}
				break;
			case SEARCH:
				if (searchOn == null) {
					searchOn = SupportType.valueOfAllOkUC(args[i]);

					if (searchOn == null) {
						Instance.getInstance().getTraceHandler().error("Website not known: <" + args[i] + ">");
						exitCode = 41;
						break;
					}

					if (BasicSearchable.getSearchable(searchOn) == null) {
						Instance.getInstance().getTraceHandler().error("Website not supported: " + searchOn);
						exitCode = 42;
						break;
					}
				} else if (search == null) {
					search = args[i];
				} else if (page != null && page == -1) {
					try {
						page = Integer.parseInt(args[i]);
					} catch (Exception e) {
						page = -2;
					}
				} else if (item != null && item == -1) {
					try {
						item = Integer.parseInt(args[i]);
					} catch (Exception e) {
						item = -2;
					}
				} else if (page == null || item == null) {
					if (page == null && "page".equals(args[i])) {
						page = -1;
					} else if (item == null && "item".equals(args[i])) {
						item = -1;
					} else {
						exitCode = 255;
					}
				} else {
					exitCode = 255;
				}
				break;
			case SEARCH_TAG:
				if (searchOn == null) {
					searchOn = SupportType.valueOfAllOkUC(args[i]);

					if (searchOn == null) {
						Instance.getInstance().getTraceHandler().error("Website not known: <" + args[i] + ">");
						exitCode = 255;
					}

					if (BasicSearchable.getSearchable(searchOn) == null) {
						Instance.getInstance().getTraceHandler().error("Website not supported: " + searchOn);
						exitCode = 255;
					}
				} else if (page == null && item == null) {
					if ("page".equals(args[i])) {
						page = -1;
					} else if ("item".equals(args[i])) {
						item = -1;
					} else {
						try {
							int index = Integer.parseInt(args[i]);
							tags.add(index);
						} catch (NumberFormatException e) {
							Instance.getInstance().getTraceHandler().error("Invalid tag index: " + args[i]);
							exitCode = 255;
						}
					}
				} else if (page != null && page == -1) {
					try {
						page = Integer.parseInt(args[i]);
					} catch (Exception e) {
						page = -2;
					}
				} else if (item != null && item == -1) {
					try {
						item = Integer.parseInt(args[i]);
					} catch (Exception e) {
						item = -2;
					}
				} else if (page == null || item == null) {
					if (page == null && "page".equals(args[i])) {
						page = -1;
					} else if (item == null && "item".equals(args[i])) {
						item = -1;
					} else {
						exitCode = 255;
					}
				} else {
					exitCode = 255;
				}
				break;
			case HELP:
				exitCode = 255;
				break;
			case SET_READER:
				exitCode = setReaderType(args[i]);
				action = MainAction.START;
				break;
			case START:
				exitCode = 255; // not supposed to be selected by user
				break;
			case VERSION:
				exitCode = 255; // no arguments for this option
				break;
			case SERVER:
				exitCode = 255; // no arguments for this option
				break;
			case STOP_SERVER:
				exitCode = 255; // no arguments for this option
				break;
			case REMOTE:
				if (key == null) {
					key = args[i];
				} else if (host == null) {
					host = args[i];
				} else if (port == null) {
					port = Integer.parseInt(args[i]);

					BasicLibrary lib = new RemoteLibrary(key, host, port);
					lib = new CacheLibrary(Instance.getInstance().getRemoteDir(host), lib,
							Instance.getInstance().getUiConfig());

					BasicReader.setDefaultLibrary(lib);

					action = MainAction.START;
				} else {
					exitCode = 255;
				}
				break;
			}
		}

		final Progress mainProgress = new Progress(0, 80);
		mainProgress.addProgressListener(new Progress.ProgressListener() {
			private int current = mainProgress.getMin();

			@Override
			public void progress(Progress progress, String name) {
				int diff = progress.getProgress() - current;
				current += diff;

				if (diff <= 0)
					return;

				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < diff; i++) {
					builder.append('.');
				}

				System.err.print(builder.toString());

				if (progress.isDone()) {
					System.err.println("");
				}
			}
		});
		Progress pg = new Progress();
		mainProgress.addProgress(pg, mainProgress.getMax());

		VersionCheck updates = VersionCheck.check();
		if (updates.isNewVersionAvailable()) {
			// Sent to syserr so not to cause problem if one tries to capture a
			// story content in text mode
			System.err
					.println("A new version of the program is available at https://github.com/nikiroo/fanfix/releases");
			System.err.println("");
			for (Version v : updates.getNewer()) {
				System.err.println("\tVersion " + v);
				System.err.println("\t-------------");
				System.err.println("");
				for (String it : updates.getChanges().get(v)) {
					System.err.println("\t- " + it);
				}
				System.err.println("");
			}
		}

		if (exitCode == 0) {
			switch (action) {
			case IMPORT:
				exitCode = imprt(urlString, pg);
				updates.ok(); // we consider it read
				break;
			case EXPORT:
				exitCode = export(luid, sourceString, target, pg);
				updates.ok(); // we consider it read
				break;
			case CONVERT:
				exitCode = convert(urlString, sourceString, target,
						plusInfo == null ? false : plusInfo, pg);
				updates.ok(); // we consider it read
				break;
			case LIST:
				if (BasicReader.getReader() == null) {
					Instance.getInstance().getTraceHandler().error(new Exception("No reader type has been configured"));
					exitCode = 10;
					break;
				}
				exitCode = list(sourceString);
				break;
			case SET_SOURCE:
				try {
					Instance.getInstance().getLibrary().changeSource(luid, sourceString, pg);
				} catch (IOException e1) {
					Instance.getInstance().getTraceHandler().error(e1);
					exitCode = 21;
				}
				break;
			case SET_TITLE:
				try {
					Instance.getInstance().getLibrary().changeTitle(luid, titleString, pg);
				} catch (IOException e1) {
					Instance.getInstance().getTraceHandler().error(e1);
					exitCode = 22;
				}
				break;
			case SET_AUTHOR:
				try {
					Instance.getInstance().getLibrary().changeAuthor(luid, authorString, pg);
				} catch (IOException e1) {
					Instance.getInstance().getTraceHandler().error(e1);
					exitCode = 23;
				}
				break;
			case READ:
				if (BasicReader.getReader() == null) {
					Instance.getInstance().getTraceHandler().error(new Exception("No reader type has been configured"));
					exitCode = 10;
					break;
				}
				exitCode = read(luid, chapString, true);
				break;
			case READ_URL:
				if (BasicReader.getReader() == null) {
					Instance.getInstance().getTraceHandler().error(new Exception("No reader type has been configured"));
					exitCode = 10;
					break;
				}
				exitCode = read(urlString, chapString, false);
				break;
			case SEARCH:
				page = page == null ? 1 : page;
				if (page < 0) {
					Instance.getInstance().getTraceHandler().error("Incorrect page number");
					exitCode = 255;
					break;
				}

				item = item == null ? 0 : item;
				if (item < 0) {
					Instance.getInstance().getTraceHandler().error("Incorrect item number");
					exitCode = 255;
					break;
				}

				if (BasicReader.getReader() == null) {
					Instance.getInstance().getTraceHandler().error(new Exception("No reader type has been configured"));
					exitCode = 10;
					break;
				}

				try {
					if (searchOn == null) {
						BasicReader.getReader().search(true);
					} else if (search != null) {

						BasicReader.getReader().search(searchOn, search, page,
								item, true);
					} else {
						exitCode = 255;
					}
				} catch (IOException e1) {
					Instance.getInstance().getTraceHandler().error(e1);
					exitCode = 20;
				}

				break;
			case SEARCH_TAG:
				if (searchOn == null) {
					exitCode = 255;
					break;
				}

				page = page == null ? 1 : page;
				if (page < 0) {
					Instance.getInstance().getTraceHandler().error("Incorrect page number");
					exitCode = 255;
					break;
				}

				item = item == null ? 0 : item;
				if (item < 0) {
					Instance.getInstance().getTraceHandler().error("Incorrect item number");
					exitCode = 255;
					break;
				}

				if (BasicReader.getReader() == null) {
					Instance.getInstance().getTraceHandler().error(new Exception("No reader type has been configured"));
					exitCode = 10;
					break;
				}

				try {
					BasicReader.getReader().searchTag(searchOn, page, item,
							true, tags.toArray(new Integer[] {}));
				} catch (IOException e1) {
					Instance.getInstance().getTraceHandler().error(e1);
				}

				break;
			case HELP:
				syntax(true);
				exitCode = 0;
				break;
			case SET_READER:
				exitCode = 255;
				break;
			case VERSION:
				System.out
						.println(String.format("Fanfix version %s"
								+ "%nhttps://github.com/nikiroo/fanfix/"
								+ "%n\tWritten by Nikiroo",
								Version.getCurrentVersion()));
				updates.ok(); // we consider it read
				break;
			case START:
				if (BasicReader.getReader() == null) {
					Instance.getInstance().getTraceHandler().error(new Exception("No reader type has been configured"));
					exitCode = 10;
					break;
				}
				try {
					BasicReader.getReader().browse(null);
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler().error(e);
					exitCode = 66;
				}
				break;
			case SERVER:
				key = Instance.getInstance().getConfig().getString(Config.SERVER_KEY);
				port = Instance.getInstance().getConfig().getInteger(Config.SERVER_PORT);
				if (port == null) {
					System.err.println("No port configured in the config file");
					exitCode = 15;
					break;
				}
				try {
					ServerObject server = new RemoteLibraryServer(key, port);
					server.setTraceHandler(Instance.getInstance().getTraceHandler());
					server.run();
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler().error(e);
				}
				return;
			case STOP_SERVER:
				// Can be given via "--remote XX XX XX"
				if (key == null)
					key = Instance.getInstance().getConfig().getString(Config.SERVER_KEY);
				if (port == null)
					port = Instance.getInstance().getConfig().getInteger(Config.SERVER_PORT);

				if (port == null) {
					System.err.println("No port given nor configured in the config file");
					exitCode = 15;
					break;
				}
				try {
					new RemoteLibrary(key, host, port).exit();
				} catch (SSLException e) {
					Instance.getInstance().getTraceHandler().error(
							"Bad access key for remote library");
					exitCode = 43;
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler().error(e);
					exitCode = 44;
				}

				break;
			case REMOTE:
				exitCode = 255; // should not be reachable (REMOTE -> START)
				break;
			}
		}

		try {
			Instance.getInstance().getTempFiles().close();
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(new IOException("Cannot dispose of the temporary files", e));
		}

		if (exitCode == 255) {
			syntax(false);
		}

		System.exit(exitCode);
	}

	/**
	 * Import the given resource into the {@link LocalLibrary}.
	 * 
	 * @param urlString
	 *            the resource to import
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the exit return code (0 = success)
	 */
	public static int imprt(String urlString, Progress pg) {
		try {
			MetaData meta = Instance.getInstance().getLibrary().imprt(BasicReader.getUrl(urlString), pg);
			System.out.println(meta.getLuid() + ": \"" + meta.getTitle() + "\" imported.");
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(e);
			return 1;
		}

		return 0;
	}

	/**
	 * Export the {@link Story} from the {@link LocalLibrary} to the given
	 * target.
	 * 
	 * @param luid
	 *            the story LUID
	 * @param typeString
	 *            the {@link OutputType} to use
	 * @param target
	 *            the target
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the exit return code (0 = success)
	 */
	public static int export(String luid, String typeString, String target,
			Progress pg) {
		OutputType type = OutputType.valueOfNullOkUC(typeString, null);
		if (type == null) {
			Instance.getInstance().getTraceHandler().error(new Exception(trans(StringId.OUTPUT_DESC, typeString)));
			return 1;
		}

		try {
			Instance.getInstance().getLibrary().export(luid, type, target, pg);
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(e);
			return 4;
		}

		return 0;
	}

	/**
	 * List the stories of the given source from the {@link LocalLibrary}
	 * (unless NULL is passed, in which case all stories will be listed).
	 * 
	 * @param source
	 *            the source to list the known stories of, or NULL to list all
	 *            stories
	 * 
	 * @return the exit return code (0 = success)
	 */
	private static int list(String source) {
		BasicReader.setDefaultReaderType(ReaderType.CLI);
		try {
			BasicReader.getReader().browse(source);
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(e);
			return 66;
		}

		return 0;
	}

	/**
	 * Start the current reader for this {@link Story}.
	 * 
	 * @param story
	 *            the LUID of the {@link Story} in the {@link LocalLibrary}
	 *            <b>or</b> the {@link Story} {@link URL}
	 * @param chapString
	 *            which {@link Chapter} to read (starting at 1), or NULL to get
	 *            the {@link Story} description
	 * @param library
	 *            TRUE if the source is the {@link Story} LUID, FALSE if it is a
	 *            {@link URL}
	 * 
	 * @return the exit return code (0 = success)
	 */
	private static int read(String story, String chapString, boolean library) {
		try {
			Reader reader = BasicReader.getReader();
			if (library) {
				reader.setMeta(story);
			} else {
				reader.setMeta(BasicReader.getUrl(story), null);
			}

			if (chapString != null) {
				try {
					reader.setChapter(Integer.parseInt(chapString));
					reader.read(true);
				} catch (NumberFormatException e) {
					Instance.getInstance().getTraceHandler()
							.error(new IOException("Chapter number cannot be parsed: " + chapString, e));
					return 2;
				}
			} else {
				reader.read(true);
			}
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(e);
			return 1;
		}

		return 0;
	}

	/**
	 * Convert the {@link Story} into another format.
	 * 
	 * @param urlString
	 *            the source {@link Story} to convert
	 * @param typeString
	 *            the {@link OutputType} to convert to
	 * @param target
	 *            the target file
	 * @param infoCover
	 *            TRUE to also export the cover and info file, even if the given
	 *            {@link OutputType} does not usually save them
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the exit return code (0 = success)
	 */
	public static int convert(String urlString, String typeString,
			String target, boolean infoCover, Progress pg) {
		int exitCode = 0;

		Instance.getInstance().getTraceHandler().trace("Convert: " + urlString);
		String sourceName = urlString;
		try {
			URL source = BasicReader.getUrl(urlString);
			sourceName = source.toString();
			if (sourceName.startsWith("file://")) {
				sourceName = sourceName.substring("file://".length());
			}

			OutputType type = OutputType.valueOfAllOkUC(typeString, null);
			if (type == null) {
				Instance.getInstance().getTraceHandler()
						.error(new IOException(trans(StringId.ERR_BAD_OUTPUT_TYPE, typeString)));

				exitCode = 2;
			} else {
				try {
					BasicSupport support = BasicSupport.getSupport(source);

					if (support != null) {
						Instance.getInstance().getTraceHandler().trace("Support found: " + support.getClass());
						Progress pgIn = new Progress();
						Progress pgOut = new Progress();
						if (pg != null) {
							pg.setMax(2);
							pg.addProgress(pgIn, 1);
							pg.addProgress(pgOut, 1);
						}

						Story story = support.process(pgIn);
						try {
							target = new File(target).getAbsolutePath();
							BasicOutput.getOutput(type, infoCover, infoCover).process(story, target, pgOut);
						} catch (IOException e) {
							Instance.getInstance().getTraceHandler()
									.error(new IOException(trans(StringId.ERR_SAVING, target), e));
							exitCode = 5;
						}
					} else {
						Instance.getInstance().getTraceHandler()
								.error(new IOException(trans(	StringId.ERR_NOT_SUPPORTED, source)));

						exitCode = 4;
					}
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler()
							.error(new IOException(trans(StringId.ERR_LOADING, sourceName), e));
					exitCode = 3;
				}
			}
		} catch (MalformedURLException e) {
			Instance.getInstance().getTraceHandler().error(new IOException(trans(StringId.ERR_BAD_URL, sourceName), e));
			exitCode = 1;
		}

		return exitCode;
	}

	/**
	 * Simple shortcut method to call {link Instance#getTrans()#getString()}.
	 * 
	 * @param id
	 *            the ID to translate
	 * 
	 * @return the translated result
	 */
	private static String trans(StringId id, Object... params) {
		return Instance.getInstance().getTrans().getString(id, params);
	}

	/**
	 * Display the correct syntax of the program to the user to stdout, or an
	 * error message if the syntax used was wrong on stderr.
	 * 
	 * @param showHelp
	 *            TRUE to show the syntax help, FALSE to show "syntax error"
	 */
	private static void syntax(boolean showHelp) {
		if (showHelp) {
			StringBuilder builder = new StringBuilder();
			for (SupportType type : SupportType.values()) {
				builder.append(trans(StringId.ERR_SYNTAX_TYPE, type.toString(),
						type.getDesc()));
				builder.append('\n');
			}

			String typesIn = builder.toString();
			builder.setLength(0);

			for (OutputType type : OutputType.values()) {
				builder.append(trans(StringId.ERR_SYNTAX_TYPE, type.toString(),
						type.getDesc(true)));
				builder.append('\n');
			}

			String typesOut = builder.toString();

			System.out.println(trans(StringId.HELP_SYNTAX, typesIn, typesOut));
		} else {
			System.err.println(trans(StringId.ERR_SYNTAX));
		}
	}

	/**
	 * Set the default reader type for this session only (it can be changed in
	 * the configuration file, too, but this value will override it).
	 * 
	 * @param readerTypeString
	 *            the type
	 */
	private static int setReaderType(String readerTypeString) {
		try {
			ReaderType readerType = ReaderType.valueOf(readerTypeString
					.toUpperCase());
			BasicReader.setDefaultReaderType(readerType);
			return 0;
		} catch (IllegalArgumentException e) {
			Instance.getInstance().getTraceHandler()
					.error(new IOException("Unknown reader type: " + readerTypeString,	e));
			return 1;
		}
	}
}
