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
import jexer.TPanel;
import jexer.TText;
import jexer.TWindow;
import jexer.layout.BoxLayoutManager;

/**
 * This class shows off BoxLayout and TPanel.
 */
public class Demo7 {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(Demo7.class.getName());

    // ------------------------------------------------------------------------
    // Demo7 ------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Main entry point.
     *
     * @param args Command line arguments
     */
    public static void main(final String [] args) throws Exception {
        // This demo will build everything "from the outside".

        // Swing is the default backend on Windows unless explicitly
        // overridden by jexer.Swing.
        TApplication.BackendType backendType = TApplication.BackendType.XTERM;
        if (System.getProperty("os.name").startsWith("Windows")) {
            backendType = TApplication.BackendType.SWING;
        }
        if (System.getProperty("os.name").startsWith("Mac")) {
            backendType = TApplication.BackendType.SWING;
        }
        if (System.getProperty("jexer.Swing") != null) {
            if (System.getProperty("jexer.Swing", "false").equals("true")) {
                backendType = TApplication.BackendType.SWING;
            } else {
                backendType = TApplication.BackendType.XTERM;
            }
        }
        TApplication app = new TApplication(backendType);
        app.addToolMenu();
        app.addFileMenu();
        TWindow window = new TWindow(app, i18n.getString("windowTitle"),
            60, 22);
        window.setLayoutManager(new BoxLayoutManager(window.getWidth() - 2,
                window.getHeight() - 2, false));

        TPanel right = window.addPanel(0, 0, 10, 10);
        TPanel left = window.addPanel(0, 0, 10, 10);
        right.setLayoutManager(new BoxLayoutManager(right.getWidth(),
                right.getHeight(), true));
        left.setLayoutManager(new BoxLayoutManager(left.getWidth(),
                left.getHeight(), true));

        left.addText("C1", 0, 0, left.getWidth(), left.getHeight());
        left.addText("C2", 0, 0, left.getWidth(), left.getHeight());
        left.addText("C3", 0, 0, left.getWidth(), left.getHeight());
        right.addText("C4", 0, 0, right.getWidth(), right.getHeight());
        right.addText("C5", 0, 0, right.getWidth(), right.getHeight());
        right.addText("C6", 0, 0, right.getWidth(), right.getHeight());

        app.run();
    }

}
