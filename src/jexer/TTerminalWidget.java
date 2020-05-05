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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import jexer.backend.ECMA48Terminal;
import jexer.backend.GlyphMaker;
import jexer.backend.SwingTerminal;
import jexer.bits.Cell;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMenuEvent;
import jexer.event.TMouseEvent;
import jexer.event.TResizeEvent;
import jexer.menu.TMenu;
import jexer.tterminal.DisplayLine;
import jexer.tterminal.DisplayListener;
import jexer.tterminal.ECMA48;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * TTerminalWidget exposes a ECMA-48 / ANSI X3.64 style terminal in a widget.
 */
public class TTerminalWidget extends TScrollableWidget
                             implements DisplayListener, EditMenuUser {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(TTerminalWidget.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The emulator.
     */
    private ECMA48 emulator;

    /**
     * The Process created by the shell spawning constructor.
     */
    private Process shell;

    /**
     * If true, something called 'ptypipe' is on the PATH and executable.
     */
    private static boolean ptypipeOnPath = false;

    /**
     * If true, we are using the ptypipe utility to support dynamic window
     * resizing.  ptypipe is available at
     * https://gitlab.com/klamonte/ptypipe .
     */
    private boolean ptypipe = false;

    /**
     * Double-height font.
     */
    private GlyphMaker doubleFont;

    /**
     * Last text width value.
     */
    private int lastTextWidth = -1;

    /**
     * Last text height value.
     */
    private int lastTextHeight = -1;

    /**
     * The blink state, used only by ECMA48 backend and when double-width
     * chars must be drawn.
     */
    private boolean blinkState = true;

    /**
     * Timer flag, used only by ECMA48 backend and when double-width chars
     * must be drawn.
     */
    private boolean haveTimer = false;

    /**
     * The last seen visible display.
     */
    private List<DisplayLine> display;

    /**
     * If true, the display has changed and needs updating.
     */
    private volatile boolean dirty = true;

    /**
     * Time that the display was last updated.
     */
    private long lastUpdateTime = 0;

    /**
     * If true, hide the mouse after typing a keystroke.
     */
    private boolean hideMouseWhenTyping = true;

    /**
     * If true, the mouse should not be displayed because a keystroke was
     * typed.
     */
    private boolean typingHidMouse = false;

    /**
     * The return value from the emulator.
     */
    private int exitValue = -1;

    /**
     * Title to expose to a window.
     */
    private String title = "";

    /**
     * Action to perform when the terminal exits.
     */
    private TAction closeAction = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Static constructor.
     */
    static {
        checkForPtypipe();
    }

    /**
     * Public constructor spawns a custom command line.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param commandLine the command line to execute
     */
    public TTerminalWidget(final TWidget parent, final int x, final int y,
        final String commandLine) {

        this(parent, x, y, commandLine.split("\\s+"));
    }

    /**
     * Public constructor spawns a custom command line.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param command the command line to execute
     */
    public TTerminalWidget(final TWidget parent, final int x, final int y,
        final String [] command) {

        this(parent, x, y, command, null);
    }

    /**
     * Public constructor spawns a custom command line.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param command the command line to execute
     * @param closeAction action to perform when the shell exits
     */
    public TTerminalWidget(final TWidget parent, final int x, final int y,
        final String [] command, final TAction closeAction) {

        this(parent, x, y, 80, 24, command, closeAction);
    }

    /**
     * Public constructor spawns a custom command line.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of widget
     * @param height height of widget
     * @param command the command line to execute
     * @param closeAction action to perform when the shell exits
     */
    public TTerminalWidget(final TWidget parent, final int x, final int y,
        final int width, final int height, final String [] command,
        final TAction closeAction) {

        super(parent, x, y, width, height);

        this.closeAction = closeAction;

        String [] fullCommand;

        // Spawn a shell and pass its I/O to the other constructor.
        if ((System.getProperty("jexer.TTerminal.ptypipe") != null)
            && (System.getProperty("jexer.TTerminal.ptypipe").
                equals("true"))
        ) {
            ptypipe = true;
            fullCommand = new String[command.length + 1];
            fullCommand[0] = "ptypipe";
            System.arraycopy(command, 0, fullCommand, 1, command.length);
        } else if (System.getProperty("jexer.TTerminal.ptypipe",
                "auto").equals("auto")
            && (ptypipeOnPath == true)
        ) {
            ptypipe = true;
            fullCommand = new String[command.length + 1];
            fullCommand[0] = "ptypipe";
            System.arraycopy(command, 0, fullCommand, 1, command.length);
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            fullCommand = new String[3];
            fullCommand[0] = "cmd";
            fullCommand[1] = "/c";
            fullCommand[2] = stringArrayToString(command);
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            fullCommand = new String[6];
            fullCommand[0] = "script";
            fullCommand[1] = "-q";
            fullCommand[2] = "-F";
            fullCommand[3] = "/dev/null";
            fullCommand[4] = "-c";
            fullCommand[5] = stringArrayToString(command);
        } else {
            // Default: behave like Linux
            if (System.getProperty("jexer.TTerminal.setsid",
                    "true").equals("false")
            ) {
                fullCommand = new String[5];
                fullCommand[0] = "script";
                fullCommand[1] = "-fqe";
                fullCommand[2] = "/dev/null";
                fullCommand[3] = "-c";
                fullCommand[4] = stringArrayToString(command);
            } else {
                fullCommand = new String[6];
                fullCommand[0] = "setsid";
                fullCommand[1] = "script";
                fullCommand[2] = "-fqe";
                fullCommand[3] = "/dev/null";
                fullCommand[4] = "-c";
                fullCommand[5] = stringArrayToString(command);
            }
        }
        spawnShell(fullCommand);
    }

    /**
     * Public constructor spawns a shell.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     */
    public TTerminalWidget(final TWidget parent, final int x, final int y) {
        this(parent, x, y, (TAction) null);
    }

    /**
     * Public constructor spawns a shell.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param closeAction action to perform when the shell exits
     */
    public TTerminalWidget(final TWidget parent, final int x, final int y,
        final TAction closeAction) {

        this(parent, x, y, 80, 24, closeAction);
    }

    /**
     * Public constructor spawns a shell.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of widget
     * @param height height of widget
     * @param closeAction action to perform when the shell exits
     */
    public TTerminalWidget(final TWidget parent, final int x, final int y,
        final int width, final int height, final TAction closeAction) {

        super(parent, x, y, width, height);

        this.closeAction = closeAction;

        if (System.getProperty("jexer.TTerminal.shell") != null) {
            String shell = System.getProperty("jexer.TTerminal.shell");
            if (shell.trim().startsWith("ptypipe")) {
                ptypipe = true;
            }
            spawnShell(shell.split("\\s+"));
            return;
        }

        String cmdShellWindows = "cmd.exe";

        // You cannot run a login shell in a bare Process interactively, due
        // to libc's behavior of buffering when stdin/stdout aren't a tty.
        // Use 'script' instead to run a shell in a pty.  And because BSD and
        // GNU differ on the '-f' vs '-F' flags, we need two different
        // commands.  Lovely.
        String cmdShellGNU = "script -fqe /dev/null";
        String cmdShellGNUSetsid = "setsid script -fqe /dev/null";
        String cmdShellBSD = "script -q -F /dev/null";

        // ptypipe is another solution that permits dynamic window resizing.
        String cmdShellPtypipe = "ptypipe /bin/bash --login";

        // Spawn a shell and pass its I/O to the other constructor.
        if ((System.getProperty("jexer.TTerminal.ptypipe") != null)
            && (System.getProperty("jexer.TTerminal.ptypipe").
                equals("true"))
        ) {
            ptypipe = true;
            spawnShell(cmdShellPtypipe.split("\\s+"));
        } else if (System.getProperty("jexer.TTerminal.ptypipe",
                "auto").equals("auto")
            && (ptypipeOnPath == true)
        ) {
            ptypipe = true;
            spawnShell(cmdShellPtypipe.split("\\s+"));
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            spawnShell(cmdShellWindows.split("\\s+"));
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            spawnShell(cmdShellBSD.split("\\s+"));
        } else if (System.getProperty("os.name").startsWith("Linux")) {
            if (System.getProperty("jexer.TTerminal.setsid",
                    "true").equals("false")
            ) {
                spawnShell(cmdShellGNU.split("\\s+"));
            } else {
                spawnShell(cmdShellGNUSetsid.split("\\s+"));
            }
        } else {
            // When all else fails, assume GNU.
            spawnShell(cmdShellGNU.split("\\s+"));
        }
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle window/screen resize events.
     *
     * @param resize resize event
     */
    @Override
    public void onResize(final TResizeEvent resize) {
        // Let TWidget set my size.
        super.onResize(resize);

        if (emulator == null) {
            return;
        }

        // Synchronize against the emulator so we don't stomp on its reader
        // thread.
        synchronized (emulator) {

            if (resize.getType() == TResizeEvent.Type.WIDGET) {
                // Resize the scroll bars
                reflowData();
                placeScrollbars();

                // Get out of scrollback
                setVerticalValue(0);

                if (ptypipe) {
                    emulator.setWidth(getWidth());
                    emulator.setHeight(getHeight());

                    emulator.writeRemote("\033[8;" + getHeight() + ";" +
                        getWidth() + "t");
                }

                // Pass the correct text cell width/height to the emulator
                if (getScreen() != null) {
                    emulator.setTextWidth(getScreen().getTextWidth());
                    emulator.setTextHeight(getScreen().getTextHeight());
                }
            }
            return;

        } // synchronized (emulator)
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        if (hideMouseWhenTyping) {
            typingHidMouse = true;
        }

        // Scrollback up/down
        if (keypress.equals(kbShiftPgUp)
            || keypress.equals(kbCtrlPgUp)
            || keypress.equals(kbAltPgUp)
        ) {
            bigVerticalDecrement();
            dirty = true;
            return;
        }
        if (keypress.equals(kbShiftPgDn)
            || keypress.equals(kbCtrlPgDn)
            || keypress.equals(kbAltPgDn)
        ) {
            bigVerticalIncrement();
            dirty = true;
            return;
        }

        if ((emulator != null) && (emulator.isReading())) {
            // Get out of scrollback
            setVerticalValue(0);
            emulator.addUserEvent(keypress);

            // UGLY HACK TIME!  cmd.exe needs CRLF, not just CR, so if
            // this is kBEnter then also send kbCtrlJ.
            if (keypress.equals(kbEnter)) {
                if (System.getProperty("os.name").startsWith("Windows")
                    && (System.getProperty("jexer.TTerminal.cmdHack",
                            "true").equals("true"))
                ) {
                    emulator.addUserEvent(new TKeypressEvent(kbCtrlJ));
                }
            }

            readEmulatorState();
            return;
        }

        // Process is closed, honor "normal" TUI keystrokes
        super.onKeypress(keypress);
    }

    /**
     * Handle mouse press events.
     *
     * @param mouse mouse button press event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        if (hideMouseWhenTyping) {
            typingHidMouse = false;
        }

        if (emulator != null) {
            // If the emulator is tracking mouse buttons, it needs to see
            // wheel events.
            if (emulator.getMouseProtocol() == ECMA48.MouseProtocol.OFF) {
                if (mouse.isMouseWheelUp()) {
                    verticalDecrement();
                    dirty = true;
                    return;
                }
                if (mouse.isMouseWheelDown()) {
                    verticalIncrement();
                    dirty = true;
                    return;
                }
            }
            if (mouseOnEmulator(mouse)) {
                emulator.addUserEvent(mouse);
                readEmulatorState();
                return;
            }
        }

        // Emulator didn't consume it, pass it on
        super.onMouseDown(mouse);
    }

    /**
     * Handle mouse release events.
     *
     * @param mouse mouse button release event
     */
    @Override
    public void onMouseUp(final TMouseEvent mouse) {
        if (hideMouseWhenTyping) {
            typingHidMouse = false;
        }

        if ((emulator != null) && (mouseOnEmulator(mouse))) {
            emulator.addUserEvent(mouse);
            readEmulatorState();
            return;
        }

        // Emulator didn't consume it, pass it on
        super.onMouseUp(mouse);
    }

    /**
     * Handle mouse motion events.
     *
     * @param mouse mouse motion event
     */
    @Override
    public void onMouseMotion(final TMouseEvent mouse) {
        if (hideMouseWhenTyping) {
            typingHidMouse = false;
        }

        if ((emulator != null) && (mouseOnEmulator(mouse))) {
            emulator.addUserEvent(mouse);
            readEmulatorState();
            return;
        }

        // Emulator didn't consume it, pass it on
        super.onMouseMotion(mouse);
    }

    /**
     * Handle posted command events.
     *
     * @param command command event
     */
    @Override
    public void onCommand(final TCommandEvent command) {
        if (emulator == null) {
            return;
        }

        if (command.equals(cmPaste)) {
            // Paste text from clipboard.
            String text = getClipboard().pasteText();
            if (text != null) {
                for (int i = 0; i < text.length(); ) {
                    int ch = text.codePointAt(i);
                    emulator.addUserEvent(new TKeypressEvent(false, 0, ch,
                            false, false, false));
                    i += Character.charCount(ch);
                }
            }
            return;
        }
    }

    // ------------------------------------------------------------------------
    // TScrollableWidget ------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the display buffer.
     */
    @Override
    public void draw() {
        if (emulator == null) {
            return;
        }

        int width = getDisplayWidth();

        boolean syncEmulator = false;
        if (System.currentTimeMillis() - lastUpdateTime >= 50) {
            // Too much time has passed, draw it all.
            syncEmulator = true;
        } else if (emulator.isReading() && (dirty == false)) {
            // Wait until the emulator has brought more data in.
            syncEmulator = false;
        } else if (!emulator.isReading() && (dirty == true)) {
            // The emulator won't receive more data, update the display.
            syncEmulator = true;
        }

        if ((syncEmulator == true)
            || (display == null)
        ) {
            // We want to minimize the amount of time we have the emulator
            // locked.  Grab a copy of its display.
            synchronized (emulator) {
                // Update the scroll bars
                reflowData();

                if (!isDrawable()) {
                    // We lost the connection, onShellExit() called an action
                    // that ultimately removed this widget from the UI
                    // hierarchy, so no one cares if we update the display.
                    // Bail out.
                    return;
                }

                if ((display == null) || emulator.isReading()) {
                    display = emulator.getVisibleDisplay(getHeight(),
                        -getVerticalValue());
                    assert (display.size() == getHeight());
                }
                width = emulator.getWidth();
            }
            dirty = false;
        }

        // Now draw the emulator screen
        int row = 0;
        for (DisplayLine line: display) {
            int widthMax = width;
            if (line.isDoubleWidth()) {
                widthMax /= 2;
            }
            if (widthMax > getWidth()) {
                widthMax = getWidth();
            }
            for (int i = 0; i < widthMax; i++) {
                Cell ch = line.charAt(i);

                if (ch.isImage()) {
                    putCharXY(i, row, ch);
                    continue;
                }

                Cell newCell = new Cell(ch);
                boolean reverse = line.isReverseColor() ^ ch.isReverse();
                newCell.setReverse(false);
                if (reverse) {
                    if (ch.getForeColorRGB() < 0) {
                        newCell.setBackColor(ch.getForeColor());
                        newCell.setBackColorRGB(-1);
                    } else {
                        newCell.setBackColorRGB(ch.getForeColorRGB());
                    }
                    if (ch.getBackColorRGB() < 0) {
                        newCell.setForeColor(ch.getBackColor());
                        newCell.setForeColorRGB(-1);
                    } else {
                        newCell.setForeColorRGB(ch.getBackColorRGB());
                    }
                }
                if (line.isDoubleWidth()) {
                    putDoubleWidthCharXY(line, (i * 2), row, newCell);
                } else {
                    putCharXY(i, row, newCell);
                }
            }
            row++;
        }
    }

    /**
     * Set current value of the vertical scroll.
     *
     * @param value the new scroll value
     */
    @Override
    public void setVerticalValue(final int value) {
        super.setVerticalValue(value);
        dirty = true;
    }

    /**
     * Perform a small step change up.
     */
    @Override
    public void verticalDecrement() {
        super.verticalDecrement();
        dirty = true;
    }

    /**
     * Perform a small step change down.
     */
    @Override
    public void verticalIncrement() {
        super.verticalIncrement();
        dirty = true;
    }

    /**
     * Perform a big step change up.
     */
    public void bigVerticalDecrement() {
        super.bigVerticalDecrement();
        dirty = true;
    }

    /**
     * Perform a big step change down.
     */
    public void bigVerticalIncrement() {
        super.bigVerticalIncrement();
        dirty = true;
    }

    /**
     * Go to the top edge of the vertical scroller.
     */
    public void toTop() {
        super.toTop();
        dirty = true;
    }

    /**
     * Go to the bottom edge of the vertical scroller.
     */
    public void toBottom() {
        super.toBottom();
        dirty = true;
    }

    /**
     * Handle widget close.
     */
    @Override
    public void close() {
        if (emulator != null) {
            emulator.close();
        }
        if (shell != null) {
            terminateShellChildProcess();
            shell.destroy();
            shell = null;
        }
    }

    /**
     * Resize scrollbars for a new width/height.
     */
    @Override
    public void reflowData() {
        if (emulator == null) {
            return;
        }

        // Synchronize against the emulator so we don't stomp on its reader
        // thread.
        synchronized (emulator) {

            // Pull cursor information
            readEmulatorState();

            // Vertical scrollbar
            setTopValue(getHeight()
                - (emulator.getScrollbackBuffer().size()
                    + emulator.getDisplayBuffer().size()));
            setVerticalBigChange(getHeight());

        } // synchronized (emulator)
    }

    // ------------------------------------------------------------------------
    // TTerminalWidget --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Check for 'ptypipe' on the path.  If available, set ptypipeOnPath.
     */
    private static void checkForPtypipe() {
        String systemPath = System.getenv("PATH");
        if (systemPath == null) {
            return;
        }

        String [] paths = systemPath.split(File.pathSeparator);
        if (paths == null) {
            return;
        }
        if (paths.length == 0) {
            return;
        }
        for (int i = 0; i < paths.length; i++) {
            File path = new File(paths[i]);
            if (path.exists() && path.isDirectory()) {
                File [] files = path.listFiles();
                if (files == null) {
                    continue;
                }
                if (files.length == 0) {
                    continue;
                }
                for (int j = 0; j < files.length; j++) {
                    File file = files[j];
                    if (file.canExecute() && file.getName().equals("ptypipe")) {
                        ptypipeOnPath = true;
                        return;
                    }
                }
            }
        }
    }

    /**
     * Get the desired window title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns true if this widget does not want the application-wide mouse
     * cursor drawn over it.
     *
     * @return true if this widget does not want the application-wide mouse
     * cursor drawn over it
     */
    public boolean hasHiddenMouse() {
        if (emulator == null) {
            return false;
        }
        return (emulator.hasHiddenMousePointer() || typingHidMouse);
    }

    /**
     * See if the terminal is still running.
     *
     * @return if true, we are still connected to / reading from the remote
     * side
     */
    public boolean isReading() {
        if (emulator == null) {
            return false;
        }
        return emulator.isReading();
    }

    /**
     * Convert a string array to a whitespace-separated string.
     *
     * @param array the string array
     * @return a single string
     */
    private String stringArrayToString(final String [] array) {
        StringBuilder sb = new StringBuilder(array[0].length());
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Spawn the shell.
     *
     * @param command the command line to execute
     */
    private void spawnShell(final String [] command) {

        /*
        System.err.printf("spawnShell(): '%s'\n",
            stringArrayToString(command));
        */

        // We will have vScroller for its data fields and mouse event
        // handling, but do not want to draw it.
        vScroller = new TVScroller(null, getWidth(), 0, getHeight());
        vScroller.setVisible(false);
        setBottomValue(0);

        title = i18n.getString("windowTitle");

        // Assume XTERM
        ECMA48.DeviceType deviceType = ECMA48.DeviceType.XTERM;

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Map<String, String> env = pb.environment();
            env.put("TERM", ECMA48.deviceTypeTerm(deviceType));
            env.put("LANG", ECMA48.deviceTypeLang(deviceType, "en"));
            env.put("COLUMNS", "80");
            env.put("LINES", "24");
            pb.redirectErrorStream(true);
            shell = pb.start();
            emulator = new ECMA48(deviceType, shell.getInputStream(),
                shell.getOutputStream(), this);
        } catch (IOException e) {
            messageBox(i18n.getString("errorLaunchingShellTitle"),
                MessageFormat.format(i18n.getString("errorLaunchingShellText"),
                    e.getMessage()));
        }

        // Setup the scroll bars
        onResize(new TResizeEvent(TResizeEvent.Type.WIDGET, getWidth(),
                getHeight()));

        // Hide mouse when typing option
        if (System.getProperty("jexer.TTerminal.hideMouseWhenTyping",
                "true").equals("false")) {

            hideMouseWhenTyping = false;
        }
    }

    /**
     * Terminate the child of the 'script' process used on POSIX.  This may
     * or may not work.
     */
    private void terminateShellChildProcess() {
        int pid = -1;
        if (shell.getClass().getName().equals("java.lang.UNIXProcess")) {
            /* get the PID on unix/linux systems */
            try {
                Field field = shell.getClass().getDeclaredField("pid");
                field.setAccessible(true);
                pid = field.getInt(shell);
            } catch (Throwable e) {
                // SQUASH, this didn't work.  Just bail out quietly.
                return;
            }
        }
        if (pid != -1) {
            // shell.destroy() works successfully at killing this side of
            // 'script'.  But we need to make sure the other side (child
            // process) is also killed.
            String [] cmdKillIt = {
                "pkill", "-P", Integer.toString(pid)
            };
            try {
                Runtime.getRuntime().exec(cmdKillIt);
            } catch (Throwable e) {
                // SQUASH, this didn't work.  Just bail out quietly.
                return;
            }
        }
    }

    /**
     * Hook for subclasses to be notified of the shell termination.
     */
    public void onShellExit() {
        TApplication app = getApplication();
        if (app != null) {
            if (closeAction != null) {
                // We have to put this action inside invokeLater() because it
                // could be executed during draw() when syncing with ECMA48.
                app.invokeLater(new Runnable() {
                    public void run() {
                        closeAction.DO(TTerminalWidget.this);
                    }
                });
            }
            app.doRepaint();
        }
    }

    /**
     * Copy out variables from the emulator that TTerminal has to expose on
     * screen.
     */
    private void readEmulatorState() {
        if (emulator == null) {
            return;
        }

        // Synchronize against the emulator so we don't stomp on its reader
        // thread.
        synchronized (emulator) {

            setCursorX(emulator.getCursorX());
            setCursorY(emulator.getCursorY()
                + (getHeight() - emulator.getHeight())
                - getVerticalValue());
            setCursorVisible(emulator.isCursorVisible());
            if (getCursorX() > getWidth()) {
                setCursorVisible(false);
            }
            if ((getCursorY() >= getHeight()) || (getCursorY() < 0)) {
                setCursorVisible(false);
            }
            if (emulator.getScreenTitle().length() > 0) {
                // Only update the title if the shell is still alive
                if (shell != null) {
                    title = emulator.getScreenTitle();
                }
            }

            // Check to see if the shell has died.
            if (!emulator.isReading() && (shell != null)) {
                try {
                    int rc = shell.exitValue();
                    // The emulator exited on its own, all is fine
                    title = MessageFormat.format(i18n.
                        getString("windowTitleCompleted"), title, rc);
                    exitValue = rc;
                    shell = null;
                    emulator.close();
                    onShellExit();
                } catch (IllegalThreadStateException e) {
                    // The emulator thread has exited, but the shell Process
                    // hasn't figured that out yet.  Do nothing, we will see
                    // this in a future tick.
                }
            } else if (emulator.isReading() && (shell != null)) {
                // The shell might be dead, let's check
                try {
                    int rc = shell.exitValue();
                    // If we got here, the shell died.
                    title = MessageFormat.format(i18n.
                        getString("windowTitleCompleted"), title, rc);
                    exitValue = rc;
                    shell = null;
                    emulator.close();
                    onShellExit();
                } catch (IllegalThreadStateException e) {
                    // The shell is still running, do nothing.
                }
            }

        } // synchronized (emulator)
    }

    /**
     * Wait for a period of time to get output from the launched process.
     *
     * @param millis millis to wait for, or 0 to wait forever
     * @return true if the launched process has emitted something
     */
    public boolean waitForOutput(final int millis) {
        if (emulator == null) {
            return false;
        }
        return emulator.waitForOutput(millis);
    }

    /**
     * Check if a mouse press/release/motion event coordinate is over the
     * emulator.
     *
     * @param mouse a mouse-based event
     * @return whether or not the mouse is on the emulator
     */
    private boolean mouseOnEmulator(final TMouseEvent mouse) {
        if (emulator == null) {
            return false;
        }

        if (!emulator.isReading()) {
            return false;
        }

        if ((mouse.getX() >= 0)
            && (mouse.getX() < getWidth() - 1)
            && (mouse.getY() >= 0)
            && (mouse.getY() < getHeight())
        ) {
            return true;
        }
        return false;
    }

    /**
     * Draw glyphs for a double-width or double-height VT100 cell to two
     * screen cells.
     *
     * @param line the line this VT100 cell is in
     * @param x the X position to draw the left half to
     * @param y the Y position to draw to
     * @param cell the cell to draw
     */
    private void putDoubleWidthCharXY(final DisplayLine line, final int x,
        final int y, final Cell cell) {

        int textWidth = getScreen().getTextWidth();
        int textHeight = getScreen().getTextHeight();
        boolean cursorBlinkVisible = true;

        if (getScreen() instanceof SwingTerminal) {
            SwingTerminal terminal = (SwingTerminal) getScreen();
            cursorBlinkVisible = terminal.getCursorBlinkVisible();
        } else if (getScreen() instanceof ECMA48Terminal) {
            ECMA48Terminal terminal = (ECMA48Terminal) getScreen();

            if (!terminal.hasSixel()) {
                // The backend does not have sixel support, draw this as text
                // and bail out.
                putCharXY(x, y, cell);
                putCharXY(x + 1, y, ' ', cell);
                return;
            }
            cursorBlinkVisible = blinkState;
        } else {
            // We don't know how to dray glyphs to this screen, draw them as
            // text and bail out.
            putCharXY(x, y, cell);
            putCharXY(x + 1, y, ' ', cell);
            return;
        }

        if ((textWidth != lastTextWidth) || (textHeight != lastTextHeight)) {
            // Screen size has changed, reset the font.
            setupFont(textHeight);
            lastTextWidth = textWidth;
            lastTextHeight = textHeight;
        }
        assert (doubleFont != null);

        BufferedImage image;
        if (line.getDoubleHeight() == 1) {
            // Double-height top half: don't draw the underline.
            Cell newCell = new Cell(cell);
            newCell.setUnderline(false);
            image = doubleFont.getImage(newCell, textWidth * 2, textHeight * 2,
                cursorBlinkVisible);
        } else {
            image = doubleFont.getImage(cell,  textWidth * 2, textHeight * 2,
                cursorBlinkVisible);
        }

        // Now that we have the double-wide glyph drawn, copy the right
        // pieces of it to the cells.
        Cell left = new Cell(cell);
        Cell right = new Cell(cell);
        right.setChar(' ');
        BufferedImage leftImage = null;
        BufferedImage rightImage = null;
        /*
        System.err.println("image " + image + " textWidth " + textWidth +
            " textHeight " + textHeight);
         */

        switch (line.getDoubleHeight()) {
        case 1:
            // Top half double height
            leftImage = image.getSubimage(0, 0, textWidth, textHeight);
            rightImage = image.getSubimage(textWidth, 0, textWidth, textHeight);
            break;
        case 2:
            // Bottom half double height
            leftImage = image.getSubimage(0, textHeight, textWidth, textHeight);
            rightImage = image.getSubimage(textWidth, textHeight,
                textWidth, textHeight);
            break;
        default:
            // Either single height double-width, or error fallback
            BufferedImage wideImage = new BufferedImage(textWidth * 2,
                textHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D grWide = wideImage.createGraphics();
            grWide.drawImage(image, 0, 0, wideImage.getWidth(),
                wideImage.getHeight(), null);
            grWide.dispose();
            leftImage = wideImage.getSubimage(0, 0, textWidth, textHeight);
            rightImage = wideImage.getSubimage(textWidth, 0, textWidth,
                textHeight);
            break;
        }
        left.setImage(leftImage);
        right.setImage(rightImage);
        // Since we have image data, ditch the character here.  Otherwise, a
        // drawBoxShadow() over the terminal window will show the characters
        // which looks wrong.
        left.setChar(' ');
        right.setChar(' ');
        putCharXY(x, y, left);
        putCharXY(x + 1, y, right);
    }

    /**
     * Set up the double-width font.
     *
     * @param fontSize the size of font to request for the single-width font.
     * The double-width font will be 2x this value.
     */
    private void setupFont(final int fontSize) {
        doubleFont = GlyphMaker.getInstance(fontSize * 2);

        // Special case: the ECMA48 backend needs to have a timer to drive
        // its blink state.
        if (getScreen() instanceof jexer.backend.ECMA48Terminal) {
            if (!haveTimer) {
                // Blink every 500 millis.
                long millis = 500;
                getApplication().addTimer(millis, true,
                    new TAction() {
                        public void DO() {
                            blinkState = !blinkState;
                            getApplication().doRepaint();
                        }
                    }
                );
                haveTimer = true;
            }
        }
    }

    // ------------------------------------------------------------------------
    // DisplayListener --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Called by emulator when fresh data has come in.
     */
    public void displayChanged() {
        if (emulator != null) {
            // Force sync here: EMCA48.run() thread might be setting
            // dirty=true while TTerminalWdiget.draw() is setting
            // dirty=false.  If these writes start interleaving, the display
            // stops getting updated.
            synchronized (emulator) {
                dirty = true;
            }
        } else {
            dirty = true;
        }
        getApplication().postEvent(new TMenuEvent(TMenu.MID_REPAINT));
    }

    /**
     * Function to call to obtain the display width.
     *
     * @return the number of columns in the display
     */
    public int getDisplayWidth() {
        if (ptypipe) {
            return getWidth();
        }
        return 80;
    }

    /**
     * Function to call to obtain the display height.
     *
     * @return the number of rows in the display
     */
    public int getDisplayHeight() {
        if (ptypipe) {
            return getHeight();
        }
        return 24;
    }

    /**
     * Get the exit value for the emulator.
     *
     * @return exit value
     */
    public int getExitValue() {
        return exitValue;
    }

    // ------------------------------------------------------------------------
    // EditMenuUser -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Check if the cut menu item should be enabled.
     *
     * @return true if the cut menu item should be enabled
     */
    public boolean isEditMenuCut() {
        return false;
    }

    /**
     * Check if the copy menu item should be enabled.
     *
     * @return true if the copy menu item should be enabled
     */
    public boolean isEditMenuCopy() {
        return false;
    }

    /**
     * Check if the paste menu item should be enabled.
     *
     * @return true if the paste menu item should be enabled
     */
    public boolean isEditMenuPaste() {
        return true;
    }

    /**
     * Check if the clear menu item should be enabled.
     *
     * @return true if the clear menu item should be enabled
     */
    public boolean isEditMenuClear() {
        return false;
    }

}
