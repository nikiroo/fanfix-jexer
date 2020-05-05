package be.nikiroo.fanfix.library;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.Version;
import be.nikiroo.utils.serial.server.ConnectActionClientObject;

/**
 * This {@link BasicLibrary} will access a remote server to list the available
 * stories, and download the ones you try to load to the local directory
 * specified in the configuration.
 * 
 * @author niki
 */
public class RemoteLibrary extends BasicLibrary {
	interface RemoteAction {
		public void action(ConnectActionClientObject action) throws Exception;
	}

	class RemoteConnectAction extends ConnectActionClientObject {
		public RemoteConnectAction() throws IOException {
			super(host, port, key);
		}

		@Override
		public Object send(Object data) throws IOException,
				NoSuchFieldException, NoSuchMethodException,
				ClassNotFoundException {
			Object rep = super.send(data);
			if (rep instanceof RemoteLibraryException) {
				RemoteLibraryException remoteEx = (RemoteLibraryException) rep;
				throw remoteEx.unwrapException();
			}

			return rep;
		}
	}

	private String host;
	private int port;
	private final String key;
	private final String subkey;

	// informative only (server will make the actual checks)
	private boolean rw;

	/**
	 * Create a {@link RemoteLibrary} linked to the given server.
	 * <p>
	 * Note that the key is structured:
	 * <tt><b><i>xxx</i></b>(|<b><i>yyy</i></b>|<b>wl</b>)(|<b>rw</b>)</tt>
	 * <p>
	 * Note that anything before the first pipe (<tt>|</tt>) character is
	 * considered to be the encryption key, anything after that character is
	 * called the subkey (including the other pipe characters and flags!).
	 * <p>
	 * This is important because the subkey (including the pipe characters and
	 * flags) must be present as-is in the server configuration file to be
	 * allowed.
	 * <ul>
	 * <li><b><i>xxx</i></b>: the encryption key used to communicate with the
	 * server</li>
	 * <li><b><i>yyy</i></b>: the secondary key</li>
	 * <li><b>rw</b>: flag to allow read and write access if it is not the
	 * default on this server</li>
	 * <li><b>wl</b>: flag to allow access to all the stories (bypassing the
	 * whitelist if it exists)</li>
	 * </ul>
	 * <p>
	 * Some examples:
	 * <ul>
	 * <li><b>my_key</b>: normal connection, will take the default server
	 * options</li>
	 * <li><b>my_key|agzyzz|wl</b>: will ask to bypass the white list (if it
	 * exists)</li>
	 * <li><b>my_key|agzyzz|rw</b>: will ask read-write access (if the default
	 * is read-only)</li>
	 * <li><b>my_key|agzyzz|wl|rw</b>: will ask both read-write access and white
	 * list bypass</li>
	 * </ul>
	 * 
	 * @param key
	 *            the key that will allow us to exchange information with the
	 *            server
	 * @param host
	 *            the host to contact or NULL for localhost
	 * @param port
	 *            the port to contact it on
	 */
	public RemoteLibrary(String key, String host, int port) {
		int index = -1;
		if (key != null) {
			index = key.indexOf('|');
		}

		if (index >= 0) {
			this.key = key.substring(0, index);
			this.subkey = key.substring(index + 1);
		} else {
			this.key = key;
			this.subkey = "";
		}

		this.host = host;
		this.port = port;
	}

	@Override
	public String getLibraryName() {
		return (rw ? "[READ-ONLY] " : "") + host + ":" + port;
	}

	@Override
	public Status getStatus() {
		Instance.getInstance().getTraceHandler().trace("Getting remote lib status...");
		Status status = getStatusDo();
		Instance.getInstance().getTraceHandler().trace("Remote lib status: " + status);
		return status;
	}

	private Status getStatusDo() {
		final Status[] result = new Status[1];

		result[0] = Status.INVALID;

		try {
			new RemoteConnectAction() {
				@Override
				public void action(Version serverVersion) throws Exception {
					Object rep = send(new Object[] { subkey, "PING" });

					if ("r/w".equals(rep)) {
						rw = true;
						result[0] = Status.READ_WRITE;
					} else if ("r/o".equals(rep)) {
						rw = false;
						result[0] = Status.READ_ONLY;
					} else {
						result[0] = Status.UNAUTHORIZED;
					}
				}

				@Override
				protected void onError(Exception e) {
					if (e instanceof SSLException) {
						result[0] = Status.UNAUTHORIZED;
					} else {
						result[0] = Status.UNAVAILABLE;
					}
				}
			}.connect();
		} catch (UnknownHostException e) {
			result[0] = Status.INVALID;
		} catch (IllegalArgumentException e) {
			result[0] = Status.INVALID;
		} catch (Exception e) {
			result[0] = Status.UNAVAILABLE;
		}

		return result[0];
	}

	@Override
	public Image getCover(final String luid) throws IOException {
		final Image[] result = new Image[1];

		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				Object rep = action.send(new Object[] { subkey, "GET_COVER",
						luid });
				result[0] = (Image) rep;
			}
		});

		return result[0];
	}

	@Override
	public Image getCustomSourceCover(final String source) throws IOException {
		return getCustomCover(source, "SOURCE");
	}

	@Override
	public Image getCustomAuthorCover(final String author) throws IOException {
		return getCustomCover(author, "AUTHOR");
	}

	// type: "SOURCE" or "AUTHOR"
	private Image getCustomCover(final String source, final String type)
			throws IOException {
		final Image[] result = new Image[1];

		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				Object rep = action.send(new Object[] { subkey,
						"GET_CUSTOM_COVER", type, source });
				result[0] = (Image) rep;
			}
		});

		return result[0];
	}

	@Override
	public synchronized Story getStory(final String luid, Progress pg)
			throws IOException {
		final Progress pgF = pg;
		final Story[] result = new Story[1];

		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				Progress pg = pgF;
				if (pg == null) {
					pg = new Progress();
				}

				Object rep = action.send(new Object[] { subkey, "GET_STORY",
						luid });

				MetaData meta = null;
				if (rep instanceof MetaData) {
					meta = (MetaData) rep;
					if (meta.getWords() <= Integer.MAX_VALUE) {
						pg.setMinMax(0, (int) meta.getWords());
					}
				}

				List<Object> list = new ArrayList<Object>();
				for (Object obj = action.send(null); obj != null; obj = action
						.send(null)) {
					list.add(obj);
					pg.add(1);
				}

				result[0] = RemoteLibraryServer.rebuildStory(list);
				pg.done();
			}
		});

		return result[0];
	}

	@Override
	public synchronized Story save(final Story story, final String luid,
			Progress pg) throws IOException {

		final String[] luidSaved = new String[1];
		Progress pgSave = new Progress();
		Progress pgRefresh = new Progress();
		if (pg == null) {
			pg = new Progress();
		}

		pg.setMinMax(0, 10);
		pg.addProgress(pgSave, 9);
		pg.addProgress(pgRefresh, 1);

		final Progress pgF = pgSave;

		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				Progress pg = pgF;
				if (story.getMeta().getWords() <= Integer.MAX_VALUE) {
					pg.setMinMax(0, (int) story.getMeta().getWords());
				}

				action.send(new Object[] { subkey, "SAVE_STORY", luid });

				List<Object> list = RemoteLibraryServer.breakStory(story);
				for (Object obj : list) {
					action.send(obj);
					pg.add(1);
				}

				luidSaved[0] = (String) action.send(null);

				pg.done();
			}
		});

		// because the meta changed:
		MetaData meta = getInfo(luidSaved[0]);
		if (story.getMeta().getClass() != null) {
			// If already available locally:
			meta.setCover(story.getMeta().getCover());
		} else {
			// If required:
			meta.setCover(getCover(meta.getLuid()));
		}
		story.setMeta(meta);

		pg.done();

		return story;
	}

	@Override
	public synchronized void delete(final String luid) throws IOException {
		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				action.send(new Object[] { subkey, "DELETE_STORY", luid });
			}
		});
	}

	@Override
	public void setSourceCover(final String source, final String luid)
			throws IOException {
		setCover(source, luid, "SOURCE");
	}

	@Override
	public void setAuthorCover(final String author, final String luid)
			throws IOException {
		setCover(author, luid, "AUTHOR");
	}

	// type = "SOURCE" | "AUTHOR"
	private void setCover(final String value, final String luid,
			final String type) throws IOException {
		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				action.send(new Object[] { subkey, "SET_COVER", type, value,
						luid });
			}
		});
	}

	@Override
	// Could work (more slowly) without it
	public MetaData imprt(final URL url, Progress pg) throws IOException {
		// Import the file locally if it is actually a file
		
		if (url == null || url.getProtocol().equalsIgnoreCase("file")) {
			return super.imprt(url, pg);
		}

		// Import it remotely if it is an URL

		if (pg == null) {
			pg = new Progress();
		}

		final Progress pgF = pg;
		final String[] luid = new String[1];

		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				Progress pg = pgF;

				Object rep = action.send(new Object[] { subkey, "IMPORT",
						url.toString() });

				while (true) {
					if (!RemoteLibraryServer.updateProgress(pg, rep)) {
						break;
					}

					rep = action.send(null);
				}

				pg.done();
				luid[0] = (String) rep;
			}
		});

		if (luid[0] == null) {
			throw new IOException("Remote failure");
		}

		pg.done();
		return getInfo(luid[0]);
	}

	@Override
	// Could work (more slowly) without it
	protected synchronized void changeSTA(final String luid,
			final String newSource, final String newTitle,
			final String newAuthor, Progress pg) throws IOException {

		final Progress pgF = pg == null ? new Progress() : pg;

		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				Progress pg = pgF;

				Object rep = action.send(new Object[] { subkey, "CHANGE_STA",
						luid, newSource, newTitle, newAuthor });
				while (true) {
					if (!RemoteLibraryServer.updateProgress(pg, rep)) {
						break;
					}

					rep = action.send(null);
				}
			}
		});
	}

	@Override
	public File getFile(final String luid, Progress pg) {
		throw new java.lang.InternalError(
				"Operation not supportorted on remote Libraries");
	}

	/**
	 * Stop the server.
	 * 
	 * @throws IOException
	 *             in case of I/O error (including bad key)
	 */
	public void exit() throws IOException {
		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				action.send(new Object[] { subkey, "EXIT" });
				Thread.sleep(100);
			}
		});
	}

	@Override
	public MetaData getInfo(String luid) throws IOException {
		List<MetaData> metas = getMetasList(luid, null);
		if (!metas.isEmpty()) {
			return metas.get(0);
		}

		return null;
	}

	@Override
	protected List<MetaData> getMetas(Progress pg) throws IOException {
		return getMetasList("*", pg);
	}

	@Override
	protected void updateInfo(MetaData meta) {
		// Will be taken care of directly server side
	}

	@Override
	protected void invalidateInfo(String luid) {
		// Will be taken care of directly server side
	}

	// The following methods are only used by Save and Delete in BasicLibrary:

	@Override
	protected int getNextId() {
		throw new java.lang.InternalError("Should not have been called");
	}

	@Override
	protected void doDelete(String luid) throws IOException {
		throw new java.lang.InternalError("Should not have been called");
	}

	@Override
	protected Story doSave(Story story, Progress pg) throws IOException {
		throw new java.lang.InternalError("Should not have been called");
	}

	//

	/**
	 * Return the meta of the given story or a list of all known metas if the
	 * luid is "*".
	 * <p>
	 * Will not get the covers.
	 * 
	 * @param luid
	 *            the luid of the story or *
	 * @param pg
	 *            the optional progress
	 * 
	 * @return the metas
	 * 
	 * @throws IOException
	 *             in case of I/O error or bad key (SSLException)
	 */
	private List<MetaData> getMetasList(final String luid, Progress pg)
			throws IOException {
		final Progress pgF = pg;
		final List<MetaData> metas = new ArrayList<MetaData>();

		connectRemoteAction(new RemoteAction() {
			@Override
			public void action(ConnectActionClientObject action)
					throws Exception {
				Progress pg = pgF;
				if (pg == null) {
					pg = new Progress();
				}

				Object rep = action.send(new Object[] { subkey, "GET_METADATA",
						luid });

				while (true) {
					if (!RemoteLibraryServer.updateProgress(pg, rep)) {
						break;
					}

					rep = action.send(null);
				}

				if (rep instanceof MetaData[]) {
					for (MetaData meta : (MetaData[]) rep) {
						metas.add(meta);
					}
				} else if (rep != null) {
					metas.add((MetaData) rep);
				}
			}
		});

		return metas;
	}

	private void connectRemoteAction(final RemoteAction runAction)
			throws IOException {
		final IOException[] err = new IOException[1];
		try {
			final RemoteConnectAction[] array = new RemoteConnectAction[1];
			RemoteConnectAction ra = new RemoteConnectAction() {
				@Override
				public void action(Version serverVersion) throws Exception {
					runAction.action(array[0]);
				}

				@Override
				protected void onError(Exception e) {
					if (!(e instanceof IOException)) {
						Instance.getInstance().getTraceHandler().error(e);
						return;
					}

					err[0] = (IOException) e;
				}
			};
			array[0] = ra;
			ra.connect();
		} catch (Exception e) {
			err[0] = (IOException) e;
		}

		if (err[0] != null) {
			throw err[0];
		}
	}
}
