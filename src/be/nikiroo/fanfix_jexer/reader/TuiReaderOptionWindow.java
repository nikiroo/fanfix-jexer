package be.nikiroo.fanfix_jexer.reader;

import jexer.TStatusBar;
import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.bundles.UiConfig;

class TuiReaderOptionWindow extends TOptionWindow {
	public TuiReaderOptionWindow(TuiReaderApplication reader, boolean uiOptions) {
		super(reader, uiOptions ? UiConfig.class : Config.class,
				uiOptions ? Instance.getInstance().getUiConfig() : Instance.getInstance().getConfig(), "Options");

		TStatusBar statusBar = reader.setStatusBar(this, "Options");
	}
}
