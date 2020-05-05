package be.nikiroo.fanfix.library;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.StringUtils;

public class MetaResultList {
	/** Max number of items before splitting in [A-B] etc. for eligible items */
	static private final int MAX = 20;

	private List<MetaData> metas;

	// Lazy lists:
	// TODO: sync-protect them?
	private List<String> sources;
	private List<String> authors;
	private List<String> tags;

	// can be null (will consider it empty)
	public MetaResultList(List<MetaData> metas) {
		if (metas == null) {
			metas = new ArrayList<MetaData>();
		}

		Collections.sort(metas);
		this.metas = metas;
	}

	// not NULL
	// sorted
	public List<MetaData> getMetas() {
		return metas;
	}

	public List<String> getSources() {
		if (sources == null) {
			sources = new ArrayList<String>();
			for (MetaData meta : metas) {
				if (!sources.contains(meta.getSource()))
					sources.add(meta.getSource());
			}
			sort(sources);
		}

		return sources;
	}

	// A -> (A), A/ -> (A, A/*) if we can find something for "*"
	public List<String> getSources(String source) {
		List<String> linked = new ArrayList<String>();
		if (source != null && !source.isEmpty()) {
			if (!source.endsWith("/")) {
				linked.add(source);
			} else {
				linked.add(source.substring(0, source.length() - 1));
				for (String src : getSources()) {
					if (src.startsWith(source)) {
						linked.add(src);
					}
				}
			}
		}

		sort(linked);
		return linked;
	}

	/**
	 * List all the known types (sources) of stories, grouped by directory
	 * ("Source_1/a" and "Source_1/b" will be grouped into "Source_1").
	 * <p>
	 * Note that an empty item in the list means a non-grouped source (type) --
	 * e.g., you could have for Source_1:
	 * <ul>
	 * <li><tt></tt>: empty, so source is "Source_1"</li>
	 * <li><tt>a</tt>: empty, so source is "Source_1/a"</li>
	 * <li><tt>b</tt>: empty, so source is "Source_1/b"</li>
	 * </ul>
	 * 
	 * @return the grouped list
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public Map<String, List<String>> getSourcesGrouped() throws IOException {
		Map<String, List<String>> map = new TreeMap<String, List<String>>();
		for (String source : getSources()) {
			String name;
			String subname;

			int pos = source.indexOf('/');
			if (pos > 0 && pos < source.length() - 1) {
				name = source.substring(0, pos);
				subname = source.substring(pos + 1);

			} else {
				name = source;
				subname = "";
			}

			List<String> list = map.get(name);
			if (list == null) {
				list = new ArrayList<String>();
				map.put(name, list);
			}
			list.add(subname);
		}

		return map;
	}

	public List<String> getAuthors() {
		if (authors == null) {
			authors = new ArrayList<String>();
			for (MetaData meta : metas) {
				if (!authors.contains(meta.getAuthor()))
					authors.add(meta.getAuthor());
			}
			sort(authors);
		}

		return authors;
	}

	/**
	 * Return the list of authors, grouped by starting letter(s) if needed.
	 * <p>
	 * If the number of authors is not too high, only one group with an empty
	 * name and all the authors will be returned.
	 * <p>
	 * If not, the authors will be separated into groups:
	 * <ul>
	 * <li><tt>*</tt>: any author whose name doesn't contain letters nor numbers
	 * </li>
	 * <li><tt>0-9</tt>: any author whose name starts with a number</li>
	 * <li><tt>A-C</tt> (for instance): any author whose name starts with
	 * <tt>A</tt>, <tt>B</tt> or <tt>C</tt></li>
	 * </ul>
	 * Note that the letters used in the groups can vary (except <tt>*</tt> and
	 * <tt>0-9</tt>, which may only be present or not).
	 * 
	 * @return the authors' names, grouped by letter(s)
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public Map<String, List<String>> getAuthorsGrouped() throws IOException {
		return group(getAuthors());
	}

	public List<String> getTags() {
		if (tags == null) {
			tags = new ArrayList<String>();
			for (MetaData meta : metas) {
				for (String tag : meta.getTags()) {
					if (!tags.contains(tag))
						tags.add(tag);
				}
			}
			sort(tags);
		}

		return tags;
	}

	/**
	 * Return the list of tags, grouped by starting letter(s) if needed.
	 * <p>
	 * If the number of tags is not too high, only one group with an empty name
	 * and all the tags will be returned.
	 * <p>
	 * If not, the tags will be separated into groups:
	 * <ul>
	 * <li><tt>*</tt>: any tag which name doesn't contain letters nor numbers
	 * </li>
	 * <li><tt>0-9</tt>: any tag which name starts with a number</li>
	 * <li><tt>A-C</tt> (for instance): any tag which name starts with
	 * <tt>A</tt>, <tt>B</tt> or <tt>C</tt></li>
	 * </ul>
	 * Note that the letters used in the groups can vary (except <tt>*</tt> and
	 * <tt>0-9</tt>, which may only be present or not).
	 * 
	 * @return the tags' names, grouped by letter(s)
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public Map<String, List<String>> getTagsGrouped() throws IOException {
		return group(getTags());
	}

	// helper
	public List<MetaData> filter(String source, String author, String tag) {
		List<String> sources = source == null ? null : Arrays.asList(source);
		List<String> authors = author == null ? null : Arrays.asList(author);
		List<String> tags = tag == null ? null : Arrays.asList(tag);

		return filter(sources, authors, tags);
	}

	// null or empty -> no check, rest = must be included
	// source: a source ending in "/" means "this or any source starting with
	// this",
	// i;e., to enable source hierarchy
	// + sorted
	public List<MetaData> filter(List<String> sources, List<String> authors,
			List<String> tags) {
		if (sources != null && sources.isEmpty())
			sources = null;
		if (authors != null && authors.isEmpty())
			authors = null;
		if (tags != null && tags.isEmpty())
			tags = null;

		// Quick check
		if (sources == null && authors == null && tags == null) {
			return metas;
		}

		// allow "sources/" hierarchy
		if (sources != null) {
			List<String> folders = new ArrayList<String>();
			List<String> leaves = new ArrayList<String>();
			for (String source : sources) {
				if (source.endsWith("/")) {
					if (!folders.contains(source))
						folders.add(source);
				} else {
					if (!leaves.contains(source))
						leaves.add(source);
				}
			}

			sources = leaves;
			for (String folder : folders) {
				for (String otherLeaf : getSources(folder)) {
					if (!sources.contains(otherLeaf)) {
						sources.add(otherLeaf);
					}
				}
			}
		}

		List<MetaData> result = new ArrayList<MetaData>();
		for (MetaData meta : metas) {
			if (sources != null && !sources.contains(meta.getSource())) {
				continue;
			}
			if (authors != null && !authors.contains(meta.getAuthor())) {
				continue;
			}

			if (tags != null) {
				boolean keep = false;
				for (String thisTag : meta.getTags()) {
					if (tags.contains(thisTag))
						keep = true;
				}

				if (!keep)
					continue;
			}

			result.add(meta);
		}

		Collections.sort(result);
		return result;
	}

	/**
	 * Return the list of values, grouped by starting letter(s) if needed.
	 * <p>
	 * If the number of values is not too high, only one group with an empty
	 * name and all the values will be returned (see
	 * {@link MetaResultList#MAX}).
	 * <p>
	 * If not, the values will be separated into groups:
	 * <ul>
	 * <li><tt>*</tt>: any value which name doesn't contain letters nor numbers
	 * </li>
	 * <li><tt>0-9</tt>: any value which name starts with a number</li>
	 * <li><tt>A-C</tt> (for instance): any value which name starts with
	 * <tt>A</tt>, <tt>B</tt> or <tt>C</tt></li>
	 * </ul>
	 * Note that the letters used in the groups can vary (except <tt>*</tt> and
	 * <tt>0-9</tt>, which may only be present or not).
	 * 
	 * @param values
	 *            the values to group
	 * 
	 * @return the values, grouped by letter(s)
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	private Map<String, List<String>> group(List<String> values)
			throws IOException {
		Map<String, List<String>> groups = new TreeMap<String, List<String>>();

		// If all authors fit the max, just report them as is
		if (values.size() <= MAX) {
			groups.put("", values);
			return groups;
		}

		// Create groups A to Z, which can be empty here
		for (char car = 'A'; car <= 'Z'; car++) {
			groups.put(Character.toString(car), find(values, car));
		}

		// Collapse them
		List<String> keys = new ArrayList<String>(groups.keySet());
		for (int i = 0; i + 1 < keys.size(); i++) {
			String keyNow = keys.get(i);
			String keyNext = keys.get(i + 1);

			List<String> now = groups.get(keyNow);
			List<String> next = groups.get(keyNext);

			int currentTotal = now.size() + next.size();
			if (currentTotal <= MAX) {
				String key = keyNow.charAt(0) + "-"
						+ keyNext.charAt(keyNext.length() - 1);

				List<String> all = new ArrayList<String>();
				all.addAll(now);
				all.addAll(next);

				groups.remove(keyNow);
				groups.remove(keyNext);
				groups.put(key, all);

				keys.set(i, key); // set the new key instead of key(i)
				keys.remove(i + 1); // remove the next, consumed key
				i--; // restart at key(i)
			}
		}

		// Add "special" groups
		groups.put("*", find(values, '*'));
		groups.put("0-9", find(values, '0'));

		// Prune empty groups
		keys = new ArrayList<String>(groups.keySet());
		for (String key : keys) {
			if (groups.get(key).isEmpty()) {
				groups.remove(key);
			}
		}

		return groups;
	}

	/**
	 * Get all the authors that start with the given character:
	 * <ul>
	 * <li><tt>*</tt>: any author whose name doesn't contain letters nor numbers
	 * </li>
	 * <li><tt>0</tt>: any authors whose name starts with a number</li>
	 * <li><tt>A</tt> (any capital latin letter): any author whose name starts
	 * with <tt>A</tt></li>
	 * </ul>
	 * 
	 * @param values
	 *            the full list of authors
	 * @param car
	 *            the starting character, <tt>*</tt>, <tt>0</tt> or a capital
	 *            letter
	 * 
	 * @return the authors that fulfil the starting letter
	 */
	private List<String> find(List<String> values, char car) {
		List<String> accepted = new ArrayList<String>();
		for (String value : values) {
			char first = '*';
			for (int i = 0; first == '*' && i < value.length(); i++) {
				String san = StringUtils.sanitize(value, true, true);
				char c = san.charAt(i);
				if (c >= '0' && c <= '9') {
					first = '0';
				} else if (c >= 'a' && c <= 'z') {
					first = (char) (c - 'a' + 'A');
				} else if (c >= 'A' && c <= 'Z') {
					first = c;
				}
			}

			if (first == car) {
				accepted.add(value);
			}
		}

		return accepted;
	}

	/**
	 * Sort the given {@link String} values, ignoring case.
	 * 
	 * @param values
	 *            the values to sort
	 */
	private void sort(List<String> values) {
		Collections.sort(values, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return ("" + o1).compareToIgnoreCase("" + o2);
			}
		});
	}
}
