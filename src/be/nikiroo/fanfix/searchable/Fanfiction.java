package be.nikiroo.fanfix.searchable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.supported.SupportType;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.StringUtils;

/**
 * A {@link BasicSearchable} for Fanfiction.NET.
 * 
 * @author niki
 */
class Fanfiction extends BasicSearchable {
	static private String BASE_URL = "http://fanfiction.net/";

	/**
	 * Create a new {@link Fanfiction}.
	 * 
	 * @param type
	 *            {@link SupportType#FANFICTION}
	 */
	public Fanfiction(SupportType type) {
		super(type);
	}

	@Override
	public List<SearchableTag> getTags() throws IOException {
		String storiesName = null;
		String crossoversName = null;
		Map<String, String> stories = new HashMap<String, String>();
		Map<String, String> crossovers = new HashMap<String, String>();

		Document mainPage = load(BASE_URL, true);
		Element menu = mainPage.getElementsByClass("dropdown").first();
		if (menu != null) {
			Element ul = menu.getElementsByClass("dropdown-menu").first();
			if (ul != null) {
				Map<String, String> currentList = null;
				for (Element li : ul.getElementsByTag("li")) {
					if (li.hasClass("disabled")) {
						if (storiesName == null) {
							storiesName = li.text();
							currentList = stories;
						} else {
							crossoversName = li.text();
							currentList = crossovers;
						}
					} else if (currentList != null) {
						Element a = li.getElementsByTag("a").first();
						if (a != null) {
							currentList.put(a.absUrl("href"), a.text());
						}
					}
				}
			}
		}

		List<SearchableTag> tags = new ArrayList<SearchableTag>();

		if (storiesName != null) {
			SearchableTag tag = new SearchableTag(null, storiesName, false);
			for (String id : stories.keySet()) {
				tag.add(new SearchableTag(id, stories.get(id), false, false));
			}
			tags.add(tag);
		}

		if (crossoversName != null) {
			SearchableTag tag = new SearchableTag(null, crossoversName, false);
			for (String id : crossovers.keySet()) {
				tag.add(new SearchableTag(id, crossovers.get(id), false, false));
			}
			tags.add(tag);
		}

		return tags;
	}

	@Override
	public void fillTag(SearchableTag tag) throws IOException {
		if (tag.getId() == null || tag.isComplete()) {
			return;
		}

		Document doc = load(tag.getId(), false);
		Element list = doc.getElementById("list_output");
		if (list != null) {
			Element table = list.getElementsByTag("table").first();
			if (table != null) {
				for (Element div : table.getElementsByTag("div")) {
					Element a = div.getElementsByTag("a").first();
					Element span = div.getElementsByTag("span").first();

					if (a != null) {
						String subid = a.absUrl("href");
						boolean crossoverSubtag = subid
								.contains("/crossovers/");

						SearchableTag subtag = new SearchableTag(subid,
								a.text(), !crossoverSubtag, !crossoverSubtag);

						tag.add(subtag);
						if (span != null) {
							String nr = span.text();
							if (nr.startsWith("(")) {
								nr = nr.substring(1);
							}
							if (nr.endsWith(")")) {
								nr = nr.substring(0, nr.length() - 1);
							}
							nr = nr.trim();

							// TODO: fix toNumber/fromNumber
							nr = nr.replaceAll("\\.[0-9]*", "");

							subtag.setCount(StringUtils.toNumber(nr));
						}
					}
				}
			}
		}

		tag.setComplete(true);
	}

	@Override
	public List<MetaData> search(String search, int page) throws IOException {
		String encoded = URLEncoder.encode(search.toLowerCase(), "utf-8");
		String url = BASE_URL + "search/?ready=1&type=story&keywords="
				+ encoded + "&ppage=" + page;

		return getStories(url, null, null);
	}

	@Override
	public List<MetaData> search(SearchableTag tag, int page)
			throws IOException {
		List<MetaData> metas = new ArrayList<MetaData>();

		String url = tag.getId();
		if (url != null) {
			if (page > 1) {
				int pos = url.indexOf("&p=");
				if (pos >= 0) {
					url = url.replaceAll("(.*\\&p=)[0-9]*(.*)", "$1\\" + page
							+ "$2");
				} else {
					url += "&p=" + page;
				}
			}

			Document doc = load(url, false);

			// Update the pages number if needed
			if (tag.getPages() < 0 && tag.isLeaf()) {
				tag.setPages(getPages(doc));
			}

			// Find out the full subjects (including parents)
			String subjects = "";
			for (SearchableTag t = tag; t != null; t = t.getParent()) {
				if (!subjects.isEmpty()) {
					subjects += ", ";
				}
				subjects += t.getName();
			}

			metas = getStories(url, doc, subjects);
		}

		return metas;
	}

	@Override
	public int searchPages(String search) throws IOException {
		String encoded = URLEncoder.encode(search.toLowerCase(), "utf-8");
		String url = BASE_URL + "search/?ready=1&type=story&keywords="
				+ encoded;

		return getPages(load(url, false));
	}

	@Override
	public int searchPages(SearchableTag tag) throws IOException {
		if (tag.isLeaf()) {
			String url = tag.getId();
			return getPages(load(url, false));
		}

		return 0;
	}

	/**
	 * Return the number of pages in this stories result listing.
	 * 
	 * @param doc
	 *            the document
	 * 
	 * @return the number of pages or -1 if unknown
	 */
	private int getPages(Document doc) {
		int pages = -1;

		if (doc != null) {
			Element center = doc.getElementsByTag("center").first();
			if (center != null) {
				for (Element a : center.getElementsByTag("a")) {
					if (a.absUrl("href").contains("&p=")) {
						int thisLinkPages = -1;
						try {
							String[] tab = a.absUrl("href").split("=");
							tab = tab[tab.length - 1].split("&");
							thisLinkPages = Integer
									.parseInt(tab[tab.length - 1]);
						} catch (Exception e) {
						}

						pages = Math.max(pages, thisLinkPages);
					}
				}
			}
		}

		return pages;
	}

	/**
	 * Fetch the stories from the given page.
	 * 
	 * @param sourceUrl
	 *            the url of the document
	 * @param doc
	 *            the document to use (if NULL, will be loaded from
	 *            <tt>sourceUrl</tt>)
	 * @param mainSubject
	 *            the main subject (the anime/book/movie item related to the
	 *            stories, like "MLP" or "Doctor Who"), or NULL if none
	 * 
	 * @return the stories found in it
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	private List<MetaData> getStories(String sourceUrl, Document doc,
			String mainSubject) throws IOException {
		List<MetaData> metas = new ArrayList<MetaData>();

		if (doc == null) {
			doc = load(sourceUrl, false);
		}

		for (Element story : doc.getElementsByClass("z-list")) {
			MetaData meta = new MetaData();
			meta.setImageDocument(false);
			meta.setSource(getType().getSourceName());
			meta.setPublisher(getType().getSourceName());
			meta.setType(getType().toString());

			// Title, URL, Cover
			Element stitle = story.getElementsByClass("stitle").first();
			if (stitle != null) {
				meta.setTitle(stitle.text());
				meta.setUrl(stitle.absUrl("href"));
				meta.setUuid(meta.getUrl());
				Element cover = stitle.getElementsByTag("img").first();
				if (cover != null) {
					// note: see data-original if needed?
					String coverUrl = cover.absUrl("src");

					try {
						InputStream in = Instance.getInstance().getCache().open(new URL(coverUrl), getSupport(), true);
						try {
							meta.setCover(new Image(in));
						} finally {
							in.close();
						}
					} catch (Exception e) {
						// Should not happen on Fanfiction.net
						Instance.getInstance().getTraceHandler().error(new Exception(
								"Cannot download cover for Fanfiction story in search mode: " + meta.getTitle(), e));
					}
				}
			}

			// Author
			Elements as = story.getElementsByTag("a");
			if (as.size() > 1) {
				meta.setAuthor(as.get(1).text());
			}

			// Tags (concatenated text), published date, updated date, Resume
			String tags = "";
			List<String> tagList = new ArrayList<String>();
			Elements divs = story.getElementsByTag("div");
			if (divs.size() > 1 && divs.get(1).childNodeSize() > 0) {
				String resume = divs.get(1).text();
				if (divs.size() > 2) {
					tags = divs.get(2).text();
					resume = resume.substring(0,
							resume.length() - tags.length()).trim();

					for (Element d : divs.get(2).getElementsByAttribute(
							"data-xutime")) {
						String secs = d.attr("data-xutime");
						try {
							String date = new SimpleDateFormat("yyyy-MM-dd")
									.format(new Date(
											Long.parseLong(secs) * 1000));
							// (updated, ) published
							if (meta.getDate() != null) {
								tagList.add("Updated: " + meta.getDate());
							}
							meta.setDate(date);
						} catch (Exception e) {
						}
					}
				}

				meta.setResume(getSupport().makeChapter(new URL(sourceUrl), 0,
						Instance.getInstance().getTrans().getString(StringId.DESCRIPTION), resume));
			}

			// How are the tags ordered?
			// We have "Rated: xx", then the language, then all other tags
			// If the subject(s) is/are present, they are before "Rated: xx"

			// ////////////
			// Examples: //
			// ////////////

			// Search (Luna) Tags: [Harry Potter, Rated: T, English, Chapters:
			// 1, Words: 270, Reviews: 2, Published: 2/19/2013, Luna L.]

			// Normal (MLP) Tags: [Rated: T, Spanish, Drama/Suspense, Chapters:
			// 2, Words: 8,686, Reviews: 1, Favs: 1, Follows: 1, Updated: 4/7,
			// Published: 4/2]

			// Crossover (MLP/Who) Tags: [Rated: K+, English, Adventure/Romance,
			// Chapters: 8, Words: 7,788, Reviews: 2, Favs: 2, Follows: 1,
			// Published: 9/1/2016]

			boolean rated = false;
			boolean isLang = false;
			String subject = mainSubject == null ? "" : mainSubject;
			String[] tab = tags.split("  *-  *");
			for (int i = 0; i < tab.length; i++) {
				String tag = tab[i];
				if (tag.startsWith("Rated: ")) {
					rated = true;
				}

				if (!rated) {
					if (!subject.isEmpty()) {
						subject += ", ";
					}
					subject += tag;
				} else if (isLang) {
					meta.setLang(tag);
					isLang = false;
				} else {
					if (tag.contains(":")) {
						// Handle special tags:
						if (tag.startsWith("Words: ")) {
							try {
								meta.setWords(Long.parseLong(tag
										.substring("Words: ".length())
										.replace(",", "").trim()));
							} catch (Exception e) {
							}
						} else if (tag.startsWith("Rated: ")) {
							tagList.add(tag);
						}
					} else {
						// Normal tags are "/"-separated
						for (String t : tag.split("/")) {
							tagList.add(t);
						}
					}

					if (tag.startsWith("Rated: ")) {
						isLang = true;
					}
				}
			}

			meta.setSubject(subject);
			meta.setTags(tagList);

			metas.add(meta);
		}

		return metas;
	}
}
