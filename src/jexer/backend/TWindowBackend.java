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
package jexer.backend;

import java.util.ArrayList;
import java.util.List;

import jexer.TApplication;
import jexer.TWindow;
import jexer.event.TCommandEvent;
import jexer.event.TInputEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import static jexer.TCommand.*;

/**
 * TWindowBackend uses a window in one TApplication to provide a backend for
 * another TApplication.
 *
 * Note that TWindow has its own getScreen() and setTitle() functions.
 * Clients in TWindowBackend's application won't be able to use it to get at
 * the other application's screen.  getOtherScreen() has been provided.
 */
public class TWindowBackend extends TWindow implements Backend {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The listening object that run() wakes up on new input.
     */
    private Object listener;

    /**
     * The object to sync on in draw().  This is normally otherScreen, but it
     * could also be a MultiScreen.
     */
    private Object drawLock;

    /**
     * The event queue, filled up by a thread reading on input.
     */
    private List<TInputEvent> eventQueue;

    /**
     * The screen this window is monitoring.
     */
    private Screen otherScreen;

    /**
     * The application associated with otherScreen.
     */
    private TApplication otherApplication;

    /**
     * The session information.
     */
    private SessionInfo sessionInfo;

    /**
     * OtherScreen provides a hook to notify TWindowBackend of screen size
     * changes.
     */
    private class OtherScreen extends LogicalScreen {

        /**
         * The TWindowBackend to notify.
         */
        private TWindowBackend window;

        /**
         * Public constructor.
         */
        public OtherScreen(final TWindowBackend window) {
            this.window = window;
        }

        /**
         * Resize the physical screen to match the logical screen dimensions.
         */
        @Override
        public void resizeToScreen() {
            window.setWidth(getWidth() + 2);
            window.setHeight(getHeight() + 2);
        }

        /**
         * Get the width of a character cell in pixels.
         *
         * @return the width in pixels of a character cell
         */
        @Override
        public int getTextWidth() {
            return window.getScreen().getTextWidth();
        }

        /**
         * Get the height of a character cell in pixels.
         *
         * @return the height in pixels of a character cell
         */
        @Override
        public int getTextHeight() {
            return window.getScreen().getTextHeight();
        }

    }


    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  Window will be located at (0, 0).
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param width width of window
     * @param height height of window
     */
    public TWindowBackend(final Object listener,
        final TApplication application, final String title,
        final int width, final int height) {

        super(application, title, width, height);

        this.listener = listener;
        eventQueue = new ArrayList<TInputEvent>();
        sessionInfo = new TSessionInfo(width, height);
        otherScreen = new OtherScreen(this);
        otherScreen.setDimensions(width - 2, height - 2);
        drawLock = otherScreen;
        setHiddenMouse(true);
    }

    /**
     * Public constructor.  Window will be located at (0, 0).
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param width width of window
     * @param height height of window
     * @param flags bitmask of RESIZABLE, CENTERED, or MODAL
     */
    public TWindowBackend(final Object listener,
        final TApplication application, final String title,
        final int width, final int height, final int flags) {

        super(application, title, width, height, flags);

        this.listener = listener;
        eventQueue = new ArrayList<TInputEvent>();
        sessionInfo = new TSessionInfo(width, height);
        otherScreen = new OtherScreen(this);
        otherScreen.setDimensions(width - 2, height - 2);
        drawLock = otherScreen;
        setHiddenMouse(true);
    }

    /**
     * Public constructor.
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of window
     * @param height height of window
     */
    public TWindowBackend(final Object listener,
        final TApplication application, final String title,
        final int x, final int y, final int width, final int height) {

        super(application, title, x, y, width, height);

        this.listener = listener;
        eventQueue = new ArrayList<TInputEvent>();
        sessionInfo = new TSessionInfo(width, height);
        otherScreen = new OtherScreen(this);
        otherScreen.setDimensions(width - 2, height - 2);
        drawLock = otherScreen;
        setHiddenMouse(true);
    }

    /**
     * Public constructor.
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of window
     * @param height height of window
     * @param flags mask of RESIZABLE, CENTERED, or MODAL
     */
    public TWindowBackend(final Object listener,
        final TApplication application, final String title,
        final int x, final int y, final int width, final int height,
        final int flags) {

        super(application, title, x, y, width, height, flags);

        this.listener = listener;
        eventQueue = new ArrayList<TInputEvent>();
        sessionInfo = new TSessionInfo(width, height);
        otherScreen = new OtherScreen(this);
        otherScreen.setDimensions(width - 2, height - 2);
        drawLock = otherScreen;
        setHiddenMouse(true);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle window/screen resize events.
     *
     * @param event resize event
     */
    @Override
    public void onResize(final TResizeEvent event) {
        if (event.getType() == TResizeEvent.Type.WIDGET) {
            int newWidth = event.getWidth() - 2;
            int newHeight = event.getHeight() - 2;
            if ((newWidth != otherScreen.getWidth())
                || (newHeight != otherScreen.getHeight())
            ) {
                // I was resized, notify the screen I am watching to match my
                // new size.
                synchronized (eventQueue) {
                    eventQueue.add(new TResizeEvent(TResizeEvent.Type.SCREEN,
                            newWidth, newHeight));
                }
                synchronized (listener) {
                    listener.notifyAll();
                }
            }
            return;
        } else {
            super.onResize(event);
        }
    }

    /**
     * Returns true if the mouse is currently in the otherScreen window.
     *
     * @param mouse mouse event
     * @return true if mouse is currently in the otherScreen window.
     */
    protected boolean mouseOnOtherScreen(final TMouseEvent mouse) {
        if ((mouse.getY() >= 1)
            && (mouse.getY() <= otherScreen.getHeight())
            && (mouse.getX() >= 1)
            && (mouse.getX() <= otherScreen.getWidth())
        ) {
            return true;
        }
        return false;
    }

    /**
     * Handle mouse button presses.
     *
     * @param mouse mouse button event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        if (mouseOnOtherScreen(mouse)) {
            TMouseEvent event = mouse.dup();
            event.setX(mouse.getX() - 1);
            event.setY(mouse.getY() - 1);
            event.setAbsoluteX(event.getX());
            event.setAbsoluteY(event.getY());
            synchronized (eventQueue) {
                eventQueue.add(event);
            }
            synchronized (listener) {
                listener.notifyAll();
            }
        }
        super.onMouseDown(mouse);
    }

    /**
     * Handle mouse button releases.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        if (mouseOnOtherScreen(mouse)) {
            TMouseEvent event = mouse.dup();
            event.setX(mouse.getX() - 1);
            event.setY(mouse.getY() - 1);
            event.setAbsoluteX(event.getX());
            event.setAbsoluteY(event.getY());
            synchronized (eventQueue) {
                eventQueue.add(event);
            }
            synchronized (listener) {
                listener.notifyAll();
            }
        }
        super.onMouseUp(mouse);
    }

    /**
     * Handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        if (mouseOnOtherScreen(mouse)) {
            TMouseEvent event = mouse.dup();
            event.setX(mouse.getX() - 1);
            event.setY(mouse.getY() - 1);
            event.setAbsoluteX(event.getX());
            event.setAbsoluteY(event.getY());
            synchronized (eventQueue) {
                eventQueue.add(event);
            }
            synchronized (listener) {
                listener.notifyAll();
            }
        }
        super.onMouseMotion(mouse);
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        TKeypressEvent event = keypress.dup();
        synchronized (eventQueue) {
            eventQueue.add(event);
        }
        synchronized (listener) {
            listener.notifyAll();
        }
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the foreground colors grid.
     */
    @Override
    public void draw() {

        // Sync on other screen, so that we do not draw in the middle of
        // their screen update.
        synchronized (drawLock) {
            // Draw the box
            super.draw();

            // Draw every cell of the other screen
            for (int y = 0; y < otherScreen.getHeight(); y++) {
                for (int x = 0; x < otherScreen.getWidth(); x++) {
                    putCharXY(x + 1, y + 1, otherScreen.getCharXY(x, y));
                }
            }

            // If their cursor is visible, draw that here too.
            if (otherScreen.isCursorVisible()) {
                setCursorX(otherScreen.getCursorX() + 1);
                setCursorY(otherScreen.getCursorY() + 1);
                setCursorVisible(true);
            } else {
                setCursorVisible(false);
            }
        }

        // Check if the other application has died.  If so, unset hidden
        // mouse.
        if (otherApplication != null) {
            if (otherApplication.isRunning() == false) {
                setHiddenMouse(false);
            }
        }

    }

    /**
     * Subclasses should override this method to cleanup resources.  This is
     * called by application.closeWindow().
     */
    @Override
    public void onClose() {
        synchronized (eventQueue) {
            eventQueue.add(new TCommandEvent(cmBackendDisconnect));
        }
    }

    // ------------------------------------------------------------------------
    // Backend ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Getter for sessionInfo.
     *
     * @return the SessionInfo
     */
    public final SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    /**
     * Subclasses must provide an implementation that syncs the logical
     * screen to the physical device.
     */
    public void flushScreen() {
        getApplication().doRepaint();
    }

    /**
     * Check if there are events in the queue.
     *
     * @return if true, getEvents() has something to return to the application
     */
    public boolean hasEvents() {
        synchronized (eventQueue) {
            return (eventQueue.size() > 0);
        }
    }

    /**
     * Subclasses must provide an implementation to get keyboard, mouse, and
     * screen resize events.
     *
     * @param queue list to append new events to
     */
    public void getEvents(List<TInputEvent> queue) {
        synchronized (eventQueue) {
            if (eventQueue.size() > 0) {
                synchronized (queue) {
                    queue.addAll(eventQueue);
                }
                eventQueue.clear();
            }
        }
    }

    /**
     * Subclasses must provide an implementation that closes sockets,
     * restores console, etc.
     */
    public void shutdown() {
        // NOP
    }

    /**
     * Set listener to a different Object.
     *
     * @param listener the new listening object that run() wakes up on new
     * input
     */
    public void setListener(final Object listener) {
        this.listener = listener;
    }

    /**
     * Reload backend options from System properties.
     */
    public void reloadOptions() {
        // NOP
    }

    // ------------------------------------------------------------------------
    // TWindowBackend ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the object to sync to in draw().
     *
     * @param drawLock the object to synchronize on
     */
    public void setDrawLock(final Object drawLock) {
        this.drawLock = drawLock;
    }

    /**
     * Getter for the other application's screen.
     *
     * @return the Screen
     */
    public Screen getOtherScreen() {
        return otherScreen;
    }

    /**
     * Set the other screen's application.
     *
     * @param application the application driving the other screen
     */
    public void setOtherApplication(final TApplication application) {
        this.otherApplication = application;
    }

}
