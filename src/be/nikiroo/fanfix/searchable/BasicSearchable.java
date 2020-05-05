package be.nikiroo.fanfix.searchable;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.supported.BasicSupport;
import be.nikiroo.fanfix.supported.SupportType;

/**
 * This class supports browsing through stories on the supported websites. It
 * will fetch some {@link MetaData} that satisfy a search query or some tags if
 * supported.
 * 
 * @author niki
 */
public abstract class BasicSearchable {
	private SupportType type;
	private BasicSupport support;

	/**
	 * Create a new {@link BasicSearchable} of the given type.
	 * 
	 * @param type
	 *            the type, must not be NULL
	 */
	public BasicSearchable(SupportType type) {
		setType(type);
		support = BasicSupport.getSupport(getType(), null);
	}

	/**
	 * Find the given tag by its hierarchical IDs.
	 * <p>
	 * I.E., it will take the tag A, subtag B, subsubtag C...
	 * 
	 * @param ids
	 *            the IDs to look for
	 * 
	 * @return the appropriate tag fully filled, or NULL if not found
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public SearchableTag getTag(Integer... ids) throws IOException {
		SearchableTag tag = null;
		List<SearchableTag> tags = getTags();

		for (Integer tagIndex : ids) {
			// ! 1-based index !
			if (tagIndex == null || tags == null || tagIndex <= 0
					|| tagIndex > tags.size()) {
				return null;
			}

			tag = tags.get(tagIndex - 1);
			fillTag(tag);
			tags = tag.getChildren();
		}

		return tag;
	}

	/**
	 * The support type.
	 * 
	 * @return the type
	 */
	public SupportType getType() {
		return type;
	}

	/**
	 * The support type.
	 * 
	 * @param type
	 *            the new type
	 */
	protected void setType(SupportType type) {
		this.type = type;
	}

	/**
	 * The associated {@link BasicSupport}.
	 * <p>
	 * Mostly used to download content.
	 * 
	 * @return the support
	 */
	protected BasicSupport getSupport() {
		return support;
	}

	/**
	 * Get a list of tags that can be browsed here.
	 * 
	 * @return the list of tags
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	abstract public List<SearchableTag> getTags() throws IOException;

	/**
	 * Fill the tag (set it 'complete') with more information from the support.
	 * 
	 * @param tag
	 *            the tag to fill
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	abstract public void fillTag(SearchableTag tag) throws IOException;

	/**
	 * Search for the given term and return the number of pages of results of
	 * stories satisfying this search term.
	 * 
	 * @param search
	 *            the term to search for
	 * 
	 * @return a number of pages
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	abstract public int searchPages(String search) throws IOException;

	/**
	 * Search for the given tag and return the number of pages of results of
	 * stories satisfying this tag.
	 * 
	 * @param tag
	 *            the tag to search for
	 * 
	 * @return a number of pages
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	abstract public int searchPages(SearchableTag tag) throws IOException;

	/**
	 * Search for the given term and return a list of stories satisfying this
	 * search term.
	 * <p>
	 * Not that the returned stories will <b>NOT</b> be complete, but will only
	 * contain enough information to present them to the user and retrieve them.
	 * <p>
	 * URL is guaranteed to be usable, LUID will always be NULL.
	 * 
	 * @param search
	 *            the term to search for
	 * @param page
	 *            the page to use for result pagination, index is 1-based
	 * 
	 * @return a list of stories that satisfy that search term
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	abstract public List<MetaData> search(String search, int page)
			throws IOException;

	/**
	 * Search for the given tag and return a list of stories satisfying this
	 * tag.
	 * <p>
	 * Not that the returned stories will <b>NOT</b> be complete, but will only
	 * contain enough information to present them to the user and retrieve them.
	 * <p>
	 * URL is guaranteed to be usable, LUID will always be NULL.
	 * 
	 * @param tag
	 *            the tag to search for
	 * @param page
	 *            the page to use for result pagination (see
	 *            {@link SearchableTag#getPages()}, remember to check for -1),
	 *            index is 1-based
	 * 
	 * @return a list of stories that satisfy that search term
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	abstract public List<MetaData> search(SearchableTag tag, int page)
			throws IOException;

	/**
	 * Load a document from its url.
	 * 
	 * @param url
	 *            the URL to load
	 * @param stable
	 *            TRUE for more stable resources, FALSE when they often change
	 * 
	 * @return the document
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected Document load(String url, boolean stable) throws IOException {
		return load(new URL(url), stable);
	}

	/**
	 * Load a document from its url.
	 * 
	 * @param url
	 *            the URL to load
	 * @param stable
	 *            TRUE for more stable resources, FALSE when they often change
	 * 
	 * @return the document
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected Document load(URL url, boolean stable) throws IOException {
		return DataUtil.load(Instance.getInstance().getCache().open(url, support, stable), "UTF-8", url.toString());
	}

	/**
	 * Return a {@link BasicSearchable} implementation supporting the given
	 * type, or NULL if it does not exist.
	 * 
	 * @param type
	 *            the type, can be NULL (will just return NULL, since we do not
	 *            support it)
	 * 
	 * @return an implementation that supports it, or NULL
	 */
	static public BasicSearchable getSearchable(SupportType type) {
		BasicSearchable support = null;

		if (type != null) {
			switch (type) {
			case FIMFICTION:
				// TODO searchable for FIMFICTION
				break;
			case FANFICTION:
				support = new Fanfiction(type);
				break;
			case MANGAHUB:
				// TODO searchable for MANGAHUB
				break;
			case E621:
				// TODO searchable for E621
				break;
			case YIFFSTAR:
				// TODO searchable for YIFFSTAR
				break;
			case E_HENTAI:
				// TODO searchable for E_HENTAI
				break;
			case MANGA_LEL:
				support = new MangaLel();
				break;
			case CBZ:
			case HTML:
			case INFO_TEXT:
			case TEXT:
			case EPUB:
				break;
			}
		}

		return support;
	}
}
