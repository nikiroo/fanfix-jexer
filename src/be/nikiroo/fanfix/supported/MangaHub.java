package be.nikiroo.fanfix.supported;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.nodes.Element;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * Support class for <a href="https://mangahub.io/">MangaHub</a>, a website
 * dedicated to Manga.
 * 
 * @author niki
 */
class MangaHub extends BasicSupport {
	@Override
	protected boolean isHtml() {
		return true;
	}

	@Override
	protected MetaData getMeta() throws IOException {
		MetaData meta = new MetaData();

		meta.setTitle(getTitle());
		meta.setDate("");
		meta.setAuthor(getAuthor());
		meta.setTags(getTags());
		meta.setSource(getType().getSourceName());
		meta.setUrl(getSource().toString());
		meta.setPublisher(getType().getSourceName());
		meta.setUuid(getSource().toString());
		meta.setLuid("");
		meta.setLang("en");
		meta.setSubject("manga");
		meta.setType(getType().toString());
		meta.setImageDocument(true);
		meta.setCover(getCover());

		return meta;
	}

	private String getTitle() {
		Element doc = getSourceNode();

		Element el = doc.getElementsByTag("h1").first();
		if (el != null) {
			return StringUtils.unhtml(el.text()).trim();
		}

		return null;
	}

	private String getAuthor() {
		String author = "";

		Element el = getSourceNode().select("h1+div span:not([class])").first();
		if (el != null)
			author = StringUtils.unhtml(el.text()).trim();
		return author;
	}

	private List<String> getTags() {
		return getListA("genre-label");
	}

	private List<String> getListA(String uniqueClass) {
		List<String> list = new ArrayList<String>();

		Element doc = getSourceNode();
		Element el = doc.getElementsByClass(uniqueClass).first();
		if (el != null) {
			for (Element valueA : el.getElementsByTag("a")) {
				list.add(StringUtils.unhtml(valueA.text()).trim());
			}
		}

		return list;
	}

	@Override
	protected String getDesc() {
		Element doc = getSourceNode();
		Element title = doc.getElementsByClass("fullcontent").first();
		if (title != null) {
			return StringUtils.unhtml(title.text()).trim();
		}

		return null;
	}

	private Image getCover() {
		Element doc = getSourceNode();
		Element cover = doc.getElementsByClass("manga-thumb").first();
		if (cover != null) {
			try {
				return bsImages.getImage(this, new URL(cover.absUrl("src")));
			} catch (MalformedURLException e) {
				Instance.getInstance().getTraceHandler().error(e);
			}
		}

		return null;
	}

	@Override
	protected List<Entry<String, URL>> getChapters(Progress pg) {
		List<Entry<String, URL>> urls = new ArrayList<Entry<String, URL>>();

		Element doc = getSourceNode();
		for (Element el : doc.getElementsByClass("list-group-item")) {
			Element urlEl = el.getElementsByTag("a").first();
			if (urlEl == null)
				continue;

			String url = urlEl.absUrl("href");

			String title = "";
			el = el.getElementsByClass("text-secondary").first();
			if (el != null) {
				title = StringUtils.unhtml(el.text()).trim();
			}

			try {
				urls.add(new AbstractMap.SimpleEntry<String, URL>(title, new URL(url)));
			} catch (Exception e) {
				Instance.getInstance().getTraceHandler().error(e);
			}
		}

		// by default, the chapters are in reversed order
		Collections.reverse(urls);

		return urls;
	}

	@Override
	protected String getChapterContent(URL chapUrl, int number, Progress pg) throws IOException {
		if (pg == null) {
			pg = new Progress();
		}

		// 1. Get the title and chapter url part
		String path = chapUrl.getPath();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - "/".length());
		}
		String tab[] = path.split("/");
		String chap = tab[tab.length - 1];
		String title = tab[tab.length - 2];

		if (chap.startsWith("chapter-")) {
			chap = chap.substring("chapter-".length());
		}

		// 2. generate an image base
		String base = "https://img.mghubcdn.com/file/imghub/" + title + "/" + chap + "/";

		// 3. add each chapter
		StringBuilder builder = new StringBuilder();

		int i = 1;
		String url = base + i + ".jpg";
		while (getHttpStatus(new URL(url)) != 404) {
			builder.append("[");
			builder.append(url);
			builder.append("]<br/>");

			i++;
			url = base + i + ".jpg";
		}

		return builder.toString();
	}

	// HTTP response code, or -1 if other error
	// TODO: move that to Downloader?
	private int getHttpStatus(URL url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			try {
				conn.setRequestMethod("HEAD");
				conn.setRequestProperty("User-Agent", Instance.getInstance().getConfig().getString(Config.NETWORK_USER_AGENT));
				conn.setRequestProperty("Accept-Encoding", "gzip");
				conn.setRequestProperty("Accept", "*/*");
				conn.setRequestProperty("Charset", "utf-8");

				return conn.getResponseCode();
			} finally {
				conn.disconnect();
			}
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	protected boolean supports(URL url) {
		return "mangahub.io".equals(url.getHost()) || "www.mangahub.io".equals(url.getHost());
	}
}
