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

import jexer.TAction;
import jexer.TApplication;
import jexer.TText;
import jexer.TWidget;
import jexer.TWindow;
import jexer.event.TResizeEvent;
import jexer.menu.TMenu;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This window demonstates the TText, THScroller, and TVScroller widgets.
 */
public class DemoTextWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoTextWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Hang onto my TText so I can resize it with the window.
     */
    private TText textField;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor makes a text window out of any string.
     *
     * @param parent the main application
     * @param title the text string
     * @param text the text string
     */
    public DemoTextWindow(final TApplication parent, final String title,
        final String text) {

        super(parent, title, 0, 0, 44, 22, RESIZABLE);
        textField = addText(text, 1, 3, 40, 16);

        addButton(i18n.getString("left"), 1, 1, new TAction() {
                public void DO() {
                    textField.leftJustify();
                }
        });

        addButton(i18n.getString("center"), 10, 1, new TAction() {
                public void DO() {
                    textField.centerJustify();
                }
        });

        addButton(i18n.getString("right"), 21, 1, new TAction() {
                public void DO() {
                    textField.rightJustify();
                }
        });

        addButton(i18n.getString("full"), 31, 1, new TAction() {
                public void DO() {
                    textField.fullJustify();
                }
        });

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

    /**
     * Public constructor.
     *
     * @param parent the main application
     */
    public DemoTextWindow(final TApplication parent) {
        this(parent, i18n.getString("windowTitle"),
"This is an example of a reflowable text field.  Some example text follows.\n" +
"\n" +
"Notice that some menu items should be disabled when this window has focus.\n" +
"\n" +
"This library implements a text-based windowing system loosely " +
"reminiscent of Borland's [Turbo " +
"Vision](http://en.wikipedia.org/wiki/Turbo_Vision) library.  For those " +
"wishing to use the actual C++ Turbo Vision library, see [Sergio " +
"Sigala's updated version](http://tvision.sourceforge.net/) that runs " +
"on many more platforms.\n" +
"\n" +
"This library is licensed MIT.  See the file LICENSE for the full license " +
"for the details.\n");

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
            TResizeEvent textSize = new TResizeEvent(TResizeEvent.Type.WIDGET,
                event.getWidth() - 4, event.getHeight() - 6);
            textField.onResize(textSize);
            return;
        }

        // Pass to children instead
        for (TWidget widget: getChildren()) {
            widget.onResize(event);
        }
    }

    /**
     * Play with menu items.
     */
    public void onFocus() {
        getApplication().enableMenuItem(2001);
        getApplication().disableMenuItem(TMenu.MID_SHELL);
        getApplication().disableMenuItem(TMenu.MID_EXIT);
    }

    /**
     * Called by application.switchWindow() when another window gets the
     * focus.
     */
    public void onUnfocus() {
        getApplication().disableMenuItem(2001);
        getApplication().enableMenuItem(TMenu.MID_SHELL);
        getApplication().enableMenuItem(TMenu.MID_EXIT);
    }

}
