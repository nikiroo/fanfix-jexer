package be.nikiroo.fanfix.supported;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * Support class for <a href="https://e-hentai.org/">e-hentai.org</a>, a website
 * supporting mostly but not always NSFW comics, including some of MLP.
 * 
 * @author niki
 */
class EHentai extends BasicSupport_Deprecated {
	@Override
	protected MetaData getMeta(URL source, InputStream in) throws IOException {
		MetaData meta = new MetaData();

		meta.setTitle(getTitle(reset(in)));
		meta.setAuthor(getAuthor(reset(in)));
		meta.setDate(getDate(reset(in)));
		meta.setTags(getTags(reset(in)));
		meta.setSource(getType().getSourceName());
		meta.setUrl(source.toString());
		meta.setPublisher(getType().getSourceName());
		meta.setUuid(source.toString());
		meta.setLuid("");
		meta.setLang(getLang(reset(in)));
		meta.setSubject("Hentai");
		meta.setType(getType().toString());
		meta.setImageDocument(true);
		meta.setCover(getCover(source, reset(in)));
		meta.setFakeCover(true);

		return meta;
	}

	@Override
	public Story process(URL url, Progress pg) throws IOException {
		// There is no chapters on e621, just pagination...
		Story story = super.process(url, pg);

		Chapter only = new Chapter(1, "");
		for (Chapter chap : story) {
			only.getParagraphs().addAll(chap.getParagraphs());
		}

		story.getChapters().clear();
		story.getChapters().add(only);

		return story;
	}

	@Override
	protected boolean supports(URL url) {
		return "e-hentai.org".equals(url.getHost());
	}

	@Override
	protected boolean isHtml() {
		return true;
	}

	@Override
	public Map<String, String> getCookies() {
		Map<String, String> cookies = super.getCookies();
		cookies.put("nw", "1");
		return cookies;
	}

	private Image getCover(URL source, InputStream in) {
		Image author = null;
		String coverLine = getKeyLine(in, "<div id=\"gd1\"", " url(", ")");
		if (coverLine != null) {
			coverLine = StringUtils.unhtml(coverLine).trim();
			author = getImage(this, source, coverLine);
		}

		return author;
	}

	private String getAuthor(InputStream in) {
		String author = null;

		List<String> tagsAuthor = getTagsAuthor(in);
		if (!tagsAuthor.isEmpty()) {
			author = tagsAuthor.get(0);
		}

		return author;
	}

	private String getLang(InputStream in) {
		String lang = null;

		String langLine = getKeyLine(in, "class=\"gdt1\">Language",
				"class=\"gdt2\"", "</td>");
		if (langLine != null) {
			langLine = StringUtils.unhtml(langLine).trim();
			if (langLine.equalsIgnoreCase("English")) {
				lang = "en";
			} else if (langLine.equalsIgnoreCase("Japanese")) {
				lang = "jp";
			} else if (langLine.equalsIgnoreCase("French")) {
				lang = "fr";
			} else {
				// TODO find the code for other languages?
				lang = langLine;
			}
		}

		return lang;
	}

	private String getDate(InputStream in) {
		String date = null;

		String dateLine = getKeyLine(in, "class=\"gdt1\">Posted",
				"class=\"gdt2\"", "</td>");
		if (dateLine != null) {
			dateLine = StringUtils.unhtml(dateLine).trim();
			if (dateLine.length() > 10) {
				dateLine = dateLine.substring(0, 10).trim();
			}

			date = dateLine;
		}

		return date;
	}

	private List<String> getTags(InputStream in) {
		List<String> tags = new ArrayList<String>();
		List<String> tagsAuthor = getTagsAuthor(in);

		for (int i = 1; i < tagsAuthor.size(); i++) {
			tags.add(tagsAuthor.get(i));
		}

		return tags;
	}

	private List<String> getTagsAuthor(InputStream in) {
		List<String> tags = new ArrayList<String>();
		String tagLine = getKeyLine(in, "<meta name=\"description\"", "Tags: ",
				null);
		if (tagLine != null) {
			for (String tag : tagLine.split(",")) {
				String candi = tag.trim();
				if (!candi.isEmpty() && !tags.contains(candi)) {
					tags.add(candi);
				}
			}
		}

		return tags;
	}

	private String getTitle(InputStream in) {
		String siteName = " - E-Hentai Galleries";

		String title = getLine(in, "<title>", 0);
		if (title != null) {
			title = StringUtils.unhtml(title).trim();
			if (title.endsWith(siteName)) {
				title = title.substring(0, title.length() - siteName.length())
						.trim();
			}
		}

		return title;
	}

	@Override
	protected String getDesc(URL source, InputStream in) throws IOException {
		String desc = null;

		String descLine = getKeyLine(in, "Uploader Comment", null,
				"<div class=\"c7\"");
		if (descLine != null) {
			desc = StringUtils.unhtml(descLine);
		}

		return desc;
	}

	@Override
	protected List<Entry<String, URL>> getChapters(URL source, InputStream in,
			Progress pg) throws IOException {
		List<Entry<String, URL>> urls = new ArrayList<Entry<String, URL>>();
		int last = 0; // no pool/show when only one page, first page == page 0

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter(">");
		while (scan.hasNext()) {
			String line = scan.next();
			if (line.contains(source.toString())) {
				String page = line.substring(line.indexOf(source.toString()));
				String pkey = "?p=";
				if (page.contains(pkey)) {
					page = page.substring(page.indexOf(pkey) + pkey.length());
					String number = "";
					while (!page.isEmpty() && page.charAt(0) >= '0'
							&& page.charAt(0) <= '9') {
						number += page.charAt(0);
						page = page.substring(1);
					}
					if (number.isEmpty()) {
						number = "0";
					}

					int current = Integer.parseInt(number);
					if (last < current) {
						last = current;
					}
				}
			}
		}

		for (int i = 0; i <= last; i++) {
			urls.add(new AbstractMap.SimpleEntry<String, URL>(Integer
					.toString(i + 1), new URL(source.toString() + "?p=" + i)));
		}

		return urls;
	}

	@Override
	protected String getChapterContent(URL source, InputStream in, int number,
			Progress pg) throws IOException {
		String staticSite = "https://e-hentai.org/s/";
		List<URL> pages = new ArrayList<URL>();

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\"");
		while (scan.hasNext()) {
			String line = scan.next();
			if (line.startsWith(staticSite)) {
				try {
					pages.add(new URL(line));
				} catch (MalformedURLException e) {
					Instance.getInstance().getTraceHandler()
							.error(new IOException("Parsing error, a link is not correctly parsed: " + line, e));
				}
			}
		}

		if (pg == null) {
			pg = new Progress();
		}
		pg.setMinMax(0, pages.size());
		pg.setProgress(0);

		StringBuilder builder = new StringBuilder();

		for (URL page : pages) {
			InputStream pageIn = Instance.getInstance().getCache().open(page, this, false);
			try {
				String link = getKeyLine(pageIn, "id=\"img\"", "src=\"", "\"");
				if (link != null && !link.isEmpty()) {
					builder.append("[");
					builder.append(link);
					builder.append("]<br/>");
				}
				pg.add(1);
			} finally {
				if (pageIn != null) {
					pageIn.close();
				}
			}
		}

		pg.done();
		return builder.toString();
	}
}
