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

import java.util.HashSet;
import java.util.Set;

import jexer.backend.Screen;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.bits.StringUtils;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import jexer.menu.TMenu;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * TWindow is the top-level container and drawing surface for other widgets.
 */
public class TWindow extends TWidget {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Window is resizable (default yes).
     */
    public static final int RESIZABLE   = 0x01;

    /**
     * Window is modal (default no).
     */
    public static final int MODAL       = 0x02;

    /**
     * Window is centered (default no).
     */
    public static final int CENTERED    = 0x04;

    /**
     * Window has no close box (default no).  Window can still be closed via
     * TApplication.closeWindow() and TWindow.close().
     */
    public static final int NOCLOSEBOX  = 0x08;

    /**
     * Window has no maximize box (default no).
     */
    public static final int NOZOOMBOX   = 0x10;

    /**
     * Window is placed at absolute position (no smart placement) (default
     * no).
     */
    public static final int ABSOLUTEXY  = 0x20;

    /**
     * Hitting the closebox with the mouse calls TApplication.hideWindow()
     * rather than TApplication.closeWindow() (default no).
     */
    public static final int HIDEONCLOSE = 0x40;

    /**
     * Menus cannot be used when this window is active (default no).
     */
    public static final int OVERRIDEMENU        = 0x80;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Window flags.  Note package private access.
     */
    int flags = RESIZABLE;

    /**
     * Window title.
     */
    private String title = "";

    /**
     * Window's parent TApplication.
     */
    private TApplication application;

    /**
     * Z order.  Lower number means more in-front.
     */
    private int z = 0;

    /**
     * Window's keyboard shortcuts.  Any key in this set will be passed to
     * the window directly rather than processed through the menu
     * accelerators.
     */
    private Set<TKeypress> keyboardShortcuts = new HashSet<TKeypress>();

    /**
     * If true, then the user clicked on the title bar and is moving the
     * window.
     */
    protected boolean inWindowMove = false;

    /**
     * If true, then the user clicked on the bottom right corner and is
     * resizing the window.
     */
    protected boolean inWindowResize = false;

    /**
     * If true, then the user selected "Size/Move" (or hit Ctrl-F5) and is
     * resizing/moving the window via the keyboard.
     */
    protected boolean inKeyboardResize = false;

    /**
     * If true, this window is maximized.
     */
    private boolean maximized = false;

    /**
     * Remember mouse state.
     */
    protected TMouseEvent mouse;

    // For moving the window.  resizing also uses moveWindowMouseX/Y
    private int moveWindowMouseX;
    private int moveWindowMouseY;
    private int oldWindowX;
    private int oldWindowY;

    // Resizing
    private int resizeWindowWidth;
    private int resizeWindowHeight;
    private int minimumWindowWidth = 10;
    private int minimumWindowHeight = 2;
    private int maximumWindowWidth = -1;
    private int maximumWindowHeight = -1;

    // For maximize/restore
    private int restoreWindowWidth;
    private int restoreWindowHeight;
    private int restoreWindowX;
    private int restoreWindowY;

    /**
     * Hidden flag.  A hidden window will still have its onIdle() called, and
     * will also have onClose() called at application exit.  Note package
     * private access: TApplication will force hidden false if a modal window
     * is active.
     */
    boolean hidden = false;

    /**
     * A window may have a status bar associated with it.  TApplication will
     * draw this status bar last, and will also route events to it first
     * before the window.
     */
    protected TStatusBar statusBar = null;

    /**
     * A window may request that TApplication NOT draw the mouse cursor over
     * it by setting this to true.  This is currently only used within Jexer
     * by TTerminalWindow so that only the bottom-most instance of nested
     * Jexer's draws the mouse within its application window.  But perhaps
     * other applications can use it, so public getter/setter is provided.
     */
    private boolean hideMouse = false;

    /**
     * The help topic for this window.
     */
    protected String helpTopic = "Help";

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  Window will be located at (0, 0).
     *
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param width width of window
     * @param height height of window
     */
    public TWindow(final TApplication application, final String title,
        final int width, final int height) {

        this(application, title, 0, 0, width, height, RESIZABLE);
    }

    /**
     * Public constructor.  Window will be located at (0, 0).
     *
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param width width of window
     * @param height height of window
     * @param flags bitmask of RESIZABLE, CENTERED, or MODAL
     */
    public TWindow(final TApplication application, final String title,
        final int width, final int height, final int flags) {

        this(application, title, 0, 0, width, height, flags);
    }

    /**
     * Public constructor.
     *
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of window
     * @param height height of window
     */
    public TWindow(final TApplication application, final String title,
        final int x, final int y, final int width, final int height) {

        this(application, title, x, y, width, height, RESIZABLE);
    }

    /**
     * Public constructor.
     *
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of window
     * @param height height of window
     * @param flags mask of RESIZABLE, CENTERED, or MODAL
     */
    public TWindow(final TApplication application, final String title,
        final int x, final int y, final int width, final int height,
        final int flags) {

        super();

        // I am my own window and parent
        setupForTWindow(this, x, y + application.getDesktopTop(),
            width, height);

        // Save fields
        this.title       = title;
        this.application = application;
        this.flags       = flags;

        // Minimum width/height are 10 and 2
        assert (width >= 10);
        assert (getHeight() >= 2);

        // MODAL implies CENTERED
        if (isModal()) {
            this.flags |= CENTERED;
        }

        // Center window if specified
        center();

        // Add me to the application
        application.addWindowToApplication(this);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns true if the mouse is currently on the close button.
     *
     * @return true if mouse is currently on the close button
     */
    protected boolean mouseOnClose() {
        if ((flags & NOCLOSEBOX) != 0) {
            return false;
        }
        if ((mouse != null)
            && (mouse.getAbsoluteY() == getY())
            && (mouse.getAbsoluteX() == getX() + 3)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the mouse is currently on the maximize/restore button.
     *
     * @return true if the mouse is currently on the maximize/restore button
     */
    protected boolean mouseOnMaximize() {
        if ((flags & NOZOOMBOX) != 0) {
            return false;
        }
        if ((mouse != null)
            && !isModal()
            && (mouse.getAbsoluteY() == getY())
            && (mouse.getAbsoluteX() == getX() + getWidth() - 4)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the mouse is currently on the resizable lower right
     * corner.
     *
     * @return true if the mouse is currently on the resizable lower right
     * corner
     */
    protected boolean mouseOnResize() {
        if (((flags & RESIZABLE) != 0)
            && !isModal()
            && (mouse != null)
            && (mouse.getAbsoluteY() == getY() + getHeight() - 1)
            && ((mouse.getAbsoluteX() == getX() + getWidth() - 1)
                || (mouse.getAbsoluteX() == getX() + getWidth() - 2))
        ) {
            return true;
        }
        return false;
    }

    /**
     * Subclasses should override this method to perform any user prompting
     * before they are offscreen.  Note that unlike other windowing toolkits,
     * windows can NOT use this function in some manner to avoid being
     * closed.  This is called by application.closeWindow().
     */
    protected void onPreClose() {
        // Default: do nothing.
    }

    /**
     * Subclasses should override this method to cleanup resources.  This is
     * called by application.closeWindow().
     */
    protected void onClose() {
        // Default: perform widget-specific cleanup.
        for (TWidget w: getChildren()) {
            w.close();
        }
    }

    /**
     * Called by application.switchWindow() when this window gets the
     * focus, and also by application.addWindow().
     */
    protected void onFocus() {
        // Default: do nothing
    }

    /**
     * Called by application.switchWindow() when another window gets the
     * focus.
     */
    protected void onUnfocus() {
        // Default: do nothing
    }

    /**
     * Called by application.hideWindow().
     */
    protected void onHide() {
        // Default: do nothing
    }

    /**
     * Called by application.showWindow().
     */
    protected void onShow() {
        // Default: do nothing
    }

    /**
     * Handle mouse button presses.
     *
     * @param mouse mouse button event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        this.mouse = mouse;

        inKeyboardResize = false;
        inWindowMove = false;
        inWindowResize = false;

        if ((mouse.getAbsoluteY() == getY())
            && mouse.isMouse1()
            && (getX() <= mouse.getAbsoluteX())
            && (mouse.getAbsoluteX() < getX() + getWidth())
            && !mouseOnClose()
            && !mouseOnMaximize()
        ) {
            // Begin moving window
            inWindowMove = true;
            moveWindowMouseX = mouse.getAbsoluteX();
            moveWindowMouseY = mouse.getAbsoluteY();
            oldWindowX = getX();
            oldWindowY = getY();
            if (maximized) {
                maximized = false;
            }
            return;
        }
        if (mouseOnResize()) {
            // Begin window resize
            inWindowResize = true;
            moveWindowMouseX = mouse.getAbsoluteX();
            moveWindowMouseY = mouse.getAbsoluteY();
            resizeWindowWidth = getWidth();
            resizeWindowHeight = getHeight();
            if (maximized) {
                maximized = false;
            }
            return;
        }

        // Give the shortcut bar a shot at this.
        if (statusBar != null) {
            if (statusBar.statusBarMouseDown(mouse)) {
                return;
            }
        }

        // I didn't take it, pass it on to my children
        super.onMouseDown(mouse);
    }

    /**
     * Handle mouse button releases.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        this.mouse = mouse;

        if ((inWindowMove) && (mouse.isMouse1())) {
            // Stop moving window
            inWindowMove = false;
            return;
        }

        if ((inWindowResize) && (mouse.isMouse1())) {
            // Stop resizing window
            inWindowResize = false;
            return;
        }

        if (mouse.isMouse1() && mouseOnClose()) {
            if ((flags & HIDEONCLOSE) == 0) {
                // Close window
                application.closeWindow(this);
            } else {
                // Hide window
                application.hideWindow(this);
            }
            return;
        }

        if ((mouse.getAbsoluteY() == getY())
            && mouse.isMouse1()
            && mouseOnMaximize()) {
            if (maximized) {
                // Restore
                restore();
            } else {
                // Maximize
                maximize();
            }
            // Pass a resize event to my children
            onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                    getWidth(), getHeight()));
            return;
        }

        // Give the shortcut bar a shot at this.
        if (statusBar != null) {
            if (statusBar.statusBarMouseUp(mouse)) {
                return;
            }
        }

        // I didn't take it, pass it on to my children
        super.onMouseUp(mouse);
    }

    /**
     * Handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        this.mouse = mouse;

        if (inWindowMove) {
            // Move window over
            setX(oldWindowX + (mouse.getAbsoluteX() - moveWindowMouseX));
            setY(oldWindowY + (mouse.getAbsoluteY() - moveWindowMouseY));
            // Don't cover up the menu bar
            if (getY() < application.getDesktopTop()) {
                setY(application.getDesktopTop());
            }
            // Don't go below the status bar
            if (getY() >= application.getDesktopBottom()) {
                setY(application.getDesktopBottom() - 1);
            }
            return;
        }

        if (inWindowResize) {
            // Move window over
            setWidth(resizeWindowWidth + (mouse.getAbsoluteX()
                    - moveWindowMouseX));
            setHeight(resizeWindowHeight + (mouse.getAbsoluteY()
                    - moveWindowMouseY));
            if (getX() + getWidth() > getScreen().getWidth()) {
                setWidth(getScreen().getWidth() - getX());
            }
            if (getY() + getHeight() > application.getDesktopBottom()) {
                setY(application.getDesktopBottom() - getHeight() + 1);
            }
            // Don't cover up the menu bar
            if (getY() < application.getDesktopTop()) {
                setY(application.getDesktopTop());
            }

            // Keep within min/max bounds
            if (getWidth() < minimumWindowWidth) {
                setWidth(minimumWindowWidth);
            }
            if (getHeight() < minimumWindowHeight) {
                setHeight(minimumWindowHeight);
            }
            if ((maximumWindowWidth > 0)
                && (getWidth() > maximumWindowWidth)
            ) {
                setWidth(maximumWindowWidth);
            }
            if ((maximumWindowHeight > 0)
                && (getHeight() > maximumWindowHeight)
            ) {
                setHeight(maximumWindowHeight);
            }
            if (getHeight() + getY() >= getApplication().getDesktopBottom()) {
                setHeight(getApplication().getDesktopBottom() - getY());
            }

            // Pass a resize event to my children
            onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                    getWidth(), getHeight()));
            return;
        }

        // Give the shortcut bar a shot at this.
        if (statusBar != null) {
            statusBar.statusBarMouseMotion(mouse);
        }

        // I didn't take it, pass it on to my children
        super.onMouseMotion(mouse);
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {

        if (inWindowMove || inWindowResize) {
            // ESC or ENTER - Exit size/move
            if (keypress.equals(kbEsc) || keypress.equals(kbEnter)) {
                inWindowMove = false;
                inWindowResize = false;
                return;
            }
        }

        if (inKeyboardResize) {

            // ESC or ENTER - Exit size/move
            if (keypress.equals(kbEsc) || keypress.equals(kbEnter)) {
                inKeyboardResize = false;
            }

            if (keypress.equals(kbLeft)) {
                if (getX() > 0) {
                    setX(getX() - 1);
                }
            }
            if (keypress.equals(kbRight)) {
                if (getX() < getScreen().getWidth() - 1) {
                    setX(getX() + 1);
                }
            }
            if (keypress.equals(kbDown)) {
                if (getY() < application.getDesktopBottom() - 1) {
                    setY(getY() + 1);
                }
            }
            if (keypress.equals(kbUp)) {
                if (getY() > 1) {
                    setY(getY() - 1);
                }
            }

            /*
             * Only permit keyboard resizing if the window was RESIZABLE.
             */
            if ((flags & RESIZABLE) != 0) {

                if (keypress.equals(kbShiftLeft)) {
                    if ((getWidth() > minimumWindowWidth)
                        || (minimumWindowWidth <= 0)
                    ) {
                        setWidth(getWidth() - 1);
                    }
                }
                if (keypress.equals(kbShiftRight)) {
                    if ((getWidth() < maximumWindowWidth)
                        || (maximumWindowWidth <= 0)
                    ) {
                        setWidth(getWidth() + 1);
                    }
                }
                if (keypress.equals(kbShiftUp)) {
                    if ((getHeight() > minimumWindowHeight)
                        || (minimumWindowHeight <= 0)
                    ) {
                        setHeight(getHeight() - 1);
                    }
                }
                if (keypress.equals(kbShiftDown)) {
                    if ((getHeight() < maximumWindowHeight)
                        || (maximumWindowHeight <= 0)
                    ) {
                        setHeight(getHeight() + 1);
                    }
                }

                // Pass a resize event to my children
                onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                        getWidth(), getHeight()));

            } // if ((flags & RESIZABLE) != 0)

            return;
        }

        // Give the shortcut bar a shot at this.
        if (statusBar != null) {
            if (statusBar.statusBarKeypress(keypress)) {
                return;
            }
        }

        // These keystrokes will typically not be seen unless a subclass
        // overrides onMenu() due to how TApplication dispatches
        // accelerators.

        if (!(this instanceof TDesktop)) {

            // Ctrl-W - close window
            if (keypress.equals(kbCtrlW)) {
                if ((flags & NOCLOSEBOX) == 0) {
                    if ((flags & HIDEONCLOSE) == 0) {
                        // Close window
                        application.closeWindow(this);
                    } else {
                        // Hide window
                        application.hideWindow(this);
                    }
                }
                return;
            }

            // F6 - behave like Alt-TAB
            if (keypress.equals(kbF6)) {
                application.switchWindow(true);
                return;
            }

            // Shift-F6 - behave like Shift-Alt-TAB
            if (keypress.equals(kbShiftF6)) {
                application.switchWindow(false);
                return;
            }

            // F5 - zoom
            if (keypress.equals(kbF5) && ((flags & NOZOOMBOX) == 0)) {
                if (maximized) {
                    restore();
                } else {
                    maximize();
                }
            }

            // Ctrl-F5 - size/move
            if (keypress.equals(kbCtrlF5)) {
                inKeyboardResize = !inKeyboardResize;
            }

        } // if (!(this instanceof TDesktop))

        // I didn't take it, pass it on to my children
        super.onKeypress(keypress);
    }

    /**
     * Handle posted command events.
     *
     * @param command command event
     */
    @Override
    public void onCommand(final TCommandEvent command) {

        // These commands will typically not be seen unless a subclass
        // overrides onMenu() due to how TApplication dispatches
        // accelerators.

        if (!(this instanceof TDesktop)) {

            if (command.equals(cmWindowClose)) {
                if ((flags & NOCLOSEBOX) == 0) {
                    if ((flags & HIDEONCLOSE) == 0) {
                        // Close window
                        application.closeWindow(this);
                    } else {
                        // Hide window
                        application.hideWindow(this);
                    }
                }
                return;
            }

            if (command.equals(cmWindowNext)) {
                application.switchWindow(true);
                return;
            }

            if (command.equals(cmWindowPrevious)) {
                application.switchWindow(false);
                return;
            }

            if (command.equals(cmWindowMove)) {
                inKeyboardResize = true;
                return;
            }

            if (command.equals(cmWindowZoom) && ((flags & NOZOOMBOX) == 0)) {
                if (maximized) {
                    restore();
                } else {
                    maximize();
                }
            }

        } // if (!(this instanceof TDesktop))

        // I didn't take it, pass it on to my children
        super.onCommand(command);
    }

    /**
     * Handle posted menu events.
     *
     * @param menu menu event
     */
    @Override
    public void onMenu(final TMenuEvent menu) {

        if (!(this instanceof TDesktop)) {

            if (menu.getId() == TMenu.MID_WINDOW_CLOSE) {
                if ((flags & NOCLOSEBOX) == 0) {
                    if ((flags & HIDEONCLOSE) == 0) {
                        // Close window
                        application.closeWindow(this);
                    } else {
                        // Hide window
                        application.hideWindow(this);
                    }
                }
                return;
            }

            if (menu.getId() == TMenu.MID_WINDOW_NEXT) {
                application.switchWindow(true);
                return;
            }

            if (menu.getId() == TMenu.MID_WINDOW_PREVIOUS) {
                application.switchWindow(false);
                return;
            }

            if (menu.getId() == TMenu.MID_WINDOW_MOVE) {
                inKeyboardResize = true;
                return;
            }

            if ((menu.getId() == TMenu.MID_WINDOW_ZOOM)
                && ((flags & NOZOOMBOX) == 0)
            ) {
                if (maximized) {
                    restore();
                } else {
                    maximize();
                }
                return;
            }

        } // if (!(this instanceof TDesktop))

        // I didn't take it, pass it on to my children
        super.onMenu(menu);
    }

    /**
     * Method that subclasses can override to handle window/screen resize
     * events.
     *
     * @param resize resize event
     */
    @Override
    public void onResize(final TResizeEvent resize) {
        if (resize.getType() == TResizeEvent.Type.WIDGET) {
            if (getChildren().size() == 1) {
                TWidget child = getChildren().get(0);
                if ((child instanceof TSplitPane)
                    || (child instanceof TPanel)
                ) {
                    if (this instanceof TDesktop) {
                        child.onResize(new TResizeEvent(
                            TResizeEvent.Type.WIDGET,
                            resize.getWidth(), resize.getHeight()));
                    } else {
                        child.onResize(new TResizeEvent(
                            TResizeEvent.Type.WIDGET,
                            resize.getWidth() - 2, resize.getHeight() - 2));
                    }
                }
                return;
            }
        }

        // Pass on to TWidget.
        super.onResize(resize);
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get this TWindow's parent TApplication.
     *
     * @return this TWindow's parent TApplication
     */
    @Override
    public final TApplication getApplication() {
        return application;
    }

    /**
     * Get the Screen.
     *
     * @return the Screen
     */
    @Override
    public final Screen getScreen() {
        return application.getScreen();
    }

    /**
     * Called by TApplication.drawChildren() to render on screen.
     */
    @Override
    public void draw() {
        // Draw the box and background first.
        CellAttributes border = getBorder();
        CellAttributes background = getBackground();
        int borderType = getBorderType();

        drawBox(0, 0, getWidth(), getHeight(), border, background, borderType,
            true);

        // Draw the title
        int titleLength = StringUtils.width(title);
        int titleLeft = (getWidth() - titleLength - 2) / 2;
        putCharXY(titleLeft, 0, ' ', border);
        putStringXY(titleLeft + 1, 0, title, border);
        putCharXY(titleLeft + titleLength + 1, 0, ' ', border);

        if (isActive()) {

            // Draw the close button
            if ((flags & NOCLOSEBOX) == 0) {
                putCharXY(2, 0, '[', border);
                putCharXY(4, 0, ']', border);
                if (mouseOnClose() && mouse.isMouse1()) {
                    putCharXY(3, 0, GraphicsChars.CP437[0x0F],
                        getBorderControls());
                } else {
                    putCharXY(3, 0, GraphicsChars.CP437[0xFE],
                        getBorderControls());
                }
            }

            // Draw the maximize button
            if (!isModal() && ((flags & NOZOOMBOX) == 0)) {

                putCharXY(getWidth() - 5, 0, '[', border);
                putCharXY(getWidth() - 3, 0, ']', border);
                if (mouseOnMaximize() && mouse.isMouse1()) {
                    putCharXY(getWidth() - 4, 0, GraphicsChars.CP437[0x0F],
                        getBorderControls());
                } else {
                    if (maximized) {
                        putCharXY(getWidth() - 4, 0, GraphicsChars.CP437[0x12],
                            getBorderControls());
                    } else {
                        putCharXY(getWidth() - 4, 0, GraphicsChars.UPARROW,
                            getBorderControls());
                    }
                }

                // Draw the resize corner
                if ((flags & RESIZABLE) != 0) {
                    putCharXY(getWidth() - 2, getHeight() - 1,
                        GraphicsChars.SINGLE_BAR, getBorderControls());
                    putCharXY(getWidth() - 1, getHeight() - 1,
                        GraphicsChars.LRCORNER, getBorderControls());
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get window title.
     *
     * @return window title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Set window title.
     *
     * @param title new window title
     */
    public final void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Get Z order.  Lower number means more in-front.
     *
     * @return Z value.  Lower number means more in-front.
     */
    public final int getZ() {
        return z;
    }

    /**
     * Set Z order.  Lower number means more in-front.
     *
     * @param z the new Z value.  Lower number means more in-front.
     */
    public final void setZ(final int z) {
        this.z = z;
    }

    /**
     * Add a keypress to be overridden for this window.
     *
     * @param key the key to start taking control of
     */
    protected void addShortcutKeypress(final TKeypress key) {
        keyboardShortcuts.add(key);
    }

    /**
     * Remove a keypress to be overridden for this window.
     *
     * @param key the key to stop taking control of
     */
    protected void removeShortcutKeypress(final TKeypress key) {
        keyboardShortcuts.remove(key);
    }

    /**
     * Remove all keypresses to be overridden for this window.
     */
    protected void clearShortcutKeypresses() {
        keyboardShortcuts.clear();
    }

    /**
     * Determine if a keypress is overridden for this window.
     *
     * @param key the key to check
     * @return true if this window wants to process this key on its own
     */
    public boolean isShortcutKeypress(final TKeypress key) {
        return keyboardShortcuts.contains(key);
    }

    /**
     * Get the window's status bar, or null if it does not have one.
     *
     * @return the status bar, or null
     */
    public TStatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Set the window's status bar to a new one.
     *
     * @param text the status bar text
     * @return the status bar
     */
    public TStatusBar newStatusBar(final String text) {
        statusBar = new TStatusBar(this, text);
        return statusBar;
    }

    /**
     * Set the maximum width for this window.
     *
     * @param maximumWindowWidth new maximum width
     */
    public final void setMaximumWindowWidth(final int maximumWindowWidth) {
        if ((maximumWindowWidth != -1)
            && (maximumWindowWidth < minimumWindowWidth + 1)
        ) {
            throw new IllegalArgumentException("Maximum window width cannot " +
                "be smaller than minimum window width + 1");
        }
        this.maximumWindowWidth = maximumWindowWidth;
    }

    /**
     * Set the minimum width for this window.
     *
     * @param minimumWindowWidth new minimum width
     */
    public final void setMinimumWindowWidth(final int minimumWindowWidth) {
        if ((maximumWindowWidth != -1)
            && (minimumWindowWidth > maximumWindowWidth - 1)
        ) {
            throw new IllegalArgumentException("Minimum window width cannot " +
                "be larger than maximum window width - 1");
        }
        this.minimumWindowWidth = minimumWindowWidth;
    }

    /**
     * Set the maximum height for this window.
     *
     * @param maximumWindowHeight new maximum height
     */
    public final void setMaximumWindowHeight(final int maximumWindowHeight) {
        if ((maximumWindowHeight != -1)
            && (maximumWindowHeight < minimumWindowHeight + 1)
        ) {
            throw new IllegalArgumentException("Maximum window height cannot " +
                "be smaller than minimum window height + 1");
        }
        this.maximumWindowHeight = maximumWindowHeight;
    }

    /**
     * Set the minimum height for this window.
     *
     * @param minimumWindowHeight new minimum height
     */
    public final void setMinimumWindowHeight(final int minimumWindowHeight) {
        if ((maximumWindowHeight != -1)
            && (minimumWindowHeight > maximumWindowHeight - 1)
        ) {
            throw new IllegalArgumentException("Minimum window height cannot " +
                "be larger than maximum window height - 1");
        }
        this.minimumWindowHeight = minimumWindowHeight;
    }

    /**
     * Recenter the window on-screen.
     */
    public final void center() {
        if ((flags & CENTERED) != 0) {
            if (getWidth() < getScreen().getWidth()) {
                setX((getScreen().getWidth() - getWidth()) / 2);
            } else {
                setX(0);
            }
            setY(((application.getDesktopBottom()
                    - application.getDesktopTop()) - getHeight()) / 2);
            if (getY() < 0) {
                setY(0);
            }
            setY(getY() + application.getDesktopTop());
        }
    }

    /**
     * Maximize window.
     */
    public void maximize() {
        if (maximized) {
            return;
        }

        restoreWindowWidth = getWidth();
        restoreWindowHeight = getHeight();
        restoreWindowX = getX();
        restoreWindowY = getY();
        setWidth(getScreen().getWidth());
        setHeight(application.getDesktopBottom() - application.getDesktopTop());
        setX(0);
        setY(application.getDesktopTop());
        maximized = true;

        onResize(new TResizeEvent(TResizeEvent.Type.WIDGET, getWidth(),
                getHeight()));
    }

    /**
     * Restore (unmaximize) window.
     */
    public void restore() {
        if (!maximized) {
            return;
        }

        setWidth(restoreWindowWidth);
        setHeight(restoreWindowHeight);
        setX(restoreWindowX);
        setY(restoreWindowY);
        maximized = false;

        onResize(new TResizeEvent(TResizeEvent.Type.WIDGET, getWidth(),
                getHeight()));
    }

    /**
     * Returns true if this window is hidden.
     *
     * @return true if this window is hidden, false if the window is shown
     */
    public final boolean isHidden() {
        return hidden;
    }

    /**
     * Returns true if this window is shown.
     *
     * @return true if this window is shown, false if the window is hidden
     */
    public final boolean isShown() {
        return !hidden;
    }

    /**
     * Hide window.  A hidden window will still have its onIdle() called, and
     * will also have onClose() called at application exit.  Hidden windows
     * will not receive any other events.
     */
    public void hide() {
        application.hideWindow(this);
    }

    /**
     * Show window.
     */
    public void show() {
        application.showWindow(this);
    }

    /**
     * Activate window (bring to top and receive events).
     */
    @Override
    public void activate() {
        application.activateWindow(this);
    }

    /**
     * Close window.  Note that windows without a close box can still be
     * closed by calling the close() method.
     */
    @Override
    public void close() {
        application.closeWindow(this);
    }

    /**
     * See if this window is undergoing any movement/resize/etc.
     *
     * @return true if the window is moving
     */
    public boolean inMovements() {
        if (inWindowResize || inWindowMove || inKeyboardResize) {
            return true;
        }
        return false;
    }

    /**
     * Stop any pending movement/resize/etc.
     */
    public void stopMovements() {
        inWindowResize = false;
        inWindowMove = false;
        inKeyboardResize = false;
    }

    /**
     * Returns true if this window is modal.
     *
     * @return true if this window is modal
     */
    public final boolean isModal() {
        if ((flags & MODAL) == 0) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if this window has a close box.
     *
     * @return true if this window has a close box
     */
    public final boolean hasCloseBox() {
        if ((flags & NOCLOSEBOX) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if this window has a maximize/zoom box.
     *
     * @return true if this window has a maximize/zoom box
     */
    public final boolean hasZoomBox() {
        if ((flags & NOZOOMBOX) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if this window does not want menus to work while it is
     * visible.
     *
     * @return true if this window does not want menus to work while it is
     * visible
     */
    public final boolean hasOverriddenMenu() {
        if ((flags & OVERRIDEMENU) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Retrieve the background color.
     *
     * @return the background color
     */
    public CellAttributes getBackground() {
        if (!isModal()
            && (inWindowMove || inWindowResize || inKeyboardResize)
        ) {
            assert (isActive());
            return getTheme().getColor("twindow.background.windowmove");
        } else if (isModal() && inWindowMove) {
            assert (isActive());
            return getTheme().getColor("twindow.background.modal");
        } else if (isModal()) {
            if (isActive()) {
                return getTheme().getColor("twindow.background.modal");
            }
            return getTheme().getColor("twindow.background.modal.inactive");
        } else if (isActive()) {
            assert (!isModal());
            return getTheme().getColor("twindow.background");
        } else {
            assert (!isModal());
            return getTheme().getColor("twindow.background.inactive");
        }
    }

    /**
     * Retrieve the border color.
     *
     * @return the border color
     */
    public CellAttributes getBorder() {
        if (!isModal()
            && (inWindowMove || inWindowResize || inKeyboardResize)
        ) {
            if (!isActive()) {
                // The user's terminal never passed a mouse up event, and now
                // another window is active but we never finished a drag.
                inWindowMove = false;
                inWindowResize = false;
                inKeyboardResize = false;
                return getTheme().getColor("twindow.border.inactive");
            }

            return getTheme().getColor("twindow.border.windowmove");
        } else if (isModal() && inWindowMove) {
            assert (isActive());
            return getTheme().getColor("twindow.border.modal.windowmove");
        } else if (isModal()) {
            if (isActive()) {
                return getTheme().getColor("twindow.border.modal");
            } else {
                return getTheme().getColor("twindow.border.modal.inactive");
            }
        } else if (isActive()) {
            assert (!isModal());
            return getTheme().getColor("twindow.border");
        } else {
            assert (!isModal());
            return getTheme().getColor("twindow.border.inactive");
        }
    }

    /**
     * Retrieve the color used by the window movement/sizing controls.
     *
     * @return the color used by the zoom box, resize bar, and close box
     */
    public CellAttributes getBorderControls() {
        if (isModal()) {
            return getTheme().getColor("twindow.border.modal.windowmove");
        }
        return getTheme().getColor("twindow.border.windowmove");
    }

    /**
     * Retrieve the border line type.
     *
     * @return the border line type
     */
    private int getBorderType() {
        if (!isModal()
            && (inWindowMove || inWindowResize || inKeyboardResize)
        ) {
            assert (isActive());
            return 1;
        } else if (isModal() && inWindowMove) {
            assert (isActive());
            return 1;
        } else if (isModal()) {
            if (isActive()) {
                return 2;
            } else {
                return 1;
            }
        } else if (isActive()) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Returns true if this window does not want the application-wide mouse
     * cursor drawn over it.
     *
     * @return true if this window does not want the application-wide mouse
     * cursor drawn over it
     */
    public boolean hasHiddenMouse() {
        return hideMouse;
    }

    /**
     * Set request to prevent the application-wide mouse cursor from being
     * drawn over this window.
     *
     * @param hideMouse if true, this window does not want the
     * application-wide mouse cursor drawn over it
     */
    public final void setHiddenMouse(final boolean hideMouse) {
        this.hideMouse = hideMouse;
    }

    /**
     * Get this window's help topic to load.
     *
     * @return the topic name
     */
    public String getHelpTopic() {
        return helpTopic;
    }

    /**
     * Generate a human-readable string for this window.
     *
     * @return a human-readable string
     */
    @Override
    public String toString() {
        return String.format("%s(%8x) \'%s\' Z %d position (%d, %d) " +
            "geometry %dx%d  hidden %s modal %s",
            getClass().getName(), hashCode(), title, getZ(),
            getX(), getY(), getWidth(), getHeight(), hidden, isModal());
    }

}
