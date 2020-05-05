/*
 * Jexer - Java Text User Interface
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2019 Kevin Lamonte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author Kevin Lamonte [kevin.lamonte@gmail.com]
 * @version 1
 */
package jexer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import jexer.backend.Screen;
import jexer.bits.Cell;
import jexer.bits.CellAttributes;
import jexer.bits.Clipboard;
import jexer.bits.ColorTheme;
import jexer.event.TCommandEvent;
import jexer.event.TInputEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import jexer.layout.LayoutManager;
import jexer.menu.TMenu;
import jexer.ttree.TTreeItem;
import jexer.ttree.TTreeView;
import jexer.ttree.TTreeViewWidget;
import static jexer.TKeypress.*;

/**
 * TWidget is the base class of all objects that can be drawn on screen or
 * handle user input events.
 */
public abstract class TWidget implements Comparable<TWidget> {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Every widget has a parent widget that it may be "contained" in.  For
     * example, a TWindow might contain several TFields, or a TComboBox may
     * contain a TList that itself contains a TVScroller.
     */
    private TWidget parent = null;

    /**
     * Child widgets that this widget contains.
     */
    private List<TWidget> children;

    /**
     * The currently active child widget that will receive keypress events.
     */
    private TWidget activeChild = null;

    /**
     * If true, this widget will receive events.
     */
    private boolean active = false;

    /**
     * The window that this widget draws to.
     */
    private TWindow window = null;

    /**
     * Absolute X position of the top-left corner.
     */
    private int x = 0;

    /**
     * Absolute Y position of the top-left corner.
     */
    private int y = 0;

    /**
     * Width.
     */
    private int width = 0;

    /**
     * Height.
     */
    private int height = 0;

    /**
     * My tab order inside a window or containing widget.
     */
    private int tabOrder = 0;

    /**
     * If true, this widget can be tabbed to or receive events.
     */
    private boolean enabled = true;

    /**
     * If true, this widget will be rendered.
     */
    private boolean visible = true;

    /**
     * If true, this widget has a cursor.
     */
    private boolean cursorVisible = false;

    /**
     * Cursor column position in relative coordinates.
     */
    private int cursorX = 0;

    /**
     * Cursor row position in relative coordinates.
     */
    private int cursorY = 0;

    /**
     * Layout manager.
     */
    private LayoutManager layout = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Default constructor for subclasses.
     */
    protected TWidget() {
        children = new ArrayList<TWidget>();
    }

    /**
     * Protected constructor.
     *
     * @param parent parent widget
     */
    protected TWidget(final TWidget parent) {
        this(parent, true);
    }

    /**
     * Protected constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of widget
     * @param height height of widget
     */
    protected TWidget(final TWidget parent, final int x, final int y,
        final int width, final int height) {

        this(parent, true, x, y, width, height);
    }

    /**
     * Protected constructor used by subclasses that are disabled by default.
     *
     * @param parent parent widget
     * @param enabled if true assume enabled
     */
    protected TWidget(final TWidget parent, final boolean enabled) {
        this(parent, enabled, 0, 0, 0, 0);
    }

    /**
     * Protected constructor used by subclasses that are disabled by default.
     *
     * @param parent parent widget
     * @param enabled if true assume enabled
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of widget
     * @param height height of widget
     */
    protected TWidget(final TWidget parent, final boolean enabled,
        final int x, final int y, final int width, final int height) {

        if (width < 0) {
            throw new IllegalArgumentException("width cannot be negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height cannot be negative");
        }

        this.enabled = enabled;
        this.parent = parent;
        children = new ArrayList<TWidget>();

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (parent != null) {
            this.window = parent.window;
            parent.addChild(this);
        }
    }

    /**
     * Backdoor access for TWindow's constructor.  ONLY TWindow USES THIS.
     *
     * @param window the top-level window
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of window
     * @param height height of window
     */
    protected final void setupForTWindow(final TWindow window,
        final int x, final int y, final int width, final int height) {

        if (width < 0) {
            throw new IllegalArgumentException("width cannot be negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height cannot be negative");
        }

        this.parent = window;
        this.window = window;
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Subclasses should override this method to cleanup resources.  This is
     * called by TWindow.onClose().
     */
    protected void close() {
        // Default: call close() on children.
        for (TWidget w: getChildren()) {
            w.close();
        }
    }

    /**
     * Check if a mouse press/release event coordinate is contained in this
     * widget.
     *
     * @param mouse a mouse-based event
     * @return whether or not a mouse click would be sent to this widget
     */
    public final boolean mouseWouldHit(final TMouseEvent mouse) {

        if (!enabled) {
            return false;
        }

        if ((this instanceof TTreeItem)
            && ((y < 0) || (y > parent.getHeight() - 1))
        ) {
            return false;
        }

        if ((mouse.getAbsoluteX() >= getAbsoluteX())
            && (mouse.getAbsoluteX() <  getAbsoluteX() + width)
            && (mouse.getAbsoluteY() >= getAbsoluteY())
            && (mouse.getAbsoluteY() <  getAbsoluteY() + height)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Method that subclasses can override to handle keystrokes.
     *
     * @param keypress keystroke event
     */
    public void onKeypress(final TKeypressEvent keypress) {
        assert (parent != null);

        if ((children.size() == 0)
            || (this instanceof TTreeView)
            || (this instanceof TText)
            || (this instanceof TComboBox)
        ) {

            // Defaults:
            //   tab / shift-tab - switch to next/previous widget
            //   left-arrow or up-arrow: same as shift-tab
            if ((keypress.equals(kbTab))
                || (keypress.equals(kbDown) && !(this instanceof TComboBox))
            ) {
                parent.switchWidget(true);
                return;
            } else if ((keypress.equals(kbShiftTab))
                || (keypress.equals(kbBackTab))
                || (keypress.equals(kbUp) && !(this instanceof TComboBox))
            ) {
                parent.switchWidget(false);
                return;
            }
        }

        if ((children.size() == 0)
            && !(this instanceof TTreeView)
        ) {

            // Defaults:
            //   right-arrow or down-arrow: same as tab
            if (keypress.equals(kbRight)) {
                parent.switchWidget(true);
                return;
            } else if (keypress.equals(kbLeft)) {
                parent.switchWidget(false);
                return;
            }
        }

        // If I have any buttons on me AND this is an Alt-key that matches
        // its mnemonic, send it an Enter keystroke.
        for (TWidget widget: children) {
            if (widget instanceof TButton) {
                TButton button = (TButton) widget;
                if (button.isEnabled()
                    && !keypress.getKey().isFnKey()
                    && keypress.getKey().isAlt()
                    && !keypress.getKey().isCtrl()
                    && (Character.toLowerCase(button.getMnemonic().getShortcut())
                        == Character.toLowerCase(keypress.getKey().getChar()))
                ) {

                    widget.onKeypress(new TKeypressEvent(kbEnter));
                    return;
                }
            }
        }

        // If I have any labels on me AND this is an Alt-key that matches
        // its mnemonic, call its action.
        for (TWidget widget: children) {
            if (widget instanceof TLabel) {
                TLabel label = (TLabel) widget;
                if (!keypress.getKey().isFnKey()
                    && keypress.getKey().isAlt()
                    && !keypress.getKey().isCtrl()
                    && (Character.toLowerCase(label.getMnemonic().getShortcut())
                        == Character.toLowerCase(keypress.getKey().getChar()))
                ) {

                    label.dispatch();
                    return;
                }
            }
        }

        // If I have any radiobuttons on me AND this is an Alt-key that
        // matches its mnemonic, select it and send a Space to it.
        for (TWidget widget: children) {
            if (widget instanceof TRadioButton) {
                TRadioButton button = (TRadioButton) widget;
                if (button.isEnabled()
                    && !keypress.getKey().isFnKey()
                    && keypress.getKey().isAlt()
                    && !keypress.getKey().isCtrl()
                    && (Character.toLowerCase(button.getMnemonic().getShortcut())
                        == Character.toLowerCase(keypress.getKey().getChar()))
                ) {
                    activate(widget);
                    widget.onKeypress(new TKeypressEvent(kbSpace));
                    return;
                }
            }
            if (widget instanceof TRadioGroup) {
                for (TWidget child: widget.getChildren()) {
                    if (child instanceof TRadioButton) {
                        TRadioButton button = (TRadioButton) child;
                        if (button.isEnabled()
                            && !keypress.getKey().isFnKey()
                            && keypress.getKey().isAlt()
                            && !keypress.getKey().isCtrl()
                            && (Character.toLowerCase(button.getMnemonic().getShortcut())
                                == Character.toLowerCase(keypress.getKey().getChar()))
                        ) {
                            activate(widget);
                            widget.activate(child);
                            child.onKeypress(new TKeypressEvent(kbSpace));
                            return;
                        }
                    }
                }
            }
        }

        // If I have any checkboxes on me AND this is an Alt-key that matches
        // its mnemonic, select it and set it to checked.
        for (TWidget widget: children) {
            if (widget instanceof TCheckBox) {
                TCheckBox checkBox = (TCheckBox) widget;
                if (checkBox.isEnabled()
                    && !keypress.getKey().isFnKey()
                    && keypress.getKey().isAlt()
                    && !keypress.getKey().isCtrl()
                    && (Character.toLowerCase(checkBox.getMnemonic().getShortcut())
                        == Character.toLowerCase(keypress.getKey().getChar()))
                ) {
                    activate(checkBox);
                    checkBox.setChecked(true);
                    return;
                }
            }
        }

        // Dispatch the keypress to an active widget
        for (TWidget widget: children) {
            if (widget.active) {
                widget.onKeypress(keypress);
                return;
            }
        }
    }

    /**
     * Method that subclasses can override to handle mouse button presses.
     *
     * @param mouse mouse button event
     */
    public void onMouseDown(final TMouseEvent mouse) {
        // Default: do nothing, pass to children instead
        if (activeChild != null) {
            if (activeChild.mouseWouldHit(mouse)) {
                // Dispatch to the active child

                // Set x and y relative to the child's coordinates
                mouse.setX(mouse.getAbsoluteX() - activeChild.getAbsoluteX());
                mouse.setY(mouse.getAbsoluteY() - activeChild.getAbsoluteY());
                activeChild.onMouseDown(mouse);
                return;
            }
        }
        for (int i = children.size() - 1 ; i >= 0 ; i--) {
            TWidget widget = children.get(i);
            if (widget.mouseWouldHit(mouse)) {
                // Dispatch to this child, also activate it
                activate(widget);

                // Set x and y relative to the child's coordinates
                mouse.setX(mouse.getAbsoluteX() - widget.getAbsoluteX());
                mouse.setY(mouse.getAbsoluteY() - widget.getAbsoluteY());
                widget.onMouseDown(mouse);
                return;
            }
        }
    }

    /**
     * Method that subclasses can override to handle mouse button releases.
     *
     * @param mouse mouse button event
     */
    public void onMouseUp(final TMouseEvent mouse) {
        // Default: do nothing, pass to children instead
        if (activeChild != null) {
            if (activeChild.mouseWouldHit(mouse)) {
                // Dispatch to the active child

                // Set x and y relative to the child's coordinates
                mouse.setX(mouse.getAbsoluteX() - activeChild.getAbsoluteX());
                mouse.setY(mouse.getAbsoluteY() - activeChild.getAbsoluteY());
                activeChild.onMouseUp(mouse);
                return;
            }
        }
        for (int i = children.size() - 1 ; i >= 0 ; i--) {
            TWidget widget = children.get(i);
            if (widget.mouseWouldHit(mouse)) {
                // Dispatch to this child, also activate it
                activate(widget);

                // Set x and y relative to the child's coordinates
                mouse.setX(mouse.getAbsoluteX() - widget.getAbsoluteX());
                mouse.setY(mouse.getAbsoluteY() - widget.getAbsoluteY());
                widget.onMouseUp(mouse);
                return;
            }
        }
    }

    /**
     * Method that subclasses can override to handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    public void onMouseMotion(final TMouseEvent mouse) {
        // Default: do nothing, pass it on to ALL of my children.  This way
        // the children can see the mouse "leaving" their area.
        for (TWidget widget: children) {
            // Set x and y relative to the child's coordinates
            mouse.setX(mouse.getAbsoluteX() - widget.getAbsoluteX());
            mouse.setY(mouse.getAbsoluteY() - widget.getAbsoluteY());
            widget.onMouseMotion(mouse);
        }
    }

    /**
     * Method that subclasses can override to handle mouse button
     * double-clicks.
     *
     * @param mouse mouse button event
     */
    public void onMouseDoubleClick(final TMouseEvent mouse) {
        // Default: do nothing, pass to children instead
        if (activeChild != null) {
            if (activeChild.mouseWouldHit(mouse)) {
                // Dispatch to the active child

                // Set x and y relative to the child's coordinates
                mouse.setX(mouse.getAbsoluteX() - activeChild.getAbsoluteX());
                mouse.setY(mouse.getAbsoluteY() - activeChild.getAbsoluteY());
                activeChild.onMouseDoubleClick(mouse);
                return;
            }
        }
        for (int i = children.size() - 1 ; i >= 0 ; i--) {
            TWidget widget = children.get(i);
            if (widget.mouseWouldHit(mouse)) {
                // Dispatch to this child, also activate it
                activate(widget);

                // Set x and y relative to the child's coordinates
                mouse.setX(mouse.getAbsoluteX() - widget.getAbsoluteX());
                mouse.setY(mouse.getAbsoluteY() - widget.getAbsoluteY());
                widget.onMouseDoubleClick(mouse);
                return;
            }
        }
    }

    /**
     * Method that subclasses can override to handle window/screen resize
     * events.
     *
     * @param resize resize event
     */
    public void onResize(final TResizeEvent resize) {
        // Default: change my width/height.
        if (resize.getType() == TResizeEvent.Type.WIDGET) {
            width = resize.getWidth();
            height = resize.getHeight();
            if (layout != null) {
                if (this instanceof TWindow) {
                    layout.onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                            width - 2, height - 2));
                } else {
                    layout.onResize(resize);
                }
            }
        } else {
            // Let children see the screen resize
            for (TWidget widget: children) {
                widget.onResize(resize);
            }
        }
    }

    /**
     * Method that subclasses can override to handle posted command events.
     *
     * @param command command event
     */
    public void onCommand(final TCommandEvent command) {
        if (activeChild != null) {
            activeChild.onCommand(command);
        }
    }

    /**
     * Method that subclasses can override to handle menu or posted menu
     * events.
     *
     * @param menu menu event
     */
    public void onMenu(final TMenuEvent menu) {
        // Default: do nothing, pass to children instead
        for (TWidget widget: children) {
            widget.onMenu(menu);
        }
    }

    /**
     * Method that subclasses can override to do processing when the UI is
     * idle.  Note that repainting is NOT assumed.  To get a refresh after
     * onIdle, call doRepaint().
     */
    public void onIdle() {
        // Default: do nothing, pass to children instead
        for (TWidget widget: children) {
            widget.onIdle();
        }
    }

    /**
     * Consume event.  Subclasses that want to intercept all events in one go
     * can override this method.
     *
     * @param event keyboard, mouse, resize, command, or menu event
     */
    public void handleEvent(final TInputEvent event) {
        /*
        System.err.printf("TWidget (%s) event: %s\n", this.getClass().getName(),
            event);
        */

        if (!enabled) {
            // Discard event
            // System.err.println("   -- discard --");
            return;
        }

        if (event instanceof TKeypressEvent) {
            onKeypress((TKeypressEvent) event);
        } else if (event instanceof TMouseEvent) {

            TMouseEvent mouse = (TMouseEvent) event;

            switch (mouse.getType()) {

            case MOUSE_DOWN:
                onMouseDown(mouse);
                break;

            case MOUSE_UP:
                onMouseUp(mouse);
                break;

            case MOUSE_MOTION:
                onMouseMotion(mouse);
                break;

            case MOUSE_DOUBLE_CLICK:
                onMouseDoubleClick(mouse);
                break;

            default:
                throw new IllegalArgumentException("Invalid mouse event type: "
                    + mouse.getType());
            }
        } else if (event instanceof TResizeEvent) {
            onResize((TResizeEvent) event);
        } else if (event instanceof TCommandEvent) {
            onCommand((TCommandEvent) event);
        } else if (event instanceof TMenuEvent) {
            onMenu((TMenuEvent) event);
        }

        // Do nothing else
        return;
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get parent widget.
     *
     * @return parent widget
     */
    public final TWidget getParent() {
        return parent;
    }

    /**
     * Get the list of child widgets that this widget contains.
     *
     * @return the list of child widgets
     */
    public List<TWidget> getChildren() {
        return children;
    }

    /**
     * Remove this widget from its parent container.  close() will be called
     * before it is removed.
     */
    public final void remove() {
        remove(true);
    }

    /**
     * Remove this widget from its parent container.
     *
     * @param doClose if true, call the close() method before removing the
     * child
     */
    public final void remove(final boolean doClose) {
        if (parent != null) {
            parent.remove(this, doClose);
        }
    }

    /**
     * Remove a child widget from this container.
     *
     * @param child the child widget to remove
     */
    public final void remove(final TWidget child) {
        remove(child, true);
    }

    /**
     * Remove a child widget from this container.
     *
     * @param child the child widget to remove
     * @param doClose if true, call the close() method before removing the
     * child
     */
    public final void remove(final TWidget child, final boolean doClose) {
        if (!children.contains(child)) {
            throw new IndexOutOfBoundsException("child widget is not in " +
                "list of children of this parent");
        }
        if (doClose) {
            child.close();
        }
        children.remove(child);
        child.parent = null;
        child.window = null;
        if (layout != null) {
            layout.remove(this);
        }
    }

    /**
     * Set this widget's parent to a different widget.
     *
     * @param newParent new parent widget
     * @param doClose if true, call the close() method before removing the
     * child from its existing parent widget
     */
    public final void setParent(final TWidget newParent,
        final boolean doClose) {

        if (parent != null) {
            parent.remove(this, doClose);
            window = null;
        }
        assert (parent == null);
        assert (window == null);
        parent = newParent;
        setWindow(parent.window);
        parent.addChild(this);
    }

    /**
     * Set this widget's window to a specific window.
     *
     * Having a null parent with a specified window is only used within Jexer
     * by TStatusBar because TApplication routes events directly to it and
     * calls its draw() method.  Any other non-parented widgets will require
     * similar special case functionality to receive events or be drawn to
     * screen.
     *
     * @param window the window to use
     */
    public final void setWindow(final TWindow window) {
        this.window = window;
        for (TWidget child: getChildren()) {
            child.setWindow(window);
        }
    }

    /**
     * Remove a child widget from this container, and all of its children
     * recursively from their parent containers.
     *
     * @param child the child widget to remove
     * @param doClose if true, call the close() method before removing each
     * child
     */
    public final void removeAll(final TWidget child, final boolean doClose) {
        remove(child, doClose);
        for (TWidget w: child.children) {
            child.removeAll(w, doClose);
        }
    }

    /**
     * Get active flag.
     *
     * @return if true, this widget will receive events
     */
    public final boolean isActive() {
        return active;
    }

    /**
     * Set active flag.
     *
     * @param active if true, this widget will receive events
     */
    public final void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Get the window this widget is on.
     *
     * @return the window
     */
    public final TWindow getWindow() {
        return window;
    }

    /**
     * Get X position.
     *
     * @return absolute X position of the top-left corner
     */
    public final int getX() {
        return x;
    }

    /**
     * Set X position.
     *
     * @param x absolute X position of the top-left corner
     */
    public final void setX(final int x) {
        this.x = x;
    }

    /**
     * Get Y position.
     *
     * @return absolute Y position of the top-left corner
     */
    public final int getY() {
        return y;
    }

    /**
     * Set Y position.
     *
     * @param y absolute Y position of the top-left corner
     */
    public final void setY(final int y) {
        this.y = y;
    }

    /**
     * Get the width.
     *
     * @return widget width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Change the width.
     *
     * @param width new widget width
     */
    public void setWidth(final int width) {
        this.width = width;
        if (layout != null) {
            layout.onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                    width, height));
        }
    }

    /**
     * Get the height.
     *
     * @return widget height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Change the height.
     *
     * @param height new widget height
     */
    public void setHeight(final int height) {
        this.height = height;
        if (layout != null) {
            layout.onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                    width, height));
        }
    }

    /**
     * Change the dimensions.
     *
     * @param x absolute X position of the top-left corner
     * @param y absolute Y position of the top-left corner
     * @param width new widget width
     * @param height new widget height
     */
    public final void setDimensions(final int x, final int y, final int width,
        final int height) {

        this.x = x;
        this.y = y;
        // Call the functions so that subclasses can choose how to handle it.
        setWidth(width);
        setHeight(height);
        if (layout != null) {
            layout.onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                    width, height));
        }
    }

    /**
     * Get the layout manager.
     *
     * @return the layout manager, or null if not set
     */
    public LayoutManager getLayoutManager() {
        return layout;
    }

    /**
     * Set the layout manager.
     *
     * @param layout the new layout manager
     */
    public void setLayoutManager(LayoutManager layout) {
        if (this.layout != null) {
            for (TWidget w: children) {
                this.layout.remove(w);
            }
            this.layout = null;
        }
        this.layout = layout;
        if (this.layout != null) {
            for (TWidget w: children) {
                this.layout.add(w);
            }
        }
    }

    /**
     * Get enabled flag.
     *
     * @return if true, this widget can be tabbed to or receive events
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Set enabled flag.
     *
     * @param enabled if true, this widget can be tabbed to or receive events
     */
    public final void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            active = false;
            // See if there are any active siblings to switch to
            boolean foundSibling = false;
            if (parent != null) {
                for (TWidget w: parent.children) {
                    if ((w.enabled)
                        && !(this instanceof THScroller)
                        && !(this instanceof TVScroller)
                    ) {
                        parent.activate(w);
                        foundSibling = true;
                        break;
                    }
                }
                if (!foundSibling) {
                    parent.activeChild = null;
                }
            }
        }
    }

    /**
     * Set visible flag.
     *
     * @param visible if true, this widget will be drawn
     */
    public final void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * See if this widget is visible.
     *
     * @return if true, this widget will be drawn
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * Set visible cursor flag.
     *
     * @param cursorVisible if true, this widget has a cursor
     */
    public final void setCursorVisible(final boolean cursorVisible) {
        this.cursorVisible = cursorVisible;
    }

    /**
     * See if this widget has a visible cursor.
     *
     * @return if true, this widget has a visible cursor
     */
    public final boolean isCursorVisible() {
        // If cursor is out of my bounds, it is not visible.
        if ((cursorX >= width)
            || (cursorX < 0)
            || (cursorY >= height)
            || (cursorY < 0)
        ) {
            return false;
        }

        assert (window != null);

        if (window instanceof TDesktop) {
            // Desktop doesn't have a window border.
            return cursorVisible;
        }

        // If cursor is out of my window's bounds, it is not visible.
        if ((getCursorAbsoluteX() >= window.getAbsoluteX()
                + window.getWidth() - 1)
            || (getCursorAbsoluteX() < 0)
            || (getCursorAbsoluteY() >= window.getAbsoluteY()
                + window.getHeight() - 1)
            || (getCursorAbsoluteY() < 0)
        ) {
            return false;
        }
        return cursorVisible;
    }

    /**
     * Get cursor X value.
     *
     * @return cursor column position in relative coordinates
     */
    public final int getCursorX() {
        return cursorX;
    }

    /**
     * Set cursor X value.
     *
     * @param cursorX column position in relative coordinates
     */
    public final void setCursorX(final int cursorX) {
        this.cursorX = cursorX;
    }

    /**
     * Get cursor Y value.
     *
     * @return cursor row position in relative coordinates
     */
    public final int getCursorY() {
        return cursorY;
    }

    /**
     * Set cursor Y value.
     *
     * @param cursorY row position in relative coordinates
     */
    public final void setCursorY(final int cursorY) {
        this.cursorY = cursorY;
    }

    /**
     * Get this TWidget's parent TApplication.
     *
     * @return the parent TApplication, or null if not assigned
     */
    public TApplication getApplication() {
        if (window != null) {
            return window.getApplication();
        }
        return null;
    }

    /**
     * Get the Screen.
     *
     * @return the Screen, or null if not assigned
     */
    public Screen getScreen() {
        if (window != null) {
            return window.getScreen();
        }
        return null;
    }

    /**
     * Get the Clipboard.
     *
     * @return the Clipboard, or null if not assigned
     */
    public Clipboard getClipboard() {
        if (window != null) {
            return window.getApplication().getClipboard();
        }
        return null;
    }

    /**
     * Comparison operator.  For various subclasses it sorts on:
     * <ul>
     * <li>tabOrder for TWidgets</li>
     * <li>z for TWindows</li>
     * <li>text for TTreeItems</li>
     * </ul>
     *
     * @param that another TWidget, TWindow, or TTreeItem instance
     * @return difference between this.tabOrder and that.tabOrder, or
     * difference between this.z and that.z, or String.compareTo(text)
     */
    @Override
    public int compareTo(final TWidget that) {
        if ((this instanceof TWindow)
            && (that instanceof TWindow)
        ) {
            return (((TWindow) this).getZ() - ((TWindow) that).getZ());
        }
        if ((this instanceof TTreeItem)
            && (that instanceof TTreeItem)
        ) {
            return (((TTreeItem) this).getText().compareTo(
                ((TTreeItem) that).getText()));
        }
        return (this.tabOrder - that.tabOrder);
    }

    /**
     * See if this widget should render with the active color.
     *
     * @return true if this widget is active and all of its parents are
     * active.
     */
    public final boolean isAbsoluteActive() {
        if (parent == this) {
            return active;
        }
        return (active && (parent == null ? true : parent.isAbsoluteActive()));
    }

    /**
     * Returns the cursor X position.
     *
     * @return absolute screen column number for the cursor's X position
     */
    public final int getCursorAbsoluteX() {
        return getAbsoluteX() + cursorX;
    }

    /**
     * Returns the cursor Y position.
     *
     * @return absolute screen row number for the cursor's Y position
     */
    public final int getCursorAbsoluteY() {
        return getAbsoluteY() + cursorY;
    }

    /**
     * Compute my absolute X position as the sum of my X plus all my parent's
     * X's.
     *
     * @return absolute screen column number for my X position
     */
    public final int getAbsoluteX() {
        assert (parent != null);
        if (parent == this) {
            return x;
        }
        if ((parent instanceof TWindow)
            && !(parent instanceof TMenu)
            && !(parent instanceof TDesktop)
        ) {
            // Widgets on a TWindow have (0,0) as their top-left, but this is
            // actually the TWindow's (1,1).
            return parent.getAbsoluteX() + x + 1;
        }
        return parent.getAbsoluteX() + x;
    }

    /**
     * Compute my absolute Y position as the sum of my Y plus all my parent's
     * Y's.
     *
     * @return absolute screen row number for my Y position
     */
    public final int getAbsoluteY() {
        assert (parent != null);
        if (parent == this) {
            return y;
        }
        if ((parent instanceof TWindow)
            && !(parent instanceof TMenu)
            && !(parent instanceof TDesktop)
        ) {
            // Widgets on a TWindow have (0,0) as their top-left, but this is
            // actually the TWindow's (1,1).
            return parent.getAbsoluteY() + y + 1;
        }
        return parent.getAbsoluteY() + y;
    }

    /**
     * Get the global color theme.
     *
     * @return the ColorTheme
     */
    protected final ColorTheme getTheme() {
        return window.getApplication().getTheme();
    }

    /**
     * See if this widget can be drawn onto a screen.
     *
     * @return true if this widget is part of the hierarchy that can draw to
     * a screen
     */
    public final boolean isDrawable() {
        if ((window == null)
            || (window.getScreen() == null)
            || (parent == null)
        ) {
            return false;
        }
        if (parent == this) {
            return true;
        }
        return (parent.isDrawable());
    }

    /**
     * Draw my specific widget.  When called, the screen rectangle I draw
     * into is already setup (offset and clipping).
     */
    public void draw() {
        // Default widget draws nothing.
    }

    /**
     * Called by parent to render to TWindow.  Note package private access.
     */
    final void drawChildren() {
        if (!isDrawable()) {
            return;
        }

        // Set my clipping rectangle
        assert (window != null);
        assert (getScreen() != null);
        Screen screen = getScreen();

        // Special case: TStatusBar is drawn by TApplication, not anything
        // else.
        if (this instanceof TStatusBar) {
            return;
        }

        screen.setClipRight(width);
        screen.setClipBottom(height);

        int absoluteRightEdge = window.getAbsoluteX() + window.getWidth();
        int absoluteBottomEdge = window.getAbsoluteY() + window.getHeight();
        if (!(this instanceof TWindow)
            && !(this instanceof TVScroller)
            && !(window instanceof TDesktop)
        ) {
            absoluteRightEdge -= 1;
        }
        if (!(this instanceof TWindow)
            && !(this instanceof THScroller)
            && !(window instanceof TDesktop)
        ) {
            absoluteBottomEdge -= 1;
        }
        int myRightEdge = getAbsoluteX() + width;
        int myBottomEdge = getAbsoluteY() + height;
        if (getAbsoluteX() > absoluteRightEdge) {
            // I am offscreen
            screen.setClipRight(0);
        } else if (myRightEdge > absoluteRightEdge) {
            screen.setClipRight(screen.getClipRight()
                - (myRightEdge - absoluteRightEdge));
        }
        if (getAbsoluteY() > absoluteBottomEdge) {
            // I am offscreen
            screen.setClipBottom(0);
        } else if (myBottomEdge > absoluteBottomEdge) {
            screen.setClipBottom(screen.getClipBottom()
                - (myBottomEdge - absoluteBottomEdge));
        }

        // Set my offset
        screen.setOffsetX(getAbsoluteX());
        screen.setOffsetY(getAbsoluteY());

        // Draw me
        draw();
        if (!isDrawable()) {
            // An action taken by a draw method unhooked me from the UI.
            // Bail out.
            return;
        }

        assert (visible == true);

        // Continue down the chain.  Draw the active child last so that it
        // is on top.
        for (TWidget widget: children) {
            if (widget.isVisible() && (widget != activeChild)) {
                widget.drawChildren();
                if (!isDrawable()) {
                    // An action taken by a draw method unhooked me from the UI.
                    // Bail out.
                    return;
                }
            }
        }
        if (activeChild != null) {
            activeChild.drawChildren();
        }
    }

    /**
     * Repaint the screen on the next update.
     */
    protected final void doRepaint() {
        window.getApplication().doRepaint();
    }

    /**
     * Add a child widget to my list of children.  We set its tabOrder to 0
     * and increment the tabOrder of all other children.
     *
     * @param child TWidget to add
     */
    public void addChild(final TWidget child) {
        children.add(child);

        if ((child.enabled)
            && !(child instanceof THScroller)
            && !(child instanceof TVScroller)
        ) {
            for (TWidget widget: children) {
                widget.active = false;
            }
            child.active = true;
            activeChild = child;
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).tabOrder = i;
        }
        if (layout != null) {
            layout.add(child);
        }
    }

    /**
     * Reset the tab order of children to match their position in the list.
     * Available so that subclasses can re-order their widgets if needed.
     */
    protected void resetTabOrder() {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).tabOrder = i;
        }
    }
    
    /**
     * Remove and {@link TWidget#close()} the given child from this {@link TWidget}.
     * <p>
     * Will also reorder the tab values of the remaining children.
     * 
     * @param child the child to remove
     * 
     * @return TRUE if the child was removed, FALSE if it was not found
     */
    public boolean removeChild(final TWidget child) {
        if (children.remove(child)) {
                child.close();
                child.parent = null;
                child.window = null;
                
                resetTabOrder();
                
                return true;
        }
        
        return false;
    }

    /**
     * Switch the active child.
     *
     * @param child TWidget to activate
     */
    public final void activate(final TWidget child) {
        assert (child.enabled);
        if ((child instanceof THScroller)
            || (child instanceof TVScroller)
        ) {
            return;
        }

        if (children.size() == 1) {
            if (children.get(0).enabled == true) {
                child.active = true;
                activeChild = child;
            }
        } else {
            if (child != activeChild) {
                if (activeChild != null) {
                    activeChild.active = false;
                }
            }
            child.active = true;
            activeChild = child;
        }
    }

    /**
     * Switch the active child.
     *
     * @param tabOrder tabOrder of the child to activate.  If that child
     * isn't enabled, then the next enabled child will be activated.
     */
    public final void activate(final int tabOrder) {
        if (children.size() == 1) {
            if (children.get(0).enabled == true) {
                children.get(0).active = true;
                activeChild = children.get(0);
            }
            return;
        }

        TWidget child = null;
        for (TWidget widget: children) {
            if ((widget.enabled)
                && !(widget instanceof THScroller)
                && !(widget instanceof TVScroller)
                && (widget.tabOrder >= tabOrder)
            ) {
                child = widget;
                break;
            }
        }
        if ((child != null) && (child != activeChild)) {
            if (activeChild != null) {
                activeChild.active = false;
            }
            assert (child.enabled);
            child.active = true;
            activeChild = child;
        }
    }

    /**
     * Make this widget the active child of its parent.  Note that this is
     * not final since TWindow overrides activate().
     */
    public void activate() {
        if (enabled) {
            if (parent != null) {
                parent.activate(this);
            }
        }
    }

    /**
     * Make this widget, all of its parents, the active child.
     */
    public final void activateAll() {
        activate();
        if (parent == this) {
            return;
        }
        if (parent != null) {
            parent.activateAll();
        }
    }

    /**
     * Switch the active widget with the next in the tab order.
     *
     * @param forward if true, then switch to the next enabled widget in the
     * list, otherwise switch to the previous enabled widget in the list
     */
    public final void switchWidget(final boolean forward) {

        // No children: do nothing.
        if (children.size() == 0) {
            return;
        }

        assert (parent != null);

        // If there is only one child, make it active if it is enabled.
        if (children.size() == 1) {
            if (children.get(0).enabled == true) {
                activeChild = children.get(0);
                activeChild.active = true;
            } else {
                children.get(0).active = false;
                activeChild = null;
            }
            return;
        }

        // Two or more children: go forward or backward to the next enabled
        // child.
        int tabOrder = 0;
        if (activeChild != null) {
            tabOrder = activeChild.tabOrder;
        }
        do {
            if (forward) {
                tabOrder++;
            } else {
                tabOrder--;
            }
            if (tabOrder < 0) {

                // If at the end, pass the switch to my parent.
                if ((!forward) && (parent != this)) {
                    parent.switchWidget(forward);
                    return;
                }

                tabOrder = children.size() - 1;
            } else if (tabOrder == children.size()) {
                // If at the end, pass the switch to my parent.
                if ((forward) && (parent != this)) {
                    parent.switchWidget(forward);
                    return;
                }

                tabOrder = 0;
            }
            if (activeChild == null) {
                if (tabOrder == 0) {
                    // We wrapped around
                    break;
                }
            } else if (activeChild.tabOrder == tabOrder) {
                // We wrapped around
                break;
            }
        } while ((!children.get(tabOrder).enabled)
            && !(children.get(tabOrder) instanceof THScroller)
            && !(children.get(tabOrder) instanceof TVScroller));

        if (activeChild != null) {
            assert (children.get(tabOrder).enabled);

            activeChild.active = false;
        }
        if (children.get(tabOrder).enabled == true) {
            children.get(tabOrder).active = true;
            activeChild = children.get(tabOrder);
        }
    }

    /**
     * Returns my active widget.
     *
     * @return widget that is active, or this if no children
     */
    public TWidget getActiveChild() {
        if ((this instanceof THScroller)
            || (this instanceof TVScroller)
        ) {
            return parent;
        }

        for (TWidget widget: children) {
            if (widget.active) {
                return widget.getActiveChild();
            }
        }
        // No active children, return me
        return this;
    }

    /**
     * Insert a vertical split between this widget and parent, and optionally
     * put another widget in the other side of the split.
     *
     * @param newWidgetOnLeft if true, the new widget (if specified) will be
     * on the left pane, and this widget will be placed on the right pane
     * @param newWidget the new widget to add to the other pane, or null
     * @return the new split pane widget
     */
    public TSplitPane splitVertical(final boolean newWidgetOnLeft,
        final TWidget newWidget) {

        TSplitPane splitPane = new TSplitPane(null, x, y, width, height, true);
        TWidget myParent = parent;
        remove(false);
        if (myParent instanceof TSplitPane) {
            // TSplitPane has a left/right/top/bottom link to me somewhere,
            // replace it with a link to splitPane.
            ((TSplitPane) myParent).replaceWidget(this, splitPane);
        }
        splitPane.setParent(myParent, false);
        if (newWidgetOnLeft) {
            splitPane.setLeft(newWidget);
            splitPane.setRight(this);
        } else {
            splitPane.setLeft(this);
            splitPane.setRight(newWidget);
        }
        if (newWidget != null) {
            newWidget.activateAll();
        } else {
            activateAll();
        }

        assert (parent != null);
        assert (window != null);
        assert (splitPane.getWindow() != null);
        assert (splitPane.getParent() != null);
        assert (splitPane.isActive() == true);
        assert (parent == splitPane);
        if (newWidget != null) {
            assert (newWidget.parent == parent);
            assert (newWidget.active == true);
            assert (active == false);
        } else {
            assert (active == true);
        }
        return splitPane;
    }

    /**
     * Insert a horizontal split between this widget and parent, and
     * optionally put another widget in the other side of the split.
     *
     * @param newWidgetOnTop if true, the new widget (if specified) will be
     * on the top pane, and this widget's children will be placed on the
     * bottom pane
     * @param newWidget the new widget to add to the other pane, or null
     * @return the new split pane widget
     */
    public TSplitPane splitHorizontal(final boolean newWidgetOnTop,
        final TWidget newWidget) {

        TSplitPane splitPane = new TSplitPane(null, x, y, width, height, false);
        TWidget myParent = parent;
        remove(false);
        if (myParent instanceof TSplitPane) {
            // TSplitPane has a left/right/top/bottom link to me somewhere,
            // replace it with a link to splitPane.
            ((TSplitPane) myParent).replaceWidget(this, splitPane);
        }
        splitPane.setParent(myParent, false);
        if (newWidgetOnTop) {
            splitPane.setTop(newWidget);
            splitPane.setBottom(this);
        } else {
            splitPane.setTop(this);
            splitPane.setBottom(newWidget);
        }
        if (newWidget != null) {
            newWidget.activateAll();
        } else {
            activateAll();
        }

        assert (parent != null);
        assert (window != null);
        assert (splitPane.getWindow() != null);
        assert (splitPane.getParent() != null);
        assert (splitPane.isActive() == true);
        assert (parent == splitPane);
        if (newWidget != null) {
            assert (newWidget.parent == parent);
            assert (newWidget.active == true);
            assert (active == false);
        } else {
            assert (active == true);
        }
        return splitPane;
    }

    /**
     * Generate a human-readable string for this widget.
     *
     * @return a human-readable string
     */
    @Override
    public String toString() {
        return String.format("%s(%8x) position (%d, %d) geometry %dx%d " +
            "active %s enabled %s visible %s", getClass().getName(),
            hashCode(), x, y, width, height, active, enabled, visible);
    }

    /**
     * Generate a string for this widget's hierarchy.
     *
     * @param prefix a prefix to use for this widget's place in the hierarchy
     * @return a pretty-printable string of this hierarchy
     */
    protected String toPrettyString(final String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(toString());
        String newPrefix = "";
        for (int i = 0; i < prefix.length(); i++) {
            newPrefix += " ";
        }
        for (int i = 0; i < children.size(); i++) {
            TWidget child= children.get(i);
            sb.append("\n");
            if (i == children.size() - 1) {
                sb.append(child.toPrettyString(newPrefix + " \u2514\u2500"));
            } else {
                sb.append(child.toPrettyString(newPrefix + " \u251c\u2500"));
            }
        }
        return sb.toString();
    }

    /**
     * Generate a string for this widget's hierarchy.
     *
     * @return a pretty-printable string of this hierarchy
     */
    public String toPrettyString() {
        return toPrettyString("");
    }

    // ------------------------------------------------------------------------
    // Passthru for Screen functions ------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @return attributes at (x, y)
     */
    protected final CellAttributes getAttrXY(final int x, final int y) {
        return getScreen().getAttrXY(x, y);
    }

    /**
     * Set the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    protected final void putAttrXY(final int x, final int y,
        final CellAttributes attr) {

        getScreen().putAttrXY(x, y, attr);
    }

    /**
     * Set the attributes at one location.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param attr attributes to use (bold, foreColor, backColor)
     * @param clip if true, honor clipping/offset
     */
    protected final void putAttrXY(final int x, final int y,
        final CellAttributes attr, final boolean clip) {

        getScreen().putAttrXY(x, y, attr, clip);
    }

    /**
     * Fill the entire screen with one character with attributes.
     *
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    protected final void putAll(final int ch, final CellAttributes attr) {
        getScreen().putAll(ch, attr);
    }

    /**
     * Render one character with attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character + attributes to draw
     */
    protected final void putCharXY(final int x, final int y, final Cell ch) {
        getScreen().putCharXY(x, y, ch);
    }

    /**
     * Render one character with attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    protected final void putCharXY(final int x, final int y, final int ch,
        final CellAttributes attr) {

        getScreen().putCharXY(x, y, ch, attr);
    }

    /**
     * Render one character without changing the underlying attributes.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param ch character to draw
     */
    protected final void putCharXY(final int x, final int y, final int ch) {
        getScreen().putCharXY(x, y, ch);
    }

    /**
     * Render a string.  Does not wrap if the string exceeds the line.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param str string to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    protected final void putStringXY(final int x, final int y, final String str,
        final CellAttributes attr) {

        getScreen().putStringXY(x, y, str, attr);
    }

    /**
     * Render a string without changing the underlying attribute.  Does not
     * wrap if the string exceeds the line.
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param str string to draw
     */
    protected final void putStringXY(final int x, final int y, final String str) {
        getScreen().putStringXY(x, y, str);
    }

    /**
     * Draw a vertical line from (x, y) to (x, y + n).
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param n number of characters to draw
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    protected final void vLineXY(final int x, final int y, final int n,
        final int ch, final CellAttributes attr) {

        getScreen().vLineXY(x, y, n, ch, attr);
    }

    /**
     * Draw a horizontal line from (x, y) to (x + n, y).
     *
     * @param x column coordinate.  0 is the left-most column.
     * @param y row coordinate.  0 is the top-most row.
     * @param n number of characters to draw
     * @param ch character to draw
     * @param attr attributes to use (bold, foreColor, backColor)
     */
    protected final void hLineXY(final int x, final int y, final int n,
        final int ch, final CellAttributes attr) {

        getScreen().hLineXY(x, y, n, ch, attr);
    }

    /**
     * Draw a box with a border and empty background.
     *
     * @param left left column of box.  0 is the left-most row.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     * @param border attributes to use for the border
     * @param background attributes to use for the background
     */
    protected final void drawBox(final int left, final int top,
        final int right, final int bottom,
        final CellAttributes border, final CellAttributes background) {

        getScreen().drawBox(left, top, right, bottom, border, background);
    }

    /**
     * Draw a box with a border and empty background.
     *
     * @param left left column of box.  0 is the left-most row.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     * @param border attributes to use for the border
     * @param background attributes to use for the background
     * @param borderType if 1, draw a single-line border; if 2, draw a
     * double-line border; if 3, draw double-line top/bottom edges and
     * single-line left/right edges (like Qmodem)
     * @param shadow if true, draw a "shadow" on the box
     */
    protected final void drawBox(final int left, final int top,
        final int right, final int bottom,
        final CellAttributes border, final CellAttributes background,
        final int borderType, final boolean shadow) {

        getScreen().drawBox(left, top, right, bottom, border, background,
            borderType, shadow);
    }

    /**
     * Draw a box shadow.
     *
     * @param left left column of box.  0 is the left-most row.
     * @param top top row of the box.  0 is the top-most row.
     * @param right right column of box
     * @param bottom bottom row of the box
     */
    protected final void drawBoxShadow(final int left, final int top,
        final int right, final int bottom) {

        getScreen().drawBoxShadow(left, top, right, bottom);
    }

    // ------------------------------------------------------------------------
    // Other TWidget constructors ---------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Convenience function to add a label to this container/window.
     *
     * @param text label
     * @param x column relative to parent
     * @param y row relative to parent
     * @return the new label
     */
    public final TLabel addLabel(final String text, final int x, final int y) {
        return addLabel(text, x, y, "tlabel");
    }

    /**
     * Convenience function to add a label to this container/window.
     *
     * @param text label
     * @param x column relative to parent
     * @param y row relative to parent
     * @param action to call when shortcut is pressed
     * @return the new label
     */
    public final TLabel addLabel(final String text, final int x, final int y,
        final TAction action) {

        return addLabel(text, x, y, "tlabel", action);
    }

    /**
     * Convenience function to add a label to this container/window.
     *
     * @param text label
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text.
     * Default is "tlabel"
     * @return the new label
     */
    public final TLabel addLabel(final String text, final int x, final int y,
        final String colorKey) {

        return new TLabel(this, text, x, y, colorKey);
    }

    /**
     * Convenience function to add a label to this container/window.
     *
     * @param text label
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text.
     * Default is "tlabel"
     * @param action to call when shortcut is pressed
     * @return the new label
     */
    public final TLabel addLabel(final String text, final int x, final int y,
        final String colorKey, final TAction action) {

        return new TLabel(this, text, x, y, colorKey, action);
    }

    /**
     * Convenience function to add a label to this container/window.
     *
     * @param text label
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text.
     * Default is "tlabel"
     * @param useWindowBackground if true, use the window's background color
     * @return the new label
     */
    public final TLabel addLabel(final String text, final int x, final int y,
        final String colorKey, final boolean useWindowBackground) {

        return new TLabel(this, text, x, y, colorKey, useWindowBackground);
    }

    /**
     * Convenience function to add a label to this container/window.
     *
     * @param text label
     * @param x column relative to parent
     * @param y row relative to parent
     * @param colorKey ColorTheme key color to use for foreground text.
     * Default is "tlabel"
     * @param useWindowBackground if true, use the window's background color
     * @param action to call when shortcut is pressed
     * @return the new label
     */
    public final TLabel addLabel(final String text, final int x, final int y,
        final String colorKey, final boolean useWindowBackground,
        final TAction action) {

        return new TLabel(this, text, x, y, colorKey, useWindowBackground,
            action);
    }

    /**
     * Convenience function to add a button to this container/window.
     *
     * @param text label on the button
     * @param x column relative to parent
     * @param y row relative to parent
     * @param action action to call when button is pressed
     * @return the new button
     */
    public final TButton addButton(final String text, final int x, final int y,
        final TAction action) {

        return new TButton(this, text, x, y, action);
    }

    /**
     * Convenience function to add a checkbox to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param label label to display next to (right of) the checkbox
     * @param checked initial check state
     * @return the new checkbox
     */
    public final TCheckBox addCheckBox(final int x, final int y,
        final String label, final boolean checked) {

        return new TCheckBox(this, x, y, label, checked);
    }

    /**
     * Convenience function to add a combobox to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible combobox width, including the down-arrow
     * @param values the possible values for the box, shown in the drop-down
     * @param valuesIndex the initial index in values, or -1 for no default
     * value
     * @param maxValuesHeight the maximum height of the values drop-down when
     * it is visible
     * @param updateAction action to call when a new value is selected from
     * the list or enter is pressed in the edit field
     * @return the new combobox
     */
    public final TComboBox addComboBox(final int x, final int y,
        final int width, final List<String> values, final int valuesIndex,
        final int maxValuesHeight, final TAction updateAction) {

        return new TComboBox(this, x, y, width, values, valuesIndex,
            maxValuesHeight, updateAction);
    }

    /**
     * Convenience function to add a spinner to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param upAction action to call when the up arrow is clicked or pressed
     * @param downAction action to call when the down arrow is clicked or
     * pressed
     * @return the new spinner
     */
    public final TSpinner addSpinner(final int x, final int y,
        final TAction upAction, final TAction downAction) {

        return new TSpinner(this, x, y, upAction, downAction);
    }

    /**
     * Convenience function to add a calendar to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param updateAction action to call when the user changes the value of
     * the calendar
     * @return the new calendar
     */
    public final TCalendar addCalendar(final int x, final int y,
        final TAction updateAction) {

        return new TCalendar(this, x, y, updateAction);
    }

    /**
     * Convenience function to add a progress bar to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of progress bar
     * @param value initial value of percent complete
     * @return the new progress bar
     */
    public final TProgressBar addProgressBar(final int x, final int y,
        final int width, final int value) {

        return new TProgressBar(this, x, y, width, value);
    }

    /**
     * Convenience function to add a radio button group to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param label label to display on the group box
     * @return the new radio button group
     */
    public final TRadioGroup addRadioGroup(final int x, final int y,
        final String label) {

        return new TRadioGroup(this, x, y, label);
    }

    /**
     * Convenience function to add a radio button group to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of group
     * @param label label to display on the group box
     */
    public final TRadioGroup addRadioGroup(final int x, final int y,
        final int width, final String label) {

        return new TRadioGroup(this, x, y, width, label);
    }

    /**
     * Convenience function to add a text field to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @return the new text field
     */
    public final TField addField(final int x, final int y,
        final int width, final boolean fixed) {

        return new TField(this, x, y, width, fixed);
    }

    /**
     * Convenience function to add a text field to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @param text initial text, default is empty string
     * @return the new text field
     */
    public final TField addField(final int x, final int y,
        final int width, final boolean fixed, final String text) {

        return new TField(this, x, y, width, fixed, text);
    }

    /**
     * Convenience function to add a text field to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @param text initial text, default is empty string
     * @param enterAction function to call when enter key is pressed
     * @param updateAction function to call when the text is updated
     * @return the new text field
     */
    public final TField addField(final int x, final int y,
        final int width, final boolean fixed, final String text,
        final TAction enterAction, final TAction updateAction) {

        return new TField(this, x, y, width, fixed, text, enterAction,
            updateAction);
    }

    /**
     * Convenience function to add a scrollable text box to this
     * container/window.
     *
     * @param text text on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param colorKey ColorTheme key color to use for foreground text
     * @return the new text box
     */
    public final TText addText(final String text, final int x,
        final int y, final int width, final int height, final String colorKey) {

        return new TText(this, text, x, y, width, height, colorKey);
    }

    /**
     * Convenience function to add a scrollable text box to this
     * container/window.
     *
     * @param text text on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @return the new text box
     */
    public final TText addText(final String text, final int x, final int y,
        final int width, final int height) {

        return new TText(this, text, x, y, width, height, "ttext");
    }

    /**
     * Convenience function to add an editable text area box to this
     * container/window.
     *
     * @param text text on the screen
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @return the new text box
     */
    public final TEditorWidget addEditor(final String text, final int x,
        final int y, final int width, final int height) {

        return new TEditorWidget(this, text, x, y, width, height);
    }

    /**
     * Convenience function to spawn a message box.
     *
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     * @return the new message box
     */
    public final TMessageBox messageBox(final String title,
        final String caption) {

        return getApplication().messageBox(title, caption, TMessageBox.Type.OK);
    }

    /**
     * Convenience function to spawn a message box.
     *
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     * @param type one of the TMessageBox.Type constants.  Default is
     * Type.OK.
     * @return the new message box
     */
    public final TMessageBox messageBox(final String title,
        final String caption, final TMessageBox.Type type) {

        return getApplication().messageBox(title, caption, type);
    }

    /**
     * Convenience function to spawn an input box.
     *
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     * @return the new input box
     */
    public final TInputBox inputBox(final String title, final String caption) {

        return getApplication().inputBox(title, caption);
    }

    /**
     * Convenience function to spawn an input box.
     *
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     * @param text initial text to seed the field with
     * @return the new input box
     */
    public final TInputBox inputBox(final String title, final String caption,
        final String text) {

        return getApplication().inputBox(title, caption, text);
    }

    /**
     * Convenience function to spawn an input box.
     *
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     * @param text initial text to seed the field with
     * @param type one of the Type constants.  Default is Type.OK.
     * @return the new input box
     */
    public final TInputBox inputBox(final String title, final String caption,
        final String text, final TInputBox.Type type) {

        return getApplication().inputBox(title, caption, text, type);
    }

    /**
     * Convenience function to add a password text field to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @return the new text field
     */
    public final TPasswordField addPasswordField(final int x, final int y,
        final int width, final boolean fixed) {

        return new TPasswordField(this, x, y, width, fixed);
    }

    /**
     * Convenience function to add a password text field to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @param text initial text, default is empty string
     * @return the new text field
     */
    public final TPasswordField addPasswordField(final int x, final int y,
        final int width, final boolean fixed, final String text) {

        return new TPasswordField(this, x, y, width, fixed, text);
    }

    /**
     * Convenience function to add a password text field to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width visible text width
     * @param fixed if true, the text cannot exceed the display width
     * @param text initial text, default is empty string
     * @param enterAction function to call when enter key is pressed
     * @param updateAction function to call when the text is updated
     * @return the new text field
     */
    public final TPasswordField addPasswordField(final int x, final int y,
        final int width, final boolean fixed, final String text,
        final TAction enterAction, final TAction updateAction) {

        return new TPasswordField(this, x, y, width, fixed, text, enterAction,
            updateAction);
    }

    /**
     * Convenience function to add a scrollable tree view to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of tree view
     * @param height height of tree view
     * @return the new tree view
     */
    public final TTreeViewWidget addTreeViewWidget(final int x, final int y,
        final int width, final int height) {

        return new TTreeViewWidget(this, x, y, width, height);
    }

    /**
     * Convenience function to add a scrollable tree view to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of tree view
     * @param height height of tree view
     * @param action action to perform when an item is selected
     * @return the new tree view
     */
    public final TTreeViewWidget addTreeViewWidget(final int x, final int y,
        final int width, final int height, final TAction action) {

        return new TTreeViewWidget(this, x, y, width, height, action);
    }

    /**
     * Convenience function to spawn a file open box.
     *
     * @param path path of selected file
     * @return the result of the new file open box
     * @throws IOException if a java.io operation throws
     */
    public final String fileOpenBox(final String path) throws IOException {
        return getApplication().fileOpenBox(path);
    }

    /**
     * Convenience function to spawn a file save box.
     *
     * @param path path of selected file
     * @return the result of the new file open box
     * @throws IOException if a java.io operation throws
     */
    public final String fileSaveBox(final String path) throws IOException {
        return getApplication().fileOpenBox(path, TFileOpenBox.Type.SAVE);
    }

    /**
     * Convenience function to spawn a file open box.
     *
     * @param path path of selected file
     * @param type one of the Type constants
     * @return the result of the new file open box
     * @throws IOException if a java.io operation throws
     */
    public final String fileOpenBox(final String path,
        final TFileOpenBox.Type type) throws IOException {

        return getApplication().fileOpenBox(path, type);
    }

    /**
     * Convenience function to spawn a file open box.
     *
     * @param path path of selected file
     * @param type one of the Type constants
     * @param filter a string that files must match to be displayed
     * @return the result of the new file open box
     * @throws IOException of a java.io operation throws
     */
    public final String fileOpenBox(final String path,
        final TFileOpenBox.Type type, final String filter) throws IOException {

        ArrayList<String> filters = new ArrayList<String>();
        filters.add(filter);

        return getApplication().fileOpenBox(path, type, filters);
    }

    /**
     * Convenience function to spawn a file open box.
     *
     * @param path path of selected file
     * @param type one of the Type constants
     * @param filters a list of strings that files must match to be displayed
     * @return the result of the new file open box
     * @throws IOException of a java.io operation throws
     */
    public final String fileOpenBox(final String path,
        final TFileOpenBox.Type type,
        final List<String> filters) throws IOException {

        return getApplication().fileOpenBox(path, type, filters);
    }

    /**
     * Convenience function to add a directory list to this container/window.
     *
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @return the new directory list
     */
    public final TDirectoryList addDirectoryList(final String path, final int x,
        final int y, final int width, final int height) {

        return new TDirectoryList(this, path, x, y, width, height, null);
    }

    /**
     * Convenience function to add a directory list to this container/window.
     *
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param action action to perform when an item is selected (enter or
     * double-click)
     * @return the new directory list
     */
    public final TDirectoryList addDirectoryList(final String path, final int x,
        final int y, final int width, final int height, final TAction action) {

        return new TDirectoryList(this, path, x, y, width, height, action);
    }

    /**
     * Convenience function to add a directory list to this container/window.
     *
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param action action to perform when an item is selected (enter or
     * double-click)
     * @param singleClickAction action to perform when an item is selected
     * (single-click)
     * @return the new directory list
     */
    public final TDirectoryList addDirectoryList(final String path, final int x,
        final int y, final int width, final int height, final TAction action,
        final TAction singleClickAction) {

        return new TDirectoryList(this, path, x, y, width, height, action,
            singleClickAction);
    }

    /**
     * Convenience function to add a directory list to this container/window.
     *
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param action action to perform when an item is selected (enter or
     * double-click)
     * @param singleClickAction action to perform when an item is selected
     * (single-click)
     * @param filters a list of strings that files must match to be displayed
     * @return the new directory list
     */
    public final TDirectoryList addDirectoryList(final String path, final int x,
        final int y, final int width, final int height, final TAction action,
        final TAction singleClickAction, final List<String> filters) {

        return new TDirectoryList(this, path, x, y, width, height, action,
            singleClickAction, filters);
    }

    /**
     * Convenience function to add a list to this container/window.
     *
     * @param strings list of strings to show
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @return the new directory list
     */
    public final TList addList(final List<String> strings, final int x,
        final int y, final int width, final int height) {

        return new TList(this, strings, x, y, width, height, null);
    }

    /**
     * Convenience function to add a list to this container/window.
     *
     * @param strings list of strings to show
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param enterAction action to perform when an item is selected
     * @return the new directory list
     */
    public final TList addList(final List<String> strings, final int x,
        final int y, final int width, final int height,
        final TAction enterAction) {

        return new TList(this, strings, x, y, width, height, enterAction);
    }

    /**
     * Convenience function to add a list to this container/window.
     *
     * @param strings list of strings to show
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param enterAction action to perform when an item is selected
     * @param moveAction action to perform when the user navigates to a new
     * item with arrow/page keys
     * @return the new directory list
     */
    public final TList addList(final List<String> strings, final int x,
        final int y, final int width, final int height,
        final TAction enterAction, final TAction moveAction) {

        return new TList(this, strings, x, y, width, height, enterAction,
            moveAction);
    }

    /**
     * Convenience function to add a list to this container/window.
     *
     * @param strings list of strings to show.  This is allowed to be null
     * and set later with setList() or by subclasses.
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param enterAction action to perform when an item is selected
     * @param moveAction action to perform when the user navigates to a new
     * item with arrow/page keys
     * @param singleClickAction action to perform when the user clicks on an
     * item
     */
    public TList addList(final List<String> strings, final int x,
        final int y, final int width, final int height,
        final TAction enterAction, final TAction moveAction,
        final TAction singleClickAction) {

        return new TList(this, strings, x, y, width, height, enterAction,
            moveAction, singleClickAction);
    }


    /**
     * Convenience function to add an image to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width number of text cells for width of the image
     * @param height number of text cells for height of the image
     * @param image the image to display
     * @param left left column of the image.  0 is the left-most column.
     * @param top top row of the image.  0 is the top-most row.
     */
    public final TImage addImage(final int x, final int y,
        final int width, final int height,
        final BufferedImage image, final int left, final int top) {

        return new TImage(this, x, y, width, height, image, left, top);
    }

    /**
     * Convenience function to add an image to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width number of text cells for width of the image
     * @param height number of text cells for height of the image
     * @param image the image to display
     * @param left left column of the image.  0 is the left-most column.
     * @param top top row of the image.  0 is the top-most row.
     * @param clickAction function to call when mouse is pressed
     */
    public final TImage addImage(final int x, final int y,
        final int width, final int height,
        final BufferedImage image, final int left, final int top,
        final TAction clickAction) {

        return new TImage(this, x, y, width, height, image, left, top,
            clickAction);
    }

    /**
     * Convenience function to add an editable 2D data table to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of widget
     * @param height height of widget
     */
    public TTableWidget addTable(final int x, final int y, final int width,
        final int height) {

        return new TTableWidget(this, x, y, width, height);
    }

    /**
     * Convenience function to add an editable 2D data table to this
     * container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of widget
     * @param height height of widget
     * @param gridColumns number of columns in grid
     * @param gridRows number of rows in grid
     */
    public TTableWidget addTable(final int x, final int y, final int width,
        final int height, final int gridColumns, final int gridRows) {

        return new TTableWidget(this, x, y, width, height, gridColumns,
            gridRows);
    }

    /**
     * Convenience function to add a panel to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @return the new panel
     */
    public final TPanel addPanel(final int x, final int y, final int width,
        final int height) {

        return new TPanel(this, x, y, width, height);
    }

    /**
     * Convenience function to add a split pane to this container/window.
     *
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param vertical if true, split vertically
     * @return the new split pane
     */
    public final TSplitPane addSplitPane(final int x, final int y,
        final int width, final int height, final boolean vertical) {

        return new TSplitPane(this, x, y, width, height, vertical);
    }

}
