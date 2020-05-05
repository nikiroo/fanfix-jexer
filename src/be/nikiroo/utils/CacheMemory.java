package be.nikiroo.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A memory only version of {@link Cache}.
 * 
 * @author niki
 */
public class CacheMemory extends Cache {
	private Map<String, byte[]> data;

	/**
	 * Create a new {@link CacheMemory}.
	 */
	public CacheMemory() {
		data = new HashMap<String, byte[]>();
	}

	@Override
	public boolean check(String uniqueID, boolean allowTooOld, boolean stable) {
		return data.containsKey(getKey(uniqueID));
	}

	@Override
	public boolean check(URL url, boolean allowTooOld, boolean stable) {
		return data.containsKey(getKey(url));
	}

	@Override
	public int clean(boolean onlyOld) {
		int cleaned = 0;
		if (!onlyOld) {
			cleaned = data.size();
			data.clear();
		}

		return cleaned;
	}

	@Override
	public InputStream load(String uniqueID, boolean allowTooOld, boolean stable) {
		if (check(uniqueID, allowTooOld, stable)) {
			return load(getKey(uniqueID));
		}

		return null;
	}

	@Override
	public InputStream load(URL url, boolean allowTooOld, boolean stable) {
		if (check(url, allowTooOld, stable)) {
			return load(getKey(url));
		}

		return null;
	}

	@Override
	public boolean remove(String uniqueID) {
		return data.remove(getKey(uniqueID)) != null;
	}

	@Override
	public boolean remove(URL url) {
		return data.remove(getKey(url)) != null;
	}

	@Override
	public long save(InputStream in, String uniqueID) throws IOException {
		byte[] bytes = IOUtils.toByteArray(in);
		data.put(getKey(uniqueID), bytes);
		return bytes.length;
	}

	@Override
	public long save(InputStream in, URL url) throws IOException {
		byte[] bytes = IOUtils.toByteArray(in);
		data.put(getKey(url), bytes);
		return bytes.length;
	}

	/**
	 * Return a key mapping to the given unique ID.
	 * 
	 * @param uniqueID the unique ID
	 * 
	 * @return the key
	 */
	private String getKey(String uniqueID) {
		return "UID:" + uniqueID;
	}

	/**
	 * Return a key mapping to the given urm.
	 * 
	 * @param url the url
	 * 
	 * @return the key
	 */
	private String getKey(URL url) {
		return "URL:" + url.toString();
	}

	/**
	 * Load the given key.
	 * 
	 * @param key the key to load
	 * @return the loaded data
	 */
	private InputStream load(String key) {
		byte[] data = this.data.get(key);
		if (data != null) {
			return new ByteArrayInputStream(data);
		}

		return null;
	}
}
