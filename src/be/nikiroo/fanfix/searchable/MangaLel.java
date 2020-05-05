package be.nikiroo.fanfix.searchable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.supported.SupportType;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.StringUtils;

class MangaLel extends BasicSearchable {
	private String BASE_URL = "http://mangas-lecture-en-ligne.fr/index_lel.php";

	public MangaLel() {
		super(SupportType.MANGA_LEL);
	}

	@Override
	public List<SearchableTag> getTags() throws IOException {
		List<SearchableTag> tags = new ArrayList<SearchableTag>();

		String url = BASE_URL + "?page=recherche";
		Document doc = load(url, false);

		Element genre = doc.getElementsByClass("genre").first();
		if (genre != null) {
			for (Element el : genre.getElementsByAttributeValueStarting("for",
					"genre")) {
				tags.add(new SearchableTag(el.attr("for"), el.text(), true));
			}
		}

		return tags;
	}

	@Override
	public void fillTag(SearchableTag tag) throws IOException {
		// Tags are always complete
	}

	@Override
	public List<MetaData> search(String search, int page) throws IOException {
		String url = BASE_URL + "?nomProjet="
				+ URLEncoder.encode(search, "utf-8")
				+ "&nomAuteur=&nomTeam=&page=recherche&truc=truc";

		// No pagination
		return getResults(url);
	}

	@Override
	public List<MetaData> search(SearchableTag tag, int page)
			throws IOException {
		String url = BASE_URL + "?nomProjet=&nomAuteur=&nomTeam=&"
				+ tag.getId() + "=on&page=recherche&truc=truc";

		// No pagination
		return getResults(url);
	}

	@Override
	public int searchPages(String search) throws IOException {
		// No pagination
		return 1;
	}

	@Override
	public int searchPages(SearchableTag tag) throws IOException {
		if (tag.isLeaf()) {
			// No pagination
			return 1;
		}

		return 0;
	}

	private List<MetaData> getResults(String sourceUrl) throws IOException {
		List<MetaData> metas = new ArrayList<MetaData>();

		Document doc = DataUtil.load(Instance.getInstance().getCache().open(new URL(sourceUrl), getSupport(), false),
				"UTF-8", sourceUrl);

		for (Element result : doc.getElementsByClass("rechercheAffichage")) {
			Element a = result.getElementsByTag("a").first();
			if (a != null) {
				int projectId = -1;

				MetaData meta = new MetaData();

				// Target:
				// http://mangas-lecture-en-ligne.fr/index_lel.php?page=presentationProjet&idProjet=218

				// a.absUrl("href"):
				// http://mangas-lecture-en-ligne.fr/index_lel?onCommence=oui&idChapitre=2805

				// ...but we need the PROJECT id, not the CHAPTER id -> use
				// <IMG>

				Elements infos = result.getElementsByClass("texte");
				if (infos != null) {
					String[] tab = infos.outerHtml().split("<br>");

					meta.setLang("fr");
					meta.setSource(getType().getSourceName());
					meta.setPublisher(getType().getSourceName());
					meta.setType(getType().toString());
					meta.setSubject("manga");
					meta.setImageDocument(true);
					meta.setTitle(getVal(tab, 0));
					meta.setAuthor(getVal(tab, 1));
					meta.setTags(Arrays.asList(getVal(tab, 2).split(" ")));

					meta.setResume(getSupport().makeChapter(new URL(sourceUrl), 0,
							Instance.getInstance().getTrans().getString(StringId.DESCRIPTION), getVal(tab, 5)));
				}

				Element img = result.getElementsByTag("img").first();
				if (img != null) {
					try {
						String[] tab = img.attr("src").split("/");
						String str = tab[tab.length - 1];
						tab = str.split("\\.");
						str = tab[0];
						projectId = Integer.parseInt(str);

						String coverUrl = img.absUrl("src");
						try {
							InputStream in = Instance.getInstance().getCache().open(new URL(coverUrl), getSupport(),
									true);
							try {
								meta.setCover(new Image(in));
							} finally {
								in.close();
							}
						} catch (Exception e) {
							// Happen often on MangaLEL...
							Instance.getInstance().getTraceHandler().trace(
									"Cannot download cover for MangaLEL story in search mode: "	+ meta.getTitle());
						}
					} catch (Exception e) {
						// no project id... cannot use the story :(
						Instance.getInstance().getTraceHandler()
								.error("Cannot find ProjectId for MangaLEL story in search mode: " + meta.getTitle());
					}
				}

				if (projectId >= 0) {
					meta.setUrl("http://mangas-lecture-en-ligne.fr/index_lel.php?page=presentationProjet&idProjet="
							+ projectId);
					meta.setUuid(meta.getUrl());
					metas.add(meta);
				}
			}
		}

		return metas;
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
}
