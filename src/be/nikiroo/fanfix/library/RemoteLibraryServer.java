package be.nikiroo.fanfix.library;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.Progress.ProgressListener;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.Version;
import be.nikiroo.utils.serial.server.ConnectActionServerObject;
import be.nikiroo.utils.serial.server.ServerObject;

/**
 * Create a new remote server that will listen for orders on the given port.
 * <p>
 * The available commands are given as arrays of objects (first item is the
 * command, the rest are the arguments).
 * <p>
 * All the commands are always prefixed by the subkey (which can be EMPTY if
 * none).
 * <p>
 * <ul>
 * <li>PING: will return the mode if the key is accepted (mode can be: "r/o" or
 * "r/w")</li>
 * <li>GET_METADATA *: will return the metadata of all the stories in the
 * library (array)</li> *
 * <li>GET_METADATA [luid]: will return the metadata of the story of LUID luid</li>
 * <li>GET_STORY [luid]: will return the given story if it exists (or NULL if
 * not)</li>
 * <li>SAVE_STORY [luid]: save the story (that must be sent just after the
 * command) with the given LUID, then return the LUID</li>
 * <li>IMPORT [url]: save the story found at the given URL, then return the LUID
 * </li>
 * <li>DELETE_STORY [luid]: delete the story of LUID luid</li>
 * <li>GET_COVER [luid]: return the cover of the story</li>
 * <li>GET_CUSTOM_COVER ["SOURCE"|"AUTHOR"] [source]: return the cover for this
 * source/author</li>
 * <li>SET_COVER ["SOURCE"|"AUTHOR"] [value] [luid]: set the default cover for
 * the given source/author to the cover of the story denoted by luid</li>
 * <li>CHANGE_SOURCE [luid] [new source]: change the source of the story of LUID
 * luid</li>
 * <li>EXIT: stop the server</li>
 * </ul>
 * 
 * @author niki
 */
public class RemoteLibraryServer extends ServerObject {
	private Map<Long, String> commands = new HashMap<Long, String>();
	private Map<Long, Long> times = new HashMap<Long, Long>();
	private Map<Long, Boolean> wls = new HashMap<Long, Boolean>();
	private Map<Long, Boolean> rws = new HashMap<Long, Boolean>();

	/**
	 * Create a new remote server (will not be active until
	 * {@link RemoteLibraryServer#start()} is called).
	 * <p>
	 * Note: the key we use here is the encryption key (it must not contain a
	 * subkey).
	 * 
	 * @param key
	 *            the key that will restrict access to this server
	 * @param port
	 *            the port to listen on
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public RemoteLibraryServer(String key, int port) throws IOException {
		super("Fanfix remote library", port, key);
		setTraceHandler(Instance.getInstance().getTraceHandler());
	}

	@Override
	protected Object onRequest(ConnectActionServerObject action,
			Version clientVersion, Object data, long id) throws Exception {
		long start = new Date().getTime();

		// defaults are positive (as previous versions without the feature)
		boolean rw = true;
		boolean wl = true;

		String subkey = "";
		String command = "";
		Object[] args = new Object[0];
		if (data instanceof Object[]) {
			Object[] dataArray = (Object[]) data;
			if (dataArray.length > 0) {
				subkey = "" + dataArray[0];
			}
			if (dataArray.length > 1) {
				command = "" + dataArray[1];

				args = new Object[dataArray.length - 2];
				for (int i = 2; i < dataArray.length; i++) {
					args[i - 2] = dataArray[i];
				}
			}
		}

		List<String> whitelist = Instance.getInstance().getConfig().getList(Config.SERVER_WHITELIST);
		if (whitelist == null) {
			whitelist = new ArrayList<String>();
		}

		if (whitelist.isEmpty()) {
			wl = false;
		}

		rw = Instance.getInstance().getConfig().getBoolean(Config.SERVER_RW, rw);
		if (!subkey.isEmpty()) {
			List<String> allowed = Instance.getInstance().getConfig().getList(Config.SERVER_ALLOWED_SUBKEYS);
			if (allowed.contains(subkey)) {
				if ((subkey + "|").contains("|rw|")) {
					rw = true;
				}
				if ((subkey + "|").contains("|wl|")) {
					wl = false; // |wl| = bypass whitelist
					whitelist = new ArrayList<String>();
				}
			}
		}

		String mode = display(wl, rw);

		String trace = mode + "[ " + command + "] ";
		for (Object arg : args) {
			trace += arg + " ";
		}
		long now = System.currentTimeMillis();
		System.out.println(StringUtils.fromTime(now) + ": " + trace);

		Object rep = null;
		try {
			rep = doRequest(action, command, args, rw, whitelist);
		} catch (IOException e) {
			rep = new RemoteLibraryException(e, true);
		}

		commands.put(id, command);
		wls.put(id, wl);
		rws.put(id, rw);
		times.put(id, (new Date().getTime() - start));

		return rep;
	}

	private String display(boolean whitelist, boolean rw) {
		String mode = "";
		if (!rw) {
			mode += "RO: ";
		}
		if (whitelist) {
			mode += "WL: ";
		}

		return mode;
	}

	@Override
	protected void onRequestDone(long id, long bytesReceived, long bytesSent) {
		boolean whitelist = wls.get(id);
		boolean rw = rws.get(id);
		wls.remove(id);
		rws.remove(id);

		String rec = StringUtils.formatNumber(bytesReceived) + "b";
		String sent = StringUtils.formatNumber(bytesSent) + "b";
		long now = System.currentTimeMillis();
		System.out.println(StringUtils.fromTime(now)
				+ ": "
				+ String.format("%s[>%s]: (%s sent, %s rec) in %d ms",
						display(whitelist, rw), commands.get(id), sent, rec,
						times.get(id)));

		commands.remove(id);
		times.remove(id);
	}

	private Object doRequest(ConnectActionServerObject action, String command,
			Object[] args, boolean rw, List<String> whitelist)
			throws NoSuchFieldException, NoSuchMethodException,
			ClassNotFoundException, IOException {
		if ("PING".equals(command)) {
			return rw ? "r/w" : "r/o";
		} else if ("GET_METADATA".equals(command)) {
			List<MetaData> metas = new ArrayList<MetaData>();

			if ("*".equals(args[0])) {
				Progress pg = createPgForwarder(action);

				for (MetaData meta : Instance.getInstance().getLibrary().getMetas(pg)) {
					metas.add(removeCover(meta));
				}

				forcePgDoneSent(pg);
			} else {
				MetaData meta = Instance.getInstance().getLibrary().getInfo((String) args[0]);
				MetaData light;
				if (meta.getCover() == null) {
					light = meta;
				} else {
					light = meta.clone();
					light.setCover(null);
				}

				metas.add(light);
			}

			if (!whitelist.isEmpty()) {
				for (int i = 0; i < metas.size(); i++) {
					if (!whitelist.contains(metas.get(i).getSource())) {
						metas.remove(i);
						i--;
					}
				}
			}

			return metas.toArray(new MetaData[0]);
		} else if ("GET_STORY".equals(command)) {
			MetaData meta = Instance.getInstance().getLibrary().getInfo((String) args[0]);
			if (meta == null) {
				return null;
			}

			if (!whitelist.isEmpty()) {
				if (!whitelist.contains(meta.getSource())) {
					return null;
				}
			}

			meta = meta.clone();
			meta.setCover(null);

			action.send(meta);
			action.rec();

			Story story = Instance.getInstance().getLibrary().getStory((String) args[0], null);
			for (Object obj : breakStory(story)) {
				action.send(obj);
				action.rec();
			}
		} else if ("SAVE_STORY".equals(command)) {
			if (!rw) {
				throw new RemoteLibraryException("Read-Only remote library: "
						+ args[0], false);
			}

			List<Object> list = new ArrayList<Object>();

			action.send(null);
			Object obj = action.rec();
			while (obj != null) {
				list.add(obj);
				action.send(null);
				obj = action.rec();
			}

			Story story = rebuildStory(list);
			Instance.getInstance().getLibrary().save(story, (String) args[0], null);
			return story.getMeta().getLuid();
		} else if ("IMPORT".equals(command)) {
			if (!rw) {
				throw new RemoteLibraryException("Read-Only remote library: "
						+ args[0], false);
			}

			Progress pg = createPgForwarder(action);
			MetaData meta = Instance.getInstance().getLibrary().imprt(new URL((String) args[0]), pg);
			forcePgDoneSent(pg);
			return meta.getLuid();
		} else if ("DELETE_STORY".equals(command)) {
			if (!rw) {
				throw new RemoteLibraryException("Read-Only remote library: "
						+ args[0], false);
			}

			Instance.getInstance().getLibrary().delete((String) args[0]);
		} else if ("GET_COVER".equals(command)) {
			return Instance.getInstance().getLibrary().getCover((String) args[0]);
		} else if ("GET_CUSTOM_COVER".equals(command)) {
			if ("SOURCE".equals(args[0])) {
				return Instance.getInstance().getLibrary().getCustomSourceCover((String) args[1]);
			} else if ("AUTHOR".equals(args[0])) {
				return Instance.getInstance().getLibrary().getCustomAuthorCover((String) args[1]);
			} else {
				return null;
			}
		} else if ("SET_COVER".equals(command)) {
			if (!rw) {
				throw new RemoteLibraryException("Read-Only remote library: "
						+ args[0] + ", " + args[1], false);
			}

			if ("SOURCE".equals(args[0])) {
				Instance.getInstance().getLibrary().setSourceCover((String) args[1], (String) args[2]);
			} else if ("AUTHOR".equals(args[0])) {
				Instance.getInstance().getLibrary().setAuthorCover((String) args[1], (String) args[2]);
			}
		} else if ("CHANGE_STA".equals(command)) {
			if (!rw) {
				throw new RemoteLibraryException("Read-Only remote library: " + args[0] + ", " + args[1], false);
			}

			Progress pg = createPgForwarder(action);
			Instance.getInstance().getLibrary().changeSTA((String) args[0], (String) args[1], (String) args[2],
					(String) args[3], pg);
			forcePgDoneSent(pg);
		} else if ("EXIT".equals(command)) {
			if (!rw) {
				throw new RemoteLibraryException(
						"Read-Only remote library: EXIT", false);
			}

			stop(10000, false);
		}

		return null;
	}

	@Override
	protected void onError(Exception e) {
		if (e instanceof SSLException) {
			long now = System.currentTimeMillis();
			System.out.println(StringUtils.fromTime(now) + ": "
					+ "[Client connection refused (bad key)]");
		} else {
			getTraceHandler().error(e);
		}
	}

	/**
	 * Break a story in multiple {@link Object}s for easier serialisation.
	 * 
	 * @param story
	 *            the {@link Story} to break
	 * 
	 * @return the list of {@link Object}s
	 */
	static List<Object> breakStory(Story story) {
		List<Object> list = new ArrayList<Object>();

		story = story.clone();
		list.add(story);

		if (story.getMeta().isImageDocument()) {
			for (Chapter chap : story) {
				list.add(chap);
				list.addAll(chap.getParagraphs());
				chap.setParagraphs(new ArrayList<Paragraph>());
			}
			story.setChapters(new ArrayList<Chapter>());
		}

		return list;
	}

	/**
	 * Rebuild a story from a list of broke up {@link Story} parts.
	 * 
	 * @param list
	 *            the list of {@link Story} parts
	 * 
	 * @return the reconstructed {@link Story}
	 */
	static Story rebuildStory(List<Object> list) {
		Story story = null;
		Chapter chap = null;

		for (Object obj : list) {
			if (obj instanceof Story) {
				story = (Story) obj;
			} else if (obj instanceof Chapter) {
				chap = (Chapter) obj;
				story.getChapters().add(chap);
			} else if (obj instanceof Paragraph) {
				chap.getParagraphs().add((Paragraph) obj);
			}
		}

		return story;
	}

	/**
	 * Update the {@link Progress} with the adequate {@link Object} received
	 * from the network via {@link RemoteLibraryServer}.
	 * 
	 * @param pg
	 *            the {@link Progress} to update
	 * @param rep
	 *            the object received from the network
	 * 
	 * @return TRUE if it was a progress event, FALSE if not
	 */
	static boolean updateProgress(Progress pg, Object rep) {
		boolean updateProgress = false;
		if (rep instanceof Integer[] && ((Integer[]) rep).length == 3)
			updateProgress = true;
		if (rep instanceof Object[] && ((Object[]) rep).length >= 5
				&& "UPDATE".equals(((Object[]) rep)[0]))
			updateProgress = true;

		if (updateProgress) {
			Object[] a = (Object[]) rep;

			int offset = 0;
			if (a[0] instanceof String) {
				offset = 1;
			}

			int min = (Integer) a[0 + offset];
			int max = (Integer) a[1 + offset];
			int progress = (Integer) a[2 + offset];
			
			Object meta = null;
			if (a.length > (3 + offset)) {
				meta = a[3 + offset];
			}
			
			String name = null;
			if (a.length > (4 + offset)) {
				name = a[4 + offset] == null ? "" : a[4 + offset].toString();
			}
			

			if (min >= 0 && min <= max) {
				pg.setName(name);
				pg.setMinMax(min, max);
				pg.setProgress(progress);
				if (meta != null) {
					pg.put("meta", meta);
				}

				return true;
			}
		}

		return false;
	}

	/**
	 * Create a {@link Progress} that will forward its progress over the
	 * network.
	 * 
	 * @param action
	 *            the {@link ConnectActionServerObject} to use to forward it
	 * 
	 * @return the {@link Progress}
	 */
	private Progress createPgForwarder(final ConnectActionServerObject action) {
		final Boolean[] isDoneForwarded = new Boolean[] { false };
		final Progress pg = new Progress() {
			@Override
			public boolean isDone() {
				return isDoneForwarded[0];
			}
		};

		final Integer[] p = new Integer[] { -1, -1, -1 };
		final Object[] pMeta = new MetaData[1];
		final String[] pName = new String[1];
		final Long[] lastTime = new Long[] { new Date().getTime() };
		pg.addProgressListener(new ProgressListener() {
			@Override
			public void progress(Progress progress, String name) {
				Object meta = pg.get("meta");
				if (meta instanceof MetaData) {
					meta = removeCover((MetaData)meta);
				}
				
				int min = pg.getMin();
				int max = pg.getMax();
				int rel = min
						+ (int) Math.round(pg.getRelativeProgress()
								* (max - min));
				
				boolean samePg = p[0] == min && p[1] == max && p[2] == rel;
				
				// Do not re-send the same value twice over the wire,
				// unless more than 2 seconds have elapsed (to maintain the
				// connection)
				if (!samePg || !same(pMeta[0], meta)
						|| !same(pName[0], name) //
						|| (new Date().getTime() - lastTime[0] > 2000)) {
					p[0] = min;
					p[1] = max;
					p[2] = rel;
					pMeta[0] = meta;
					pName[0] = name;

					try {
						action.send(new Object[] { "UPDATE", min, max, rel,
								meta, name });
						action.rec();
					} catch (Exception e) {
						getTraceHandler().error(e);
					}

					lastTime[0] = new Date().getTime();
				}

				isDoneForwarded[0] = (pg.getProgress() >= pg.getMax());
			}
		});

		return pg;
	}
	
	private boolean same(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null)
			return obj1 == null && obj2 == null;

		return obj1.equals(obj2);
	}

	// with 30 seconds timeout
	private void forcePgDoneSent(Progress pg) {
		long start = new Date().getTime();
		pg.done();
		while (!pg.isDone() && new Date().getTime() - start < 30000) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				getTraceHandler().error(e);
			}
		}
	}
	
	private MetaData removeCover(MetaData meta) {
		MetaData light = null;
		if (meta != null) {
			if (meta.getCover() == null) {
				light = meta;
			} else {
				light = meta.clone();
				light.setCover(null);
			}
		}
		
		return light;
	}
}
