package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jsoup.nodes.Document;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.streams.MarkableFileInputStream;

/**
 * Support class for EPUB files created with this program (as we need some
 * metadata available in those we create).
 * 
 * @author niki
 */
class Epub extends InfoText {
	private MetaData meta;
	private File tmpDir;
	private String desc;

	private URL fakeSource;
	private InputStream fakeIn;

	public File getSourceFileOriginal() {
		return super.getSourceFile();
	}

	@Override
	protected File getSourceFile() {
		try {
			return new File(fakeSource.toURI());
		} catch (URISyntaxException e) {
			Instance.getInstance().getTraceHandler().error(new IOException(
					"Cannot get the source file from the info-text URL", e));
		}

		return null;
	}

	@Override
	protected InputStream getInput() {
		if (fakeIn != null) {
			try {
				fakeIn.reset();
			} catch (IOException e) {
				Instance.getInstance().getTraceHandler().error(new IOException(
						"Cannot reset the Epub Text stream", e));
			}

			return fakeIn;
		}

		return null;
	}

	@Override
	protected boolean supports(URL url) {
		return url.getPath().toLowerCase().endsWith(".epub");
	}

	@Override
	protected MetaData getMeta() throws IOException {
		return meta;
	}

	@Override
	protected Document loadDocument(URL source) throws IOException {
		super.loadDocument(source); // prepares super.getSourceFile() and
									// super.getInput()

		InputStream in = super.getInput();
		ZipInputStream zipIn = null;
		try {
			zipIn = new ZipInputStream(in);
			tmpDir = Instance.getInstance().getTempFiles()
					.createTempDir("fanfic-reader-parser");
			File tmp = new File(tmpDir, "file.txt");
			File tmpInfo = new File(tmpDir, "file.info");

			fakeSource = tmp.toURI().toURL();
			Image cover = null;

			String url;
			try {
				url = getSource().toURI().toURL().toString();
			} catch (URISyntaxException e1) {
				url = getSource().toString();
			}
			String title = null;
			String author = null;

			for (ZipEntry entry = zipIn
					.getNextEntry(); entry != null; entry = zipIn
							.getNextEntry()) {
				if (!entry.isDirectory()
						&& entry.getName().startsWith(getDataPrefix())) {
					String entryLName = entry.getName().toLowerCase();

					boolean imageEntry = false;
					for (String ext : bsImages.getImageExt(false)) {
						if (entryLName.endsWith(ext)) {
							imageEntry = true;
						}
					}

					if (entry.getName().equals(getDataPrefix() + "version")) {
						// Nothing to do for now ("first"
						// version is 3.0)
					} else if (entryLName.endsWith(".info")) {
						// Info file
						IOUtils.write(zipIn, tmpInfo);
					} else if (imageEntry) {
						// Cover
						if (getCover()) {
							try {
								cover = new Image(zipIn);
							} catch (Exception e) {
								Instance.getInstance().getTraceHandler()
										.error(e);
							}
						}
					} else if (entry.getName()
							.equals(getDataPrefix() + "URL")) {
						String[] descArray = StringUtils
								.unhtml(IOUtils.readSmallStream(zipIn)).trim()
								.split("\n");
						if (descArray.length > 0) {
							url = descArray[0].trim();
						}
					} else if (entry.getName().endsWith(".desc")) {
						// // For old files
						// if (this.desc != null) {
						// this.desc = IOUtils.readSmallStream(zipIn).trim();
						// }
					} else if (entry.getName()
							.equals(getDataPrefix() + "SUMMARY")) {
						String[] descArray = StringUtils
								.unhtml(IOUtils.readSmallStream(zipIn)).trim()
								.split("\n");
						int skip = 0;
						if (descArray.length > 1) {
							title = descArray[0].trim();
							skip = 1;
							if (descArray.length > 2
									&& descArray[1].startsWith("©")) {
								author = descArray[1].substring(1).trim();
								skip = 2;
							}
						}
						// this.desc = "";
						// for (int i = skip; i < descArray.length; i++) {
						// this.desc += descArray[i].trim() + "\n";
						// }
						//
						// this.desc = this.desc.trim();
					} else {
						// Hopefully the data file
						IOUtils.write(zipIn, tmp);
					}
				}
			}

			if (requireInfo() && (!tmp.exists() || !tmpInfo.exists())) {
				throw new IOException(
						"file not supported (maybe not created with this program or corrupt)");
			}

			if (tmp.exists()) {
				this.fakeIn = new MarkableFileInputStream(tmp);
			}

			if (tmpInfo.exists()) {
				meta = InfoReader.readMeta(tmpInfo, true);
				tmpInfo.delete();
			} else {
				if (title == null || title.isEmpty()) {
					title = getSourceFileOriginal().getName();
					if (title.toLowerCase().endsWith(".cbz")) {
						title = title.substring(0, title.length() - 4);
					}
					title = URLDecoder.decode(title, "UTF-8").trim();
				}

				meta = new MetaData();
				meta.setLang("en");
				meta.setTags(new ArrayList<String>());
				meta.setSource(getType().getSourceName());
				meta.setUuid(url);
				meta.setUrl(url);
				meta.setTitle(title);
				meta.setAuthor(author);
				meta.setImageDocument(isImagesDocumentByDefault());
			}

			if (meta.getCover() == null) {
				if (cover != null) {
					meta.setCover(cover);
				} else {
					meta.setCover(InfoReader.getCoverByName(
							getSourceFileOriginal().toURI().toURL()));
				}
			}
		} finally {
			if (zipIn != null) {
				zipIn.close();
			}
			if (in != null) {
				in.close();
			}
		}

		return null;
	}

	@Override
	protected void close() {
		if (tmpDir != null) {
			IOUtils.deltree(tmpDir);
		}

		tmpDir = null;

		super.close();
	}

	protected String getDataPrefix() {
		return "DATA/";
	}

	protected boolean requireInfo() {
		return true;
	}

	protected boolean getCover() {
		return true;
	}

	protected boolean isImagesDocumentByDefault() {
		return false;
	}
}
