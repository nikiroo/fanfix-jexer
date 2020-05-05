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

import java.io.IOException;
import java.util.ResourceBundle;

import jexer.TApplication;
import jexer.TWidget;
import jexer.TWindow;
import jexer.event.TResizeEvent;
import jexer.ttree.TDirectoryTreeItem;
import jexer.ttree.TTreeViewWidget;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This window demonstates the TTreeView widget.
 */
public class DemoTreeViewWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoTreeViewWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Hang onto my TTreeView so I can resize it with the window.
     */
    private TTreeViewWidget treeView;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent the main application
     * @throws IOException if a java.io operation throws
     */
    public DemoTreeViewWindow(final TApplication parent) throws IOException {
        super(parent, i18n.getString("windowTitle"), 0, 0, 44, 16,
            TWindow.RESIZABLE);

        // Load the treeview with "stuff"
        treeView = addTreeViewWidget(1, 1, 40, 12);
        new TDirectoryTreeItem(treeView, ".", true);

        statusBar = newStatusBar(i18n.getString("statusBar"));
        statusBar.addShortcutKeypress(kbF1, cmHelp,
            i18n.getString("statusBarHelp"));
        statusBar.addShortcutKeypress(kbF2, cmShell,
            i18n.getString("statusBarShell"));
        statusBar.addShortcutKeypress(kbF3, cmOpen,
            i18n.getString("statusBarOpen"));
        statusBar.addShortcutKeypress(kbF10, cmExit,
            i18n.getString("statusBarExit"));
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle window/screen resize events.
     *
     * @param resize resize event
     */
    @Override
    public void onResize(final TResizeEvent resize) {
        if (resize.getType() == TResizeEvent.Type.WIDGET) {
            // Resize the treeView field
            TResizeEvent treeSize = new TResizeEvent(TResizeEvent.Type.WIDGET,
                resize.getWidth() - 4, resize.getHeight() - 4);
            treeView.onResize(treeSize);
            return;
        }

        // Pass to children instead
        for (TWidget widget: getChildren()) {
            widget.onResize(resize);
        }
    }

}
