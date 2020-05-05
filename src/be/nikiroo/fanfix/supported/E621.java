package be.nikiroo.fanfix.supported;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.Version;

/**
 * Support class for <a href="http://e621.net/">e621.net</a> and
 * <a href="http://e926.net/">e926.net</a>, a Furry website supporting comics,
 * including some of MLP.
 * <p>
 * <a href="http://e926.net/">e926.net</a> only shows the "clean" images and
 * comics, but it can be difficult to browse.
 * 
 * @author niki
 */
class E621 extends BasicSupport {
	@Override
	protected boolean supports(URL url) {
		String host = url.getHost();
		if (host.startsWith("www.")) {
			host = host.substring("www.".length());
		}

		return ("e621.net".equals(host) || "e926.net".equals(host))
				&& (isPool(url) || isSearchOrSet(url));
	}

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
		meta.setLang("en");
		meta.setSubject("Furry");
		meta.setType(getType().toString());
		meta.setImageDocument(true);
		meta.setCover(getCover());
		meta.setFakeCover(true);

		return meta;
	}

	@Override
	protected String getDesc() throws IOException {
		if (isSearchOrSet(getSource())) {
			StringBuilder builder = new StringBuilder();
			builder.append("A collection of images from ")
					.append(getSource().getHost()).append("\n") //
					.append("\tTime of creation: "
							+ StringUtils.fromTime(new Date().getTime()))
					.append("\n") //
					.append("\tTags: ");//
			for (String tag : getTags()) {
				builder.append("\t\t").append(tag);
			}

			return builder.toString();
		}

		if (isPool(getSource())) {
			Element el = getSourceNode().getElementById("description");
			if (el != null) {
				return el.text();
			}
		}

		return null;
	}

	@Override
	protected List<Entry<String, URL>> getChapters(Progress pg)
			throws IOException {
		int i = 1;
		String jsonUrl = getJsonUrl();
		if (jsonUrl != null) {
			for (i = 1; true; i++) {
				if (i > 1) {
					try {
						// The API does not accept more than 2 request per sec,
						// and asks us to limit at one per sec when possible
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				try {
					JSONObject json = getJson(jsonUrl + "&page=" + i, false);
					if (!json.has("posts"))
						break;
					JSONArray posts = json.getJSONArray("posts");
					if (posts.isEmpty())
						break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// The last page was empty:
			i--;
		}

		// The pages and images are in reverse order on /posts/
		List<Entry<String, URL>> chapters = new LinkedList<Entry<String, URL>>();
		for (int page = i; page > 0; page--) {
			chapters.add(new AbstractMap.SimpleEntry<String, URL>(
					"Page " + Integer.toString(i - page + 1),
					new URL(jsonUrl + "&page=" + page)));
		}

		return chapters;
	}

	@Override
	protected String getChapterContent(URL chapUrl, int number, Progress pg)
			throws IOException {
		StringBuilder builder = new StringBuilder();

		JSONObject json = getJson(chapUrl, false);
		JSONArray postsArr = json.getJSONArray("posts");

		// The pages and images are in reverse order on /posts/
		List<JSONObject> posts = new ArrayList<JSONObject>(postsArr.length());
		for (int i = postsArr.length() - 1; i >= 0; i--) {
			Object o = postsArr.get(i);
			if (o instanceof JSONObject)
				posts.add((JSONObject) o);
		}

		for (JSONObject post : posts) {
			if (!post.has("file"))
				continue;
			JSONObject file = post.getJSONObject("file");
			if (!file.has("url"))
				continue;

			try {
				String url = file.getString("url");
				builder.append("[");
				builder.append(url);
				builder.append("]<br/>");
			} catch (JSONException e) {
				// Can be NULL if filtered
				// When the value is NULL, we get an exception
				// but the "has" method still returns true
				Instance.getInstance().getTraceHandler()
						.error("Cannot get image for chapter " + number + " of "
								+ getSource());
			}
		}

		return builder.toString();
	}

	@Override
	protected URL getCanonicalUrl(URL source) {
		// Convert search-pools into proper pools
		if (source.getPath().equals("/posts") && source.getQuery() != null
				&& source.getQuery().startsWith("tags=pool%3A")) {
			String poolNumber = source.getQuery()
					.substring("tags=pool%3A".length());
			try {
				Integer.parseInt(poolNumber);
				String base = source.getProtocol() + "://" + source.getHost();
				if (source.getPort() != -1) {
					base = base + ":" + source.getPort();
				}
				source = new URL(base + "/pools/" + poolNumber);
			} catch (NumberFormatException e) {
				// Not a simple pool, skip
			} catch (MalformedURLException e) {
				// Cannot happen
			}
		}

		if (isSetOriginalUrl(source)) {
			try {
				Document doc = DataUtil.load(Instance.getInstance().getCache()
						.open(source, this, false), "UTF-8", source.toString());
				for (Element shortname : doc
						.getElementsByClass("set-shortname")) {
					for (Element el : shortname.getElementsByTag("a")) {
						if (!el.attr("href").isEmpty())
							return new URL(el.absUrl("href"));
					}
				}
			} catch (IOException e) {
				Instance.getInstance().getTraceHandler().error(e);
			}
		}

		if (isPool(source)) {
			try {
				return new URL(
						source.toString().replace("/pool/show/", "/pools/"));
			} catch (MalformedURLException e) {
			}
		}

		return super.getCanonicalUrl(source);
	}

	private String getTitle() {
		String title = "";

		Element el = getSourceNode().getElementsByTag("title").first();
		if (el != null) {
			title = el.text().trim();
		}

		for (String s : new String[] { "e621", "-", "e621", "Pool", "-" }) {
			if (title.startsWith(s)) {
				title = title.substring(s.length()).trim();
			}
			if (title.endsWith(s)) {
				title = title.substring(0, title.length() - s.length()).trim();
			}
		}

		if (isSearchOrSet(getSource())) {
			title = title.isEmpty() ? "e621" : "[e621] " + title;
		}

		return title;
	}

	private String getAuthor() {
		List<String> list = new ArrayList<String>();
		String jsonUrl = getJsonUrl();
		if (jsonUrl != null) {
			try {
				JSONObject json = getJson(jsonUrl, false);
				JSONArray posts = json.getJSONArray("posts");
				for (Object obj : posts) {
					if (!(obj instanceof JSONObject))
						continue;

					JSONObject post = (JSONObject) obj;
					if (!post.has("tags"))
						continue;

					JSONObject tags = post.getJSONObject("tags");
					if (!tags.has("artist"))
						continue;

					JSONArray artists = tags.getJSONArray("artist");
					for (Object artist : artists) {
						if (list.contains(artist.toString()))
							continue;

						list.add(artist.toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		StringBuilder builder = new StringBuilder();
		for (String artist : list) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(artist);
		}

		return builder.toString();
	}

	private String getDate() {
		String jsonUrl = getJsonUrl();
		if (jsonUrl != null) {
			try {
				JSONObject json = getJson(jsonUrl, false);
				JSONArray posts = json.getJSONArray("posts");
				for (Object obj : posts) {
					if (!(obj instanceof JSONObject))
						continue;

					JSONObject post = (JSONObject) obj;
					if (!post.has("created_at"))
						continue;

					return post.getString("created_at");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	// no tags for pools
	private List<String> getTags() {
		List<String> tags = new ArrayList<String>();
		if (isSearchOrSet(getSource())) {
			String str = getTagsFromUrl(getSource());
			for (String tag : str.split("\\+")) {
				try {
					tags.add(URLDecoder.decode(tag.trim(), "UTF-8").trim());
				} catch (UnsupportedEncodingException e) {
				}
			}
		}

		return tags;
	}

	// returns "xxx+ddd+ggg" if "tags=xxx+ddd+ggg" was present in the query
	private String getTagsFromUrl(URL url) {
		String tags = url == null ? "" : url.getQuery();
		int pos = tags.indexOf("tags=");

		if (pos >= 0) {
			tags = tags.substring(pos).substring("tags=".length());
		} else {
			return "";
		}

		pos = tags.indexOf('&');
		if (pos > 0) {
			tags = tags.substring(0, pos);
		}
		pos = tags.indexOf('/');
		if (pos > 0) {
			tags = tags.substring(0, pos);
		}

		return tags;
	}

	private Image getCover() throws IOException {
		Image image = null;
		List<Entry<String, URL>> chapters = getChapters(null);
		if (!chapters.isEmpty()) {
			URL chap1Url = chapters.get(0).getValue();
			String imgsChap1 = getChapterContent(chap1Url, 1, null);
			if (!imgsChap1.isEmpty()) {
				imgsChap1 = imgsChap1.split("]")[0].substring(1).trim();
				image = bsImages.getImage(this, new URL(imgsChap1));
			}
		}

		return image;
	}

	// always /posts.json/ url
	private String getJsonUrl() {
		String url = null;
		if (isSearchOrSet(getSource())) {
			url = getSource().toString().replace("/posts", "/posts.json");
		}

		if (isPool(getSource())) {
			String poolNumber = getSource().getPath()
					.substring("/pools/".length());
			url = "https://e621.net/posts.json" + "?tags=pool%3A" + poolNumber;
		}

		if (url != null) {
			// Note: one way to override the blacklist
			String login = Instance.getInstance().getConfig()
					.getString(Config.LOGIN_E621_LOGIN);
			String apk = Instance.getInstance().getConfig()
					.getString(Config.LOGIN_E621_APIKEY);

			if (login != null && !login.isEmpty() && apk != null
					&& !apk.isEmpty()) {
				url = String.format("%s&login=%s&api_key=%s&_client=%s", url,
						login, apk, "fanfix-" + Version.getCurrentVersion());
			}
		}

		return url;
	}

	// note: will be removed at getCanonicalUrl()
	private boolean isSetOriginalUrl(URL originalUrl) {
		return originalUrl.getPath().startsWith("/post_sets/");
	}

	private boolean isPool(URL url) {
		return url.getPath().startsWith("/pools/")
				|| url.getPath().startsWith("/pool/show/");
	}

	// set will be renamed into search by canonical url
	private boolean isSearchOrSet(URL url) {
		return
		// search:
		(url.getPath().equals("/posts") && url.getQuery().contains("tags="))
				// or set:
				|| isSetOriginalUrl(url);
	}
}
