package be.nikiroo.jexer;

import java.util.List;

import jexer.TScrollableWidget;
import jexer.TWidget;
import jexer.event.TResizeEvent;
import jexer.event.TResizeEvent.Type;

public class TSizeConstraint {
	private TWidget widget;
	private Integer x1;
	private Integer y1;
	private Integer x2;
	private Integer y2;

	// TODO: include in the window classes I use?

	public TSizeConstraint(TWidget widget, Integer x1, Integer y1, Integer x2,
			Integer y2) {
		this.widget = widget;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public TWidget getWidget() {
		return widget;
	}

	public Integer getX1() {
		if (x1 != null && x1 < 0)
			return widget.getParent().getWidth() + x1;
		return x1;
	}

	public Integer getY1() {
		if (y1 != null && y1 < 0)
			return widget.getParent().getHeight() + y1;
		return y1;
	}

	public Integer getX2() {
		if (x2 != null && x2 <= 0)
			return widget.getParent().getWidth() - 2 + x2;
		return x2;
	}

	public Integer getY2() {
		if (y2 != null && y2 <= 0)
			return widget.getParent().getHeight() - 2 + y2;
		return y2;
	}

	// coordinates < 0 = from the other side
	// 		x2 or y2 = 0 = max size
	// 		coordinate NULL = do not work on that side at all
	static public void setSize(List<TSizeConstraint> sizeConstraints, TWidget child,
			Integer x1, Integer y1, Integer x2, Integer y2) {
		sizeConstraints.add(new TSizeConstraint(child, x1, y1, x2, y2));
	}

	static public void resize(List<TSizeConstraint> sizeConstraints) {
		for (TSizeConstraint sizeConstraint : sizeConstraints) {
			TWidget widget = sizeConstraint.getWidget();
			Integer x1 = sizeConstraint.getX1();
			Integer y1 = sizeConstraint.getY1();
			Integer x2 = sizeConstraint.getX2();
			Integer y2 = sizeConstraint.getY2();

			if (x1 != null)
				widget.setX(x1);
			if (y1 != null)
				widget.setY(y1);

			if (x2 != null)
				widget.setWidth(x2 - widget.getX());
			if (y2 != null)
				widget.setHeight(y2 - widget.getY());

			// Resize the text field
			// TODO: why setW/setH/reflow not enough for the scrollbars?
			widget.onResize(new TResizeEvent(Type.WIDGET, widget.getWidth(),
					widget.getHeight()));

			if (widget instanceof TScrollableWidget) {
				((TScrollableWidget) widget).reflowData();
			}
		}
	}
}
