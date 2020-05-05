package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import be.nikiroo.fanfix.data.MetaData;

/**
 * Support class for <tt>.info</tt> text files ({@link Text} files with a
 * <tt>.info</tt> metadata file next to them).
 * <p>
 * The <tt>.info</tt> file is supposed to be written by this program, or
 * compatible.
 * 
 * @author niki
 */
class InfoText extends Text {
	protected File getInfoFile() {
		return new File(assureNoTxt(getSourceFile()).getPath() + ".info");
	}

	@Override
	protected MetaData getMeta() throws IOException {
		return InfoReader.readMeta(getInfoFile(), true);
	}

	@Override
	protected boolean supports(URL url) {
		return supports(url, true);
	}
}
