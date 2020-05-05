package be.nikiroo.fanfix.supported;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * Support class for <a href="http://www.fanfiction.net/">Faniction.net</a>
 * stories, a website dedicated to fanfictions of many, many different
 * universes, from TV shows to novels to games.
 * 
 * @author niki
 */
class Fanfiction extends BasicSupport_Deprecated {
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
		meta.setLang("en"); // TODO!
		meta.setSubject(getSubject(reset(in)));
		meta.setType(getType().toString());
		meta.setImageDocument(false);
		meta.setCover(getCover(source, reset(in)));

		return meta;
	}

	private String getSubject(InputStream in) {
		String line = getLine(in, "id=pre_story_links", 0);
		if (line != null) {
			int pos = line.lastIndexOf('"');
			if (pos >= 1) {
				line = line.substring(pos + 1);
				pos = line.indexOf('<');
				if (pos >= 0) {
					return StringUtils.unhtml(line.substring(0, pos)).trim();
				}
			}
		}

		return null;
	}

	private List<String> getTags(InputStream in) {
		List<String> tags = new ArrayList<String>();

		String key = "title=\"Send Private Message\"";
		String line = getLine(in, key, 2);
		if (line != null) {
			key = "Rated:";
			int pos = line.indexOf(key);
			if (pos >= 0) {
				line = line.substring(pos + key.length());
				key = "Chapters:";
				pos = line.indexOf(key);
				if (pos >= 0) {
					line = line.substring(0, pos);
					line = StringUtils.unhtml(line).trim();
					if (line.endsWith("-")) {
						line = line.substring(0, line.length() - 1);
					}

					for (String tag : line.split("-")) {
						tags.add(StringUtils.unhtml(tag).trim());
					}
				}
			}
		}

		return tags;
	}

	private String getTitle(InputStream in) {
		int i = 0;
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\\n");
		while (scan.hasNext()) {
			String line = scan.next();
			if (line.contains("xcontrast_txt")) {
				if ((++i) == 2) {
					line = StringUtils.unhtml(line).trim();
					if (line.startsWith("Follow/Fav")) {
						line = line.substring("Follow/Fav".length()).trim();
					}

					return StringUtils.unhtml(line).trim();
				}
			}
		}

		return "";
	}

	private String getAuthor(InputStream in) {
		String author = null;

		int i = 0;
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\\n");
		while (scan.hasNext()) {
			String line = scan.next();
			if (line.contains("xcontrast_txt")) {
				if ((++i) == 3) {
					author = StringUtils.unhtml(line).trim();
					break;
				}
			}
		}

		return bsHelper.fixAuthor(author);
	}

	private String getDate(InputStream in) {
		String key = "Published: <span data-xutime='";
		String line = getLine(in, key, 0);
		if (line != null) {
			int pos = line.indexOf(key);
			if (pos >= 0) {
				line = line.substring(pos + key.length());
				pos = line.indexOf('\'');
				if (pos >= 0) {
					line = line.substring(0, pos).trim();
					try {
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd");
						return sdf
								.format(new Date(1000 * Long.parseLong(line)));
					} catch (NumberFormatException e) {
						Instance.getInstance().getTraceHandler()
								.error(new IOException("Cannot convert publication date: " + line, e));
					}
				}
			}
		}

		return null;
	}

	@Override
	protected String getDesc(URL source, InputStream in) {
		return getLine(in, "title=\"Send Private Message\"", 1);
	}

	private Image getCover(URL url, InputStream in) {
		String key = "class='cimage";
		String line = getLine(in, key, 0);
		if (line != null) {
			int pos = line.indexOf(key);
			if (pos >= 0) {
				line = line.substring(pos + key.length());
				key = "src='";
				pos = line.indexOf(key);
				if (pos >= 0) {
					line = line.substring(pos + key.length());
					pos = line.indexOf('\'');
					if (pos >= 0) {
						line = line.substring(0, pos);
						if (line.startsWith("//")) {
							line = url.getProtocol() + "://"
									+ line.substring(2);
						} else if (line.startsWith("//")) {
							line = url.getProtocol() + "://" + url.getHost()
									+ "/" + line.substring(1);
						} else {
							line = url.getProtocol() + "://" + url.getHost()
									+ "/" + url.getPath() + "/" + line;
						}

						return getImage(this, null, line);
					}
				}
			}
		}

		return null;
	}

	@Override
	protected List<Entry<String, URL>> getChapters(URL source, InputStream in,
			Progress pg) {
		List<Entry<String, URL>> urls = new ArrayList<Entry<String, URL>>();

		String base = source.toString();
		int pos = base.lastIndexOf('/');
		String suffix = base.substring(pos); // including '/' at start
		base = base.substring(0, pos);
		if (base.endsWith("/1")) {
			base = base.substring(0, base.length() - 1); // including '/' at end
		}

		String line = getLine(in, "id=chap_select", 0);
		String key = "<option  value=";
		int i = 1;

		if (line != null) {
			for (pos = line.indexOf(key); pos >= 0; pos = line
					.indexOf(key, pos), i++) {
				pos = line.indexOf('>', pos);
				if (pos >= 0) {
					int endOfName = line.indexOf('<', pos);
					if (endOfName >= 0) {
						String name = line.substring(pos + 1, endOfName);
						String chapNum = i + ".";
						if (name.startsWith(chapNum)) {
							name = name.substring(chapNum.length(),
									name.length());
						}

						try {
							urls.add(new AbstractMap.SimpleEntry<String, URL>(
									name.trim(), new URL(base + i + suffix)));
						} catch (MalformedURLException e) {
							Instance.getInstance().getTraceHandler().error(
									new IOException("Cannot parse chapter " + i + " url: " + (base + i + suffix), e));
						}
					}
				}
			}
		} else {
			// only one chapter:
			final String chapName = getTitle(reset(in));
			final URL chapURL = source;
			urls.add(new Entry<String, URL>() {
				@Override
				public URL setValue(URL value) {
					return null;
				}

				@Override
				public URL getValue() {
					return chapURL;
				}

				@Override
				public String getKey() {
					return chapName;
				}
			});
		}

		return urls;
	}

	@Override
	protected String getChapterContent(URL source, InputStream in, int number,
			Progress pg) {
		StringBuilder builder = new StringBuilder();
		String startAt = "class='storytext ";
		String endAt1 = "function review_init";
		String endAt2 = "id=chap_select";
		boolean ok = false;

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\\n");
		while (scan.hasNext()) {
			String line = scan.next();
			if (!ok && line.contains(startAt)) {
				ok = true;
			} else if (ok && (line.contains(endAt1) || line.contains(endAt2))) {
				ok = false;
				break;
			}

			if (ok) {
				// First line may contain the title and chap name again
				if (builder.length() == 0) {
					int pos = line.indexOf("<hr");
					if (pos >= 0) {
						boolean chaptered = false;
						for (String lang : Instance.getInstance().getConfig().getList(Config.CONF_CHAPTER)) {
							String chapterWord = Instance.getInstance().getConfig().getStringX(Config.CONF_CHAPTER,
									lang);
							int posChap = line.indexOf(chapterWord + " ");
							if (posChap < pos) {
								chaptered = true;
								break;
							}
						}

						if (chaptered) {
							line = line.substring(pos);
						}
					}
				}

				builder.append(line);
				builder.append(' ');
			}
		}

		return builder.toString();
	}

	@Override
	protected boolean supports(URL url) {
		return "fanfiction.net".equals(url.getHost())
				|| "www.fanfiction.net".equals(url.getHost());
	}
}
