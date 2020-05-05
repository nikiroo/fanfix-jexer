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
package jexer.demos;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

import jexer.TApplication;
import jexer.TEditColorThemeWindow;
import jexer.TEditorWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;
import jexer.menu.TMenuItem;
import jexer.menu.TSubMenu;
import jexer.backend.Backend;
import jexer.backend.SwingTerminal;

/**
 * The demo application itself.
 */
public class DemoApplication extends TApplication {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoApplication.class.getName());

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param input an InputStream connected to the remote user, or null for
     * System.in.  If System.in is used, then on non-Windows systems it will
     * be put in raw mode; shutdown() will (blindly!) put System.in in cooked
     * mode.  input is always converted to a Reader with UTF-8 encoding.
     * @param output an OutputStream connected to the remote user, or null
     * for System.out.  output is always converted to a Writer with UTF-8
     * encoding.
     * @throws UnsupportedEncodingException if an exception is thrown when
     * creating the InputStreamReader
     */
    public DemoApplication(final InputStream input,
        final OutputStream output) throws UnsupportedEncodingException {
        super(input, output);
        addAllWidgets();

        getBackend().setTitle(i18n.getString("applicationTitle"));
    }

    /**
     * Public constructor.
     *
     * @param input the InputStream underlying 'reader'.  Its available()
     * method is used to determine if reader.read() will block or not.
     * @param reader a Reader connected to the remote user.
     * @param writer a PrintWriter connected to the remote user.
     * @param setRawMode if true, set System.in into raw mode with stty.
     * This should in general not be used.  It is here solely for Demo3,
     * which uses System.in.
     * @throws IllegalArgumentException if input, reader, or writer are null.
     */
    public DemoApplication(final InputStream input, final Reader reader,
        final PrintWriter writer, final boolean setRawMode) {
        super(input, reader, writer, setRawMode);
        addAllWidgets();

        getBackend().setTitle(i18n.getString("applicationTitle"));
    }

    /**
     * Public constructor.
     *
     * @param input the InputStream underlying 'reader'.  Its available()
     * method is used to determine if reader.read() will block or not.
     * @param reader a Reader connected to the remote user.
     * @param writer a PrintWriter connected to the remote user.
     * @throws IllegalArgumentException if input, reader, or writer are null.
     */
    public DemoApplication(final InputStream input, final Reader reader,
        final PrintWriter writer) {

        this(input, reader, writer, false);
    }

    /**
     * Public constructor.
     *
     * @param backend a Backend that is already ready to go.
     */
    public DemoApplication(final Backend backend) {
        super(backend);

        addAllWidgets();
    }

    /**
     * Public constructor.
     *
     * @param backendType one of the TApplication.BackendType values
     * @throws Exception if TApplication can't instantiate the Backend.
     */
    public DemoApplication(final BackendType backendType) throws Exception {
        // For the Swing demo, use an initial size of 82x28 so that a
        // terminal window precisely fits the window.
        super(backendType, (backendType == BackendType.SWING ? 82 : -1),
            (backendType == BackendType.SWING ? 28 : -1), 20);
        addAllWidgets();
        getBackend().setTitle(i18n.getString("applicationTitle"));
    }

    // ------------------------------------------------------------------------
    // TApplication -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle menu events.
     *
     * @param menu menu event
     * @return if true, the event was processed and should not be passed onto
     * a window
     */
    @Override
    public boolean onMenu(final TMenuEvent menu) {

        if (menu.getId() == 3000) {
            // Bigger +2
            assert (getScreen() instanceof SwingTerminal);
            SwingTerminal terminal = (SwingTerminal) getScreen();
            terminal.setFontSize(terminal.getFontSize() + 2);
            return true;
        }
        if (menu.getId() == 3001) {
            // Smaller -2
            assert (getScreen() instanceof SwingTerminal);
            SwingTerminal terminal = (SwingTerminal) getScreen();
            terminal.setFontSize(terminal.getFontSize() - 2);
            return true;
        }

        if (menu.getId() == 2050) {
            new TEditColorThemeWindow(this);
            return true;
        }

        if (menu.getId() == TMenu.MID_OPEN_FILE) {
            try {
                String filename = fileOpenBox(".");
                 if (filename != null) {
                     try {
                         new TEditorWindow(this, new File(filename));
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onMenu(menu);
    }

    // ------------------------------------------------------------------------
    // DemoApplication --------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Add all the widgets of the demo.
     */
    private void addAllWidgets() {
        new DemoMainWindow(this);

        // Add the menus
        addToolMenu();
        addFileMenu();
        addEditMenu();

        TMenu demoMenu = addMenu(i18n.getString("demo"));
        TMenuItem item = demoMenu.addItem(2000, i18n.getString("checkable"));
        item.setCheckable(true);
        item = demoMenu.addItem(2001, i18n.getString("disabled"));
        item.setEnabled(false);
        item = demoMenu.addItem(2002, i18n.getString("normal"));
        TSubMenu subMenu = demoMenu.addSubMenu(i18n.getString("subMenu"));
        item = demoMenu.addItem(2010, i18n.getString("normal"));
        item = demoMenu.addItem(2050, i18n.getString("colors"));

        item = subMenu.addItem(2000, i18n.getString("checkableSub"));
        item.setCheckable(true);
        item = subMenu.addItem(2001, i18n.getString("disabledSub"));
        item.setEnabled(false);
        item = subMenu.addItem(2002, i18n.getString("normalSub"));

        subMenu = subMenu.addSubMenu(i18n.getString("subMenu"));
        item = subMenu.addItem(2000, i18n.getString("checkableSub"));
        item.setCheckable(true);
        item = subMenu.addItem(2001, i18n.getString("disabledSub"));
        item.setEnabled(false);
        item = subMenu.addItem(2002, i18n.getString("normalSub"));

        if (getScreen() instanceof SwingTerminal) {
            TMenu swingMenu = addMenu(i18n.getString("swing"));
            item = swingMenu.addItem(3000, i18n.getString("bigger"));
            item = swingMenu.addItem(3001, i18n.getString("smaller"));
        }

        addTableMenu();
        addWindowMenu();
        addHelpMenu();
    }

}
