package be.nikiroo.fanfix_jexer.reader;

import jexer.TApplication;
import jexer.THScroller;
import jexer.TPanel;
import jexer.TScrollableWindow;
import jexer.TVScroller;
import jexer.TWidget;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;

public class TSimpleScrollableWindow extends TScrollableWindow {
	protected TPanel mainPane;
	private int prevHorizontal = -1;
	private int prevVertical = -1;

	public TSimpleScrollableWindow(TApplication application, String title,
			int width, int height) {
		this(application, title, width, height, 0, 0, 0);
	}

	public TSimpleScrollableWindow(TApplication application, String title,
			int width, int height, int flags) {
		this(application, title, width, height, flags, 0, 0);
	}

	// 0 = none (so, no scrollbar)
	public TSimpleScrollableWindow(TApplication application, String title,
			int width, int height, int flags, int realWidth, int realHeight) {
		super(application, title, width, height, flags);

		mainPane = new TPanel(this, 0, 0, 1, 1) {
			@Override
			public void draw() {
				for (TWidget children : mainPane.getChildren()) {
					int y = children.getY() + children.getHeight();
					int x = children.getX() + children.getWidth();
					boolean visible = (y > getVerticalValue())
							&& (x > getHorizontalValue());
					children.setVisible(visible);
				}
				super.draw();
			}
		};

		mainPane.setWidth(getWidth());
		mainPane.setHeight(getHeight());

		setRealWidth(realWidth);
		setRealHeight(realHeight);
		placeScrollbars();
	}

	/**
	 * The main pane on which you can add other widgets for this scrollable
	 * window.
	 * 
	 * @return the main pane
	 */
	public TPanel getMainPane() {
		return mainPane;
	}

	public void setRealWidth(int realWidth) {
		if (realWidth <= 0) {
			if (hScroller != null) {
				hScroller.remove();
			}
		} else {
			if (hScroller == null) {
				// size/position will be fixed by placeScrollbars()
				hScroller = new THScroller(this, 0, 0, 10);
			}
			setRightValue(realWidth);
		}

		reflowData();
	}

	public void setRealHeight(int realHeight) {
		if (realHeight <= 0) {
			if (vScroller != null) {
				vScroller.remove();
			}
		} else {
			if (vScroller == null) {
				// size/position will be fixed by placeScrollbars()
				vScroller = new TVScroller(this, 0, 0, 10);
			}
			setBottomValue(realHeight);
		}

		reflowData();
	}

	@Override
	public void onResize(TResizeEvent event) {
		super.onResize(event);
		mainPane.setWidth(getWidth());
		mainPane.setHeight(getHeight());
		mainPane.onResize(event);
	}

	@Override
	public void reflowData() {
		super.reflowData();
		reflowData(getHorizontalValue(), getVerticalValue());
	}

	protected void reflowData(int totalX, int totalY) {
		super.reflowData();
		mainPane.setX(-totalX);
		mainPane.setY(-totalY);
	}

	@Override
	public void onMouseUp(TMouseEvent mouse) {
		super.onMouseUp(mouse);

		// TODO: why? this should already be done by the scrollers
		// it could also mean we do it twice if, somehow, it sometime works...
		int mrx = mouse.getX();
		int mry = mouse.getY();

		int mx = mouse.getAbsoluteX();
		int my = mouse.getAbsoluteY();

		if (vScroller != null) {
			mouse.setX(mx - vScroller.getAbsoluteX());
			mouse.setY(my - vScroller.getAbsoluteY());
			vScroller.onMouseUp(mouse);
		}
		if (hScroller != null) {
			mouse.setX(mx - hScroller.getAbsoluteX());
			mouse.setY(my - hScroller.getAbsoluteY());
			hScroller.onMouseUp(mouse);
		}

		mouse.setX(mrx);
		mouse.setY(mry);
		//

		if (prevHorizontal != getHorizontalValue()
				|| prevVertical != getVerticalValue()) {
			prevHorizontal = getHorizontalValue();
			prevVertical = getVerticalValue();
			reflowData();
		}
	}
}
