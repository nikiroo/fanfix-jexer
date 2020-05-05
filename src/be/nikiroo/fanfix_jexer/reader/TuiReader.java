package be.nikiroo.fanfix_jexer.reader;

import java.io.IOException;

import jexer.TApplication;
import jexer.TApplication.BackendType;
import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.reader.BasicReader;
import be.nikiroo.fanfix.reader.Reader;
import be.nikiroo.fanfix.supported.SupportType;

/**
 * This {@link Reader}is based upon the TUI widget library 'jexer'
 * (https://github.com/klamonte/jexer/) and offer, as its name suggest, a Text
 * User Interface.
 * <p>
 * It is expected to be on par with the GUI version.
 * 
 * @author niki
 */
public class TuiReader extends BasicReader {
	/**
	 * Will detect the backend to use.
	 * <p>
	 * Swing is the default backend on Windows and MacOS while evreything else
	 * will use XTERM unless explicitly overridden by <tt>jexer.Swing</tt> =
	 * <tt>true</tt> or <tt>false</tt>.
	 * 
	 * @return the backend to use
	 */
	private static BackendType guessBackendType() {
		// TODO: allow a config option to force one or the other?
		TApplication.BackendType backendType = TApplication.BackendType.XTERM;
		if (System.getProperty("os.name").startsWith("Windows")) {
			backendType = TApplication.BackendType.SWING;
		}

		if (System.getProperty("os.name").startsWith("Mac")) {
			backendType = TApplication.BackendType.SWING;
		}

		if (System.getProperty("jexer.Swing") != null) {
			if (System.getProperty("jexer.Swing", "false").equals("true")) {
				backendType = TApplication.BackendType.SWING;
			} else {
				backendType = TApplication.BackendType.XTERM;
			}
		}

		return backendType;
	}

	@Override
	public void read(boolean sync) throws IOException {
		// TODO
		if (!sync) {
			// How could you do a not-sync in TUI mode?
			throw new java.lang.IllegalStateException(
					"Async mode not implemented yet.");
		}

		try {
			TuiReaderApplication app = new TuiReaderApplication(this,
					guessBackendType());
			app.run();
		} catch (Exception e) {
			Instance.getInstance().getTraceHandler().error(e);
		}
	}

	@Override
	public void browse(String source) {
		try {
			TuiReaderApplication app = new TuiReaderApplication(this, source,
					guessBackendType());
			app.run();
		} catch (Exception e) {
			Instance.getInstance().getTraceHandler().error(e);
		}
	}

	@Override
	public void search(boolean sync) throws IOException {
		// TODO
		if (sync) {
			throw new java.lang.IllegalStateException("Not implemented yet.");
		}
	}

	@Override
	public void search(SupportType searchOn, String keywords, int page,
			int item, boolean sync) {
		// TODO
		if (sync) {
			throw new java.lang.IllegalStateException("Not implemented yet.");
		}
	}

	@Override
	public void searchTag(SupportType searchOn, int page, int item,
			boolean sync, Integer... tags) {
		// TODO
		if (sync) {
			throw new java.lang.IllegalStateException("Not implemented yet.");
		}
	}
}
