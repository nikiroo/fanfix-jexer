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

import java.util.ResourceBundle;

import jexer.menu.TMenu;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * TTerminalWindow exposes a ECMA-48 / ANSI X3.64 style terminal in a window.
 */
public class TTerminalWindow extends TScrollableWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(TTerminalWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The terminal.
     */
    private TTerminalWidget terminal;

    /**
     * If true, close the window when the shell exits.
     */
    private boolean closeOnExit = false;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor spawns a custom command line.
     *
     * @param application TApplication that manages this window
     * @param x column relative to parent
     * @param y row relative to parent
     * @param commandLine the command line to execute
     */
    public TTerminalWindow(final TApplication application, final int x,
        final int y, final String commandLine) {

        this(application, x, y, RESIZABLE, commandLine.split("\\s+"),
            System.getProperty("jexer.TTerminal.closeOnExit",
                "false").equals("true"));
    }

    /**
     * Public constructor spawns a custom command line.
     *
     * @param application TApplication that manages this window
     * @param x column relative to parent
     * @param y row relative to parent
     * @param commandLine the command line to execute
     * @param closeOnExit if true, close the window when the command exits
     */
    public TTerminalWindow(final TApplication application, final int x,
        final int y, final String commandLine, final boolean closeOnExit) {

        this(application, x, y, RESIZABLE, commandLine.split("\\s+"),
            closeOnExit);
    }

    /**
     * Public constructor spawns a custom command line.
     *
     * @param application TApplication that manages this window
     * @param x column relative to parent
     * @param y row relative to parent
     * @param flags mask of CENTERED, MODAL, or RESIZABLE
     * @param command the command line to execute
     */
    public TTerminalWindow(final TApplication application, final int x,
        final int y, final int flags, final String [] command) {

        this(application, x, y, flags, command,
            System.getProperty("jexer.TTerminal.closeOnExit",
                "false").equals("true"));
    }

    /**
     * Public constructor spawns a custom command line.
     *
     * @param application TApplication that manages this window
     * @param x column relative to parent
     * @param y row relative to parent
     * @param flags mask of CENTERED, MODAL, or RESIZABLE
     * @param command the command line to execute
     * @param closeOnExit if true, close the window when the command exits
     */
    public TTerminalWindow(final TApplication application, final int x,
        final int y, final int flags, final String [] command,
        final boolean closeOnExit) {

        super(application, i18n.getString("windowTitle"), x, y,
            80 + 2, 24 + 2, flags);

        // Require at least one line for the display.
        setMinimumWindowHeight(3);

        this.closeOnExit = closeOnExit;
        vScroller = new TVScroller(this, getWidth() - 2, 0, getHeight() - 2);

        // Claim the keystrokes the emulator will need.
        addShortcutKeys();

        // Add shortcut text
        TStatusBar statusBar = newStatusBar(i18n.getString("statusBarRunning"));
        statusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("statusBarHelp"));
        statusBar.addShortcutKeypress(kbF10, cmMenu,
            i18n.getString("statusBarMenu"));

        // Spin it up
        terminal = new TTerminalWidget(this, 0, 0, command, new TAction() {
            public void DO() {
                onShellExit();
            }
        });
    }

    /**
     * Public constructor spawns a shell.
     *
     * @param application TApplication that manages this window
     * @param x column relative to parent
     * @param y row relative to parent
     * @param flags mask of CENTERED, MODAL, or RESIZABLE
     */
    public TTerminalWindow(final TApplication application, final int x,
        final int y, final int flags) {

        this(application, x, y, flags,
            System.getProperty("jexer.TTerminal.closeOnExit",
                "false").equals("true"));

    }

    /**
     * Public constructor spawns a shell.
     *
     * @param application TApplication that manages this window
     * @param x column relative to parent
     * @param y row relative to parent
     * @param flags mask of CENTERED, MODAL, or RESIZABLE
     * @param closeOnExit if true, close the window when the shell exits
     */
    public TTerminalWindow(final TApplication application, final int x,
        final int y, final int flags, final boolean closeOnExit) {

        super(application, i18n.getString("windowTitle"), x, y,
            80 + 2, 24 + 2, flags);

        // Require at least one line for the display.
        setMinimumWindowHeight(3);

        this.closeOnExit = closeOnExit;
        vScroller = new TVScroller(this, getWidth() - 2, 0, getHeight() - 2);

        // Claim the keystrokes the emulator will need.
        addShortcutKeys();

        // Add shortcut text
        TStatusBar statusBar = newStatusBar(i18n.getString("statusBarRunning"));
        statusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("statusBarHelp"));
        statusBar.addShortcutKeypress(kbF10, cmMenu,
            i18n.getString("statusBarMenu"));

        // Spin it up
        terminal = new TTerminalWidget(this, 0, 0, new TAction() {
            public void DO() {
                onShellExit();
            }
        });
    }

    // ------------------------------------------------------------------------
    // TScrollableWindow ------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the display buffer.
     */
    @Override
    public void draw() {
        if (terminal != null) {
            setTitle(terminal.getTitle());
        }
        reflowData();
        super.draw();
    }

    /**
     * Handle window/screen resize events.
     *
     * @param resize resize event
     */
    @Override
    public void onResize(final TResizeEvent resize) {
        if (resize.getType() == TResizeEvent.Type.WIDGET) {
            if (terminal != null) {
                terminal.onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                        getWidth() - 2, getHeight() - 2));
            }

            // Resize the scroll bars
            reflowData();
            placeScrollbars();
        }
        return;
    }

    /**
     * Resize scrollbars for a new width/height.
     */
    @Override
    public void reflowData() {
        // Vertical scrollbar
        if (terminal != null) {
            terminal.reflowData();
            setTopValue(terminal.getTopValue());
            setBottomValue(terminal.getBottomValue());
            setVerticalBigChange(terminal.getVerticalBigChange());
            setVerticalValue(terminal.getVerticalValue());
        }
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if ((terminal != null)
            && (terminal.isReading())
            && (!inKeyboardResize)
        ) {
            terminal.onKeypress(keypress);
        } else {
            super.onKeypress(keypress);
        }
    }

    /**
     * Handle mouse press events.
     *
     * @param mouse mouse button press event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        if (inWindowMove || inWindowResize) {
            // TWindow needs to deal with this.
            super.onMouseDown(mouse);
            return;
        }

        super.onMouseDown(mouse);
    }

    /**
     * Handle mouse release events.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        if (inWindowMove || inWindowResize) {
            // TWindow needs to deal with this.
            super.onMouseUp(mouse);
            return;
        }

        super.onMouseUp(mouse);

        if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
            // Clicked on vertical scrollbar
            if (terminal != null) {
                terminal.setVerticalValue(getVerticalValue());
            }
        }
    }

    /**
     * Handle mouse motion events.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        if (inWindowMove || inWindowResize) {
            // TWindow needs to deal with this.
            super.onMouseMotion(mouse);
            return;
        }

        super.onMouseMotion(mouse);

        if (mouse.isMouse1() && mouseOnVerticalScroller(mouse)) {
            // Clicked/dragged on vertical scrollbar
            if (terminal != null) {
                terminal.setVerticalValue(getVerticalValue());
            }
        }
    }

    /**
     * Get this window's help topic to load.
     *
     * @return the topic name
     */
    @Override
    public String getHelpTopic() {
        return "Terminal Window";
    }

    // ------------------------------------------------------------------------
    // TTerminalWindow --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns true if this window does not want the application-wide mouse
     * cursor drawn over it.
     *
     * @return true if this window does not want the application-wide mouse
     * cursor drawn over it
     */
    @Override
    public boolean hasHiddenMouse() {
        if (terminal != null) {
            return terminal.hasHiddenMouse();
        }
        return false;
    }

    /**
     * Claim the keystrokes the emulator will need.
     */
    private void addShortcutKeys() {
        addShortcutKeypress(kbCtrlA);
        addShortcutKeypress(kbCtrlB);
        addShortcutKeypress(kbCtrlC);
        addShortcutKeypress(kbCtrlD);
        addShortcutKeypress(kbCtrlE);
        addShortcutKeypress(kbCtrlF);
        addShortcutKeypress(kbCtrlG);
        addShortcutKeypress(kbCtrlH);
        addShortcutKeypress(kbCtrlU);
        addShortcutKeypress(kbCtrlJ);
        addShortcutKeypress(kbCtrlK);
        addShortcutKeypress(kbCtrlL);
        addShortcutKeypress(kbCtrlM);
        addShortcutKeypress(kbCtrlN);
        addShortcutKeypress(kbCtrlO);
        addShortcutKeypress(kbCtrlP);
        addShortcutKeypress(kbCtrlQ);
        addShortcutKeypress(kbCtrlR);
        addShortcutKeypress(kbCtrlS);
        addShortcutKeypress(kbCtrlT);
        addShortcutKeypress(kbCtrlU);
        addShortcutKeypress(kbCtrlV);
        addShortcutKeypress(kbCtrlW);
        addShortcutKeypress(kbCtrlX);
        addShortcutKeypress(kbCtrlY);
        addShortcutKeypress(kbCtrlZ);
        addShortcutKeypress(kbF1);
        addShortcutKeypress(kbF2);
        addShortcutKeypress(kbF3);
        addShortcutKeypress(kbF4);
        addShortcutKeypress(kbF5);
        addShortcutKeypress(kbF6);
        addShortcutKeypress(kbF7);
        addShortcutKeypress(kbF8);
        addShortcutKeypress(kbF9);
        addShortcutKeypress(kbF10);
        addShortcutKeypress(kbF11);
        addShortcutKeypress(kbF12);
        addShortcutKeypress(kbAltA);
        addShortcutKeypress(kbAltB);
        addShortcutKeypress(kbAltC);
        addShortcutKeypress(kbAltD);
        addShortcutKeypress(kbAltE);
        addShortcutKeypress(kbAltF);
        addShortcutKeypress(kbAltG);
        addShortcutKeypress(kbAltH);
        addShortcutKeypress(kbAltU);
        addShortcutKeypress(kbAltJ);
        addShortcutKeypress(kbAltK);
        addShortcutKeypress(kbAltL);
        addShortcutKeypress(kbAltM);
        addShortcutKeypress(kbAltN);
        addShortcutKeypress(kbAltO);
        addShortcutKeypress(kbAltP);
        addShortcutKeypress(kbAltQ);
        addShortcutKeypress(kbAltR);
        addShortcutKeypress(kbAltS);
        addShortcutKeypress(kbAltT);
        addShortcutKeypress(kbAltU);
        addShortcutKeypress(kbAltV);
        addShortcutKeypress(kbAltW);
        addShortcutKeypress(kbAltX);
        addShortcutKeypress(kbAltY);
        addShortcutKeypress(kbAltZ);
    }

    /**
     * Hook for subclasses to be notified of the shell termination.
     */
    public void onShellExit() {
        if (closeOnExit) {
            close();
        }
        clearShortcutKeypresses();
        getApplication().postEvent(new TMenuEvent(TMenu.MID_REPAINT));
    }

    /**
     * Wait for a period of time to get output from the launched process.
     *
     * @param millis millis to wait for, or 0 to wait forever
     * @return true if the launched process has emitted something
     */
    public boolean waitForOutput(final int millis) {
        if (terminal == null) {
            return false;
        }
        return terminal.waitForOutput(millis);
    }

    /**
     * Get the exit value for the emulator.
     *
     * @return exit value
     */
    public int getExitValue() {
        if (terminal == null) {
            return -1;
        }
        return terminal.getExitValue();
    }

}
