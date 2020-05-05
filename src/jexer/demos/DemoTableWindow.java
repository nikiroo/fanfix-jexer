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

import java.util.ResourceBundle;

import jexer.TApplication;
import jexer.TTableWidget;
import jexer.TWidget;
import jexer.TWindow;
import jexer.event.TResizeEvent;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This window demonstates the TTable widget.
 */
public class DemoTableWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoTableWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Hang onto my TTable so I can resize it with the window.
     */
    private TTableWidget tableField;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor makes a text window out of any string.
     *
     * @param parent the main application
     * @param title the text string
     */
    public DemoTableWindow(final TApplication parent, final String title) {

        super(parent, title, 0, 0, 44, 22, RESIZABLE);
        tableField = new TTableWidget(this, 0, 0, 42, 20);

        statusBar = newStatusBar(i18n.getString("statusBar"));
        statusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("statusBarHelp"));
        statusBar.addShortcutKeypress(kbF2, cmShell,
            i18n.getString("statusBarShell"));
        statusBar.addShortcutKeypress(kbF10, cmExit,
            i18n.getString("statusBarExit"));
    }

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public DemoTableWindow(final TApplication parent) {
        this(parent, i18n.getString("windowTitle"));
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle window/screen resize events.
     *
     * @param event resize event
     */
    @Override
    public void onResize(final TResizeEvent event) {
        if (event.getType() == TResizeEvent.Type.WIDGET) {
            // Resize the text field
            TResizeEvent tableSize = new TResizeEvent(TResizeEvent.Type.WIDGET,
                event.getWidth() - 2, event.getHeight() - 2);
            tableField.onResize(tableSize);
            return;
        }

        // Pass to children instead
        for (TWidget widget: getChildren()) {
            widget.onResize(event);
        }
    }

}
