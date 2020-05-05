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
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * Support class for <a href="https://sofurry.com/">SoFurry.com</a>, a Furry
 * website supporting images and stories (we only retrieve the stories).
 * 
 * @author niki
 */
class YiffStar extends BasicSupport_Deprecated {
	@Override
	protected MetaData getMeta(URL source, InputStream in) throws IOException {
		MetaData meta = new MetaData();

		meta.setTitle(getTitle(reset(in)));
		meta.setAuthor(getAuthor(reset(in)));
		meta.setDate("");
		meta.setTags(getTags(reset(in)));
		meta.setSource(getType().getSourceName());
		meta.setUrl(source.toString());
		meta.setPublisher(getType().getSourceName());
		meta.setUuid(source.toString());
		meta.setLuid("");
		meta.setLang("en");
		meta.setSubject("Furry");
		meta.setType(getType().toString());
		meta.setImageDocument(false);
		meta.setCover(getCover(source, reset(in)));

		return meta;
	}

	@Override
	protected boolean supports(URL url) {
		String host = url.getHost();
		if (host.startsWith("www.")) {
			host = host.substring("www.".length());
		}

		return "sofurry.com".equals(host);
	}

	@Override
	protected boolean isHtml() {
		return true;
	}

	@Override
	public void login() throws IOException {
		// Note: this should not be necessary anymore
		// (the "/guest" trick is enough)
		String login = Instance.getInstance().getConfig().getString(Config.LOGIN_YIFFSTAR_USER);
		String password = Instance.getInstance().getConfig().getString(Config.LOGIN_YIFFSTAR_PASS);

		if (login != null && !login.isEmpty() && password != null
				&& !password.isEmpty()) {

			Map<String, String> post = new HashMap<String, String>();
			post.put("LoginForm[sfLoginUsername]", login);
			post.put("LoginForm[sfLoginPassword]", password);
			post.put("YII_CSRF_TOKEN", "");
			post.put("yt1", "Login");
			post.put("returnUrl", "/");

			// Cookies will actually be retained by the cache manager once
			// logged in
			Instance.getInstance().getCache()
					.openNoCache(new URL("https://www.sofurry.com/user/login"), this, post, null, null).close();
		}
	}

	@Override
	public URL getCanonicalUrl(URL source) {
		try {
			if (source.getPath().startsWith("/view")) {
				source = guest(source.toString());
				// NO CACHE because we don't want the NotLoggedIn message later
				InputStream in = Instance.getInstance().getCache().openNoCache(source, this, null, null, null);
				String line = getLine(in, "/browse/folder/", 0);
				if (line != null) {
					String[] tab = line.split("\"");
					if (tab.length > 1) {
						String groupUrl = source.getProtocol() + "://"
								+ source.getHost() + tab[1];
						return guest(groupUrl);
					}
				}
			}
		} catch (Exception e) {
			Instance.getInstance().getTraceHandler().error(e);
		}

		return super.getCanonicalUrl(source);
	}

	private List<String> getTags(InputStream in) {
		List<String> tags = new ArrayList<String>();

		String line = getLine(in, "class=\"sf-story-big-tags", 0);
		if (line != null) {
			String[] tab = StringUtils.unhtml(line).split(",");
			for (String possibleTag : tab) {
				String tag = possibleTag.trim();
				if (!tag.isEmpty() && !tag.equals("...") && !tags.contains(tag)) {
					tags.add(tag);
				}
			}
		}

		return tags;
	}

	private Image getCover(URL source, InputStream in) throws IOException {

		List<Entry<String, URL>> chaps = getChapters(source, in, null);
		if (!chaps.isEmpty()) {
			in = Instance.getInstance().getCache().open(chaps.get(0).getValue(), this, true);
			String line = getLine(in, " name=\"og:image\"", 0);
			if (line != null) {
				int pos = -1;
				for (int i = 0; i < 3; i++) {
					pos = line.indexOf('"', pos + 1);
				}

				if (pos >= 0) {
					line = line.substring(pos + 1);
					pos = line.indexOf('"');
					if (pos >= 0) {
						line = line.substring(0, pos);
						if (line.contains("/thumb?")) {
							line = line.replace("/thumb?",
									"/auxiliaryContent?type=25&");
							return getImage(this, null, line);
						}
					}
				}
			}
		}

		return null;
	}

	private String getAuthor(InputStream in) {
		String author = getLine(in, "class=\"onlinestatus", 0);
		if (author != null) {
			return StringUtils.unhtml(author).trim();
		}

		return null;
	}

	private String getTitle(InputStream in) {
		String title = getLine(in, "class=\"sflabel pagetitle", 0);
		if (title != null) {
			if (title.contains("(series)")) {
				title = title.replace("(series)", "");
			}
			return StringUtils.unhtml(title).trim();
		}

		return null;
	}

	@Override
	protected String getDesc(URL source, InputStream in) throws IOException {
		return null; // TODO: no description at all? Cannot find one...
	}

	@Override
	protected List<Entry<String, URL>> getChapters(URL source, InputStream in,
			Progress pg) throws IOException {
		List<Entry<String, URL>> urls = new ArrayList<Entry<String, URL>>();

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\\n");
		while (scan.hasNext()) {
			String line = scan.next();
			if (line.contains("\"/view/") && line.contains("title=")) {
				String[] tab = line.split("\"");
				if (tab.length > 5) {
					String link = tab[5];
					if (link.startsWith("/")) {
						link = source.getProtocol() + "://" + source.getHost()
								+ link;
					}
					urls.add(new AbstractMap.SimpleEntry<String, URL>(
							StringUtils.unhtml(line).trim(), guest(link)));
				}
			}
		}

		return urls;
	}

	@Override
	protected String getChapterContent(URL source, InputStream in, int number,
			Progress pg) throws IOException {
		StringBuilder builder = new StringBuilder();

		String startAt = "id=\"sfContentBody";
		String endAt = "id=\"recommendationArea";
		boolean ok = false;

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		scan.useDelimiter("\\n");
		while (scan.hasNext()) {
			String line = scan.next();
			if (!ok && line.contains(startAt)) {
				ok = true;
			} else if (ok && line.contains(endAt)) {
				ok = false;
				break;
			}

			if (ok) {
				builder.append(line);
				builder.append(' ');
			}
		}

		return builder.toString();
	}

	/**
	 * Return a {@link URL} from the given link, but add the "/guest" part to it
	 * to make sure we don't need to be logged-in to see it.
	 * 
	 * @param link
	 *            the link
	 * 
	 * @return the {@link URL}
	 * 
	 * @throws MalformedURLException
	 *             in case of data error
	 */
	private URL guest(String link) throws MalformedURLException {
		if (link.contains("?")) {
			if (link.contains("/?")) {
				return new URL(link.replace("?", "guest?"));
			}

			return new URL(link.replace("?", "/guest?"));
		}

		return new URL(link + "/guest");
	}
}
