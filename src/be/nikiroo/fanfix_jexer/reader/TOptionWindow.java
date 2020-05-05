package be.nikiroo.fanfix_jexer.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jexer.TAction;
import jexer.TApplication;
import jexer.TPanel;
import jexer.TWidget;
import jexer.event.TCommandEvent;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.MetaInfo;

public class TOptionWindow<E extends Enum<E>> extends TSimpleScrollableWindow {
	private List<MetaInfo<E>> items;

	public TOptionWindow(TApplication parent, Class<E> type,
			final Bundle<E> bundle, String title) {
		super(parent, title, 0, 0, CENTERED | RESIZABLE);

		getMainPane().addLabel(title, 0, 0);

		items = new ArrayList<MetaInfo<E>>();
		List<MetaInfo<E>> groupedItems = MetaInfo.getItems(type, bundle);
		int y = 2;
		for (MetaInfo<E> item : groupedItems) {
			// will populate this.items
			y += addItem(getMainPane(), 5, y, item, 0).getHeight();
		}

		y++;

		setRealHeight(y + 1);

		getMainPane().addButton("Reset", 25, y, new TAction() {
			@Override
			public void DO() {
				for (MetaInfo<E> item : items) {
					item.reload();
				}
			}
		});

		getMainPane().addButton("Default", 15, y, new TAction() {
			@Override
			public void DO() {
				Object snap = bundle.takeSnapshot();
				bundle.reload(true);
				for (MetaInfo<E> item : items) {
					item.reload();
				}
				bundle.reload(false);
				bundle.restoreSnapshot(snap);
			}
		});

		getMainPane().addButton("Save", 1, y, new TAction() {
			@Override
			public void DO() {
				for (MetaInfo<E> item : items) {
					item.save(true);
				}

				try {
					bundle.updateFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private TWidget addItem(TWidget parent, int x, int y, MetaInfo<E> item,
			int nhgap) {
		if (item.isGroup()) {
			// TODO: width
			int w = 80 - x;

			String name = item.getName();
			String info = item.getDescription();
			info = StringUtils.justifyTexts(info, w - 3); // -3 for borders

			final TPanel pane = new TPanel(parent, x, y, w, 1);
			pane.addLabel(name, 0, 0);

			int h = 0;
			if (!info.isEmpty()) {
				h += info.split("\n").length + 1; // +1 for scroll
				pane.addText(info + "\n", 0, 1, w, h);
			}

			// +1 for the title
			h++;

			int paneY = h; // for the info desc
			for (MetaInfo<E> subitem : item) {
				paneY += addItem(pane, 4, paneY, subitem, nhgap + 11)
						.getHeight();
			}

			pane.setHeight(paneY);
			return pane;
		}

		items.add(item);
		return ConfigItem.createItem(parent, x, y, item, nhgap);
	}

	@Override
	public void onCommand(TCommandEvent command) {
		if (command.getCmd().equals(TuiReaderApplication.CMD_EXIT)) {
			TuiReaderApplication.close(this);
		} else {
			// Handle our own event if needed here
			super.onCommand(command);
		}
	}
}
