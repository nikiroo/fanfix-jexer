package be.nikiroo.fanfix.supported;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * Support class for <a href="http://www.fimfiction.net/">FimFiction.net</a>
 * stories, a website dedicated to My Little Pony.
 * 
 * @author niki
 */
class Fimfiction extends BasicSupport_Deprecated {
	@Override
	protected boolean isHtml() {
		return true;
	}

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
		meta.setLang("en");
		meta.setSubject("MLP");
		meta.setType(getType().toString());
		meta.setImageDocument(false);
		meta.setCover(getCover(reset(in)));

		return meta;
	}

	@Override
	public Map<String, String> getCookies() {
		Map<String, String> cookies = new HashMap<String, String>();
		cookies.put("view_mature", "true");
		return cookies;
	}

	private List<String> getTags(InputStream in) {
		List<String> tags = new ArrayList<String>();
		tags.add("MLP");

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\\n");
		boolean started = false;
		while (scan.hasNext()) {
			String line = scan.next();

			if (!started) {
				started = line.contains("\"story_container\"");
			}

			if (started && line.contains("class=\"tag-")) {
				if (line.contains("index.php")) {
					break; // end of *this story* tags
				}

				String keyword = "title=\"";
				Scanner tagScanner = new Scanner(line);
				tagScanner.useDelimiter(keyword);
				if (tagScanner.hasNext()) {
					tagScanner.next();// Ignore first one
				}
				while (tagScanner.hasNext()) {
					String tag = tagScanner.next();
					if (tag.contains("\"")) {
						tag = tag.split("\"")[0];
						tag = StringUtils.unhtml(tag).trim();
						if (!tag.isEmpty() && !tags.contains(tag)) {
							tags.add(tag);
						}
					}
				}
				tagScanner.close();
			}
		}

		return tags;
	}

	private String getTitle(InputStream in) {
		String line = getLine(in, " property=\"og:title\"", 0);
		if (line != null) {
			int pos = -1;
			for (int i = 0; i < 3; i++) {
				pos = line.indexOf('"', pos + 1);
			}

			if (pos >= 0) {
				line = line.substring(pos + 1);
				pos = line.indexOf('"');
				if (pos >= 0) {
					return StringUtils.unhtml(line.substring(0, pos)).trim();
				}
			}
		}

		return null;
	}

	private String getAuthor(InputStream in) {
		String line = getLine(in, " href=\"/user/", 0);
		if (line != null) {
			int pos = line.indexOf('"');
			if (pos >= 0) {
				line = line.substring(pos + 1);
				pos = line.indexOf('"');
				if (pos >= 0) {
					line = line.substring(0, pos);
					pos = line.lastIndexOf('/');
					if (pos >= 0) {
						line = line.substring(pos + 1);
						return line.replace('+', ' ');
					}
				}
			}
		}

		return null;
	}

	private String getDate(InputStream in) {
		String line = getLine(in, "<span class=\"date\">", 0);
		if (line != null) {
			int pos = -1;
			for (int i = 0; i < 3; i++) {
				pos = line.indexOf('>', pos + 1);
			}

			if (pos >= 0) {
				line = line.substring(pos + 1);
				pos = line.indexOf('<');
				if (pos >= 0) {
					return line.substring(0, pos).trim();
				}
			}
		}

		return null;
	}

	@Override
	protected String getDesc(URL source, InputStream in) {
		// the og: meta version is the SHORT resume, this is the LONG resume
		return getLine(in, "class=\"description-text bbcode\"", 1);
	}

	private Image getCover(InputStream in) {
		// Note: the 'og:image' is the SMALL cover, not the full version
		String cover = getLine(in, "class=\"story_container__story_image\"", 1);
		if (cover != null) {
			int pos = cover.indexOf('"');
			if (pos >= 0) {
				cover = cover.substring(pos + 1);
				pos = cover.indexOf('"');
				if (pos >= 0) {
					cover = cover.substring(0, pos);
				}
			}
		}

		return getImage(this, null, cover);
	}

	@Override
	protected List<Entry<String, URL>> getChapters(URL source, InputStream in,
			Progress pg) {
		List<Entry<String, URL>> urls = new ArrayList<Entry<String, URL>>();
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\\n");
		boolean started = false;
		while (scan.hasNext()) {
			String line = scan.next().trim();

			if (!started) {
				started = line.equals("<!--Chapters-->");
			} else {
				if (line.equals("</form>")) {
					break;
				}

				if (line.startsWith("<a href=")
						|| line.contains("class=\"chapter-title\"")) {
					// Chapter name
					String name = line;
					int pos = name.indexOf('>');
					if (pos >= 0) {
						name = name.substring(pos + 1);
						pos = name.indexOf('<');
						if (pos >= 0) {
							name = name.substring(0, pos);
						}
					}
					// Chapter content
					pos = line.indexOf('/');
					if (pos >= 0) {
						line = line.substring(pos); // we take the /, not +1
						pos = line.indexOf('"');
						if (pos >= 0) {
							line = line.substring(0, pos);
						}
					}

					try {
						urls.add(new AbstractMap.SimpleEntry<String, URL>(name,
								new URL("http://www.fimfiction.net" + line)));
					} catch (MalformedURLException e) {
						Instance.getInstance().getTraceHandler().error(e);
					}
				}
			}
		}

		return urls;
	}

	@Override
	protected String getChapterContent(URL source, InputStream in, int number,
			Progress pg) {
		return getLine(in, "<div class=\"bbcode\">", 1);
	}

	@Override
	protected boolean supports(URL url) {
		return "fimfiction.net".equals(url.getHost())
				|| "www.fimfiction.net".equals(url.getHost());
	}
}
