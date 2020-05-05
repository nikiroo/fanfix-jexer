package be.nikiroo.fanfix.supported;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

class MangaLel extends BasicSupport {
	@Override
	protected boolean isHtml() {
		return true;
	}

	@Override
	protected MetaData getMeta() throws IOException {
		MetaData meta = new MetaData();

		meta.setTitle(getTitle());
		meta.setAuthor(getAuthor());
		meta.setDate(bsHelper.formatDate(getDate()));
		meta.setTags(getTags());
		meta.setSource(getType().getSourceName());
		meta.setUrl(getSource().toString());
		meta.setPublisher(getType().getSourceName());
		meta.setUuid(getSource().toString());
		meta.setLuid("");
		meta.setLang("fr");
		meta.setSubject("manga");
		meta.setType(getType().toString());
		meta.setImageDocument(true);
		meta.setCover(getCover());

		return meta;
	}

	private String getTitle() {
		Element doc = getSourceNode();
		Element h4 = doc.getElementsByTag("h4").first();
		if (h4 != null) {
			return StringUtils.unhtml(h4.text()).trim();
		}

		return null;
	}

	private String getAuthor() {
		Element doc = getSourceNode();
		Element tabEls = doc.getElementsByClass("presentation-projet").first();
		if (tabEls != null) {
			String[] tab = tabEls.outerHtml().split("<br>");
			return getVal(tab, 1);
		}

		return "";
	}

	private List<String> getTags() {
		Element doc = getSourceNode();
		Element tabEls = doc.getElementsByClass("presentation-projet").first();
		if (tabEls != null) {
			String[] tab = tabEls.outerHtml().split("<br>");
			List<String> tags = new ArrayList<String>();
			for (String tag : getVal(tab, 3).split(" ")) {
				tags.add(tag);
			}
			return tags;
		}

		return new ArrayList<String>();

	}

	private String getDate() {
		Element doc = getSourceNode();
		Element table = doc.getElementsByClass("table").first();

		// We take the first date we find
		String value = "";
		if (table != null) {
			Elements els;
			els = table.getElementsByTag("tr");
			if (els.size() >= 2) {
				els = els.get(1).getElementsByTag("td");
				if (els.size() >= 3) {
					value = StringUtils.unhtml(els.get(2).text()).trim();
				}
			}
		}

		return value;
	}

	@Override
	protected String getDesc() {
		Element doc = getSourceNode();
		Element tabEls = doc.getElementsByClass("presentation-projet").first();
		if (tabEls != null) {
			String[] tab = tabEls.outerHtml().split("<br>");
			return getVal(tab, 4);
		}

		return "";
	}

	private Image getCover() {
		Element doc = getSourceNode();
		Element container = doc.getElementsByClass("container").first();

		if (container != null) {

			Elements imgs = container.getElementsByTag("img");
			Element img = null;
			if (imgs.size() >= 1) {
				img = imgs.get(0);
				if (img.hasClass("banniere-team-projet")) {
					img = null;
					if (imgs.size() >= 2) {
						img = imgs.get(1);
					}
				}
			}

			if (img != null) {
				String coverUrl = img.absUrl("src");

				InputStream coverIn;
				try {
					coverIn = Instance.getInstance().getCache().open(new URL(coverUrl), this, true);
					try {
						return new Image(coverIn);
					} finally {
						coverIn.close();
					}
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler().error(e);
				}
			}
		}

		return null;
	}

	private String getVal(String[] tab, int i) {
		String val = "";

		if (i < tab.length) {
			val = StringUtils.unhtml(tab[i]);
			int pos = val.indexOf(":");
			if (pos >= 0) {
				val = val.substring(pos + 1).trim();
			}
		}

		return val;
	}

	@Override
	protected List<Entry<String, URL>> getChapters(Progress pg)
			throws IOException {
		List<Entry<String, URL>> urls = new ArrayList<Entry<String, URL>>();

		Element doc = getSourceNode();
		Element table = doc.getElementsByClass("table").first();
		if (table != null) {
			for (Element tr : table.getElementsByTag("tr")) {
				Element a = tr.getElementsByTag("a").first();
				if (a != null) {
					String name = StringUtils.unhtml(a.text()).trim();
					URL url = new URL(a.absUrl("href"));
					urls.add(new AbstractMap.SimpleEntry<String, URL>(name, url));
				}
			}
		}

		return urls;
	}

	@Override
	protected String getChapterContent(URL chapUrl, int number, Progress pg)
			throws IOException {
		if (pg == null) {
			pg = new Progress();
		}

		StringBuilder builder = new StringBuilder();

		InputStream in = Instance.getInstance().getCache().open(chapUrl, this, false);
		try {
			Element pageDoc = DataUtil.load(in, "UTF-8", chapUrl.toString());
			Element content = pageDoc.getElementById("content");
			Elements linkEls = content.getElementsByTag("img");
			for (Element linkEl : linkEls) {
				if (linkEl.absUrl("src").isEmpty()) {
					continue;
				}

				builder.append("[");
				builder.append(linkEl.absUrl("src"));
				builder.append("]<br/>");
			}

		} finally {
			in.close();
		}

		return builder.toString();
	}

	@Override
	protected boolean supports(URL url) {
		// URL structure (the projectId is the manga key):
		// http://mangas-lecture-en-ligne.fr/index_lel.php?page=presentationProjet&idProjet=999

		return "mangas-lecture-en-ligne.fr".equals(url.getHost());
	}
}
