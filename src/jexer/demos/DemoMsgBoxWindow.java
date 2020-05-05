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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TInputBox;
import jexer.TMessageBox;
import jexer.TWindow;
import jexer.layout.StretchLayoutManager;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This window demonstates the TMessageBox and TInputBox widgets.
 */
public class DemoMsgBoxWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoMsgBoxWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent the main application
     */
    DemoMsgBoxWindow(final TApplication parent) {
        this(parent, TWindow.CENTERED | TWindow.RESIZABLE);
    }

    /**
     * Constructor.
     *
     * @param parent the main application
     * @param flags bitmask of MODAL, CENTERED, or RESIZABLE
     */
    DemoMsgBoxWindow(final TApplication parent, final int flags) {
        // Construct a demo window.  X and Y don't matter because it
        // will be centered on screen.
        super(parent, i18n.getString("windowTitle"), 0, 0, 64, 18, flags);

        setLayoutManager(new StretchLayoutManager(getWidth() - 2,
                getHeight() - 2));

        int row = 1;

        // Add some widgets
        addLabel(i18n.getString("messageBoxLabel1"), 1, row);
        addButton(i18n.getString("messageBoxButton1"), 35, row,
            new TAction() {
                public void DO() {
                    getApplication().messageBox(i18n.
                        getString("messageBoxTitle1"),
                        i18n.getString("messageBoxPrompt1"),
                        TMessageBox.Type.OK);
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("messageBoxLabel2"), 1, row);
        addButton(i18n.getString("messageBoxButton2"), 35, row,
            new TAction() {
                public void DO() {
                    getApplication().messageBox(i18n.
                        getString("messageBoxTitle2"),
                        i18n.getString("messageBoxPrompt2"),
                        TMessageBox.Type.OKCANCEL);
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("messageBoxLabel3"), 1, row);
        addButton(i18n.getString("messageBoxButton3"), 35, row,
            new TAction() {
                public void DO() {
                    getApplication().messageBox(i18n.
                        getString("messageBoxTitle3"),
                        i18n.getString("messageBoxPrompt3"),
                        TMessageBox.Type.YESNO);
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("messageBoxLabel4"), 1, row);
        addButton(i18n.getString("messageBoxButton4"), 35, row,
            new TAction() {
                public void DO() {
                    getApplication().messageBox(i18n.
                        getString("messageBoxTitle4"),
                        i18n.getString("messageBoxPrompt4"),
                        TMessageBox.Type.YESNOCANCEL);
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("inputBoxLabel1"), 1, row);
        addButton(i18n.getString("inputBoxButton1"), 35, row,
            new TAction() {
                public void DO() {
                    TInputBox in = getApplication().inputBox(i18n.
                        getString("inputBoxTitle1"),
                        i18n.getString("inputBoxPrompt1"),
                        i18n.getString("inputBoxInput1"));
                    getApplication().messageBox(i18n.
                        getString("inputBoxAnswerTitle1"),
                        MessageFormat.format(i18n.
                            getString("inputBoxAnswerPrompt1"), in.getText()));
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("inputBoxLabel2"), 1, row);
        addButton(i18n.getString("inputBoxButton2"), 35, row,
            new TAction() {
                public void DO() {
                    TInputBox in = getApplication().inputBox(i18n.
                        getString("inputBoxTitle2"),
                        i18n.getString("inputBoxPrompt2"),
                        i18n.getString("inputBoxInput2"),
                        TInputBox.Type.OKCANCEL);
                    getApplication().messageBox(i18n.
                        getString("inputBoxAnswerTitle2"),
                        MessageFormat.format(i18n.
                            getString("inputBoxAnswerPrompt2"), in.getText(),
                            in.getResult()));
                }
            }
        );
        row += 2;

        addButton(i18n.getString("closeWindow"),
            (getWidth() - 14) / 2, getHeight() - 4,
            new TAction() {
                public void DO() {
                    getApplication().closeWindow(DemoMsgBoxWindow.this);
                }
            }
        );

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
}
