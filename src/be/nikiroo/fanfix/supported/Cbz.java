package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.streams.MarkableFileInputStream;

/**
 * Support class for CBZ files (works better with CBZ created with this program,
 * as they have some metadata available).
 * 
 * @author niki
 */
class Cbz extends Epub {
	@Override
	protected boolean supports(URL url) {
		return url.toString().toLowerCase().endsWith(".cbz");
	}

	@Override
	protected String getDataPrefix() {
		return "";
	}

	@Override
	protected boolean requireInfo() {
		return false;
	}

	@Override
	protected boolean isImagesDocumentByDefault() {
		return true;
	}

	@Override
	protected boolean getCover() {
		return false;
	}

	@Override
	public Story doProcess(Progress pg) throws IOException {
		if (pg == null) {
			pg = new Progress();
		} else {
			pg.setMinMax(0, 100);
		}
		
		pg.setName("Initialising");

		Progress pgMeta = new Progress();
		pg.addProgress(pgMeta, 10);
		Story story = processMeta(true, pgMeta);
		MetaData meta = story.getMeta();

		pgMeta.done(); // 10%
		
		File tmpDir = Instance.getInstance().getTempFiles().createTempDir("info-text");
		String basename = null;

		Map<String, Image> images = new HashMap<String, Image>();
		InputStream cbzIn = null;
		ZipInputStream zipIn = null;
		try {
			cbzIn = new MarkableFileInputStream(getSourceFileOriginal());
			zipIn = new ZipInputStream(cbzIn);
			for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn
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
					
					if (imageEntry) {
						String uuid = meta.getUuid() + "_" + entry.getName();
						try {
							images.put(uuid, new Image(zipIn));
						} catch (Exception e) {
							Instance.getInstance().getTraceHandler().error(e);
						}

						if (pg.getProgress() < 85) {
							pg.add(1);
						}
					} else if (entryLName.endsWith(".info")) {
						basename = entryLName.substring(0, entryLName.length()
								- ".info".length());
						IOUtils.write(zipIn, new File(tmpDir, entryLName));
					} else if (entryLName.endsWith(".txt")) {
						IOUtils.write(zipIn, new File(tmpDir, entryLName));
					}
				}
			}
			
			String ext = "."
					+ Instance.getInstance().getConfig().getString(Config.FILE_FORMAT_IMAGE_FORMAT_COVER).toLowerCase();
			String coverName = meta.getUuid() + "_" + basename + ext;
			Image cover = images.get(coverName);
			images.remove(coverName);

			pg.setProgress(85);

			// ZIP order is not correct for us
			List<String> imagesList = new ArrayList<String>(images.keySet());
			Collections.sort(imagesList);

			pg.setProgress(90);

			// only the description/cover is kept
			Story origStory = getStoryFromTxt(tmpDir, basename);
			if (origStory != null) {
				if (origStory.getMeta().getCover() == null) {
					origStory.getMeta().setCover(story.getMeta().getCover());
				}
				story.setMeta(origStory.getMeta());
			}
			if (story.getMeta().getCover() == null) {
				story.getMeta().setCover(cover);
			}
			story.setChapters(new ArrayList<Chapter>());

			// Check if we can find non-images chapters, for hybrid-cbz support
			if (origStory != null) {
				for (Chapter chap : origStory) {
					Boolean isImages = null;
					for (Paragraph para : chap) {
						ParagraphType t = para.getType();
						if (isImages == null && !t.isText(true)) {
							isImages = true;
						}
						if (t.isText(false)) {
							String line = para.getContent();
							// Images are saved in text mode as "[image-link]"
							if (!(line.startsWith("[") && line.endsWith("]"))) {
								isImages = false;
							}
						}
					}

					if (isImages != null && !isImages) {
						story.getChapters().add(chap);
						chap.setNumber(story.getChapters().size());
					}
				}
			}

			if (!imagesList.isEmpty()) {
				Chapter chap = new Chapter(story.getChapters().size() + 1, null);
				story.getChapters().add(chap);

				for (String uuid : imagesList) {
					try {
						chap.getParagraphs().add(
								new Paragraph(images.get(uuid)));
					} catch (Exception e) {
						Instance.getInstance().getTraceHandler().error(e);
					}
				}
			}

			if (meta.getCover() == null && !images.isEmpty()) {
				meta.setCover(images.get(imagesList.get(0)));
				meta.setFakeCover(true);
			}
		} finally {
			IOUtils.deltree(tmpDir);
			if (zipIn != null) {
				zipIn.close();
			}
			if (cbzIn != null) {
				cbzIn.close();
			}
		}

		pg.done();
		return story;
	}

	private Story getStoryFromTxt(File tmpDir, String basename) {
		Story origStory = null;

		File txt = new File(tmpDir, basename + ".txt");
		if (!txt.exists()) {
			basename = null;
		}
		if (basename != null) {
			try {
				BasicSupport support = BasicSupport.getSupport(txt.toURI()
						.toURL());
				origStory = support.process(null);
			} catch (Exception e) {
				basename = null;
			}
		}

		return origStory;

	}
}
