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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TEditColorThemeWindow;
import jexer.TEditorWindow;
import jexer.TLabel;
import jexer.TProgressBar;
import jexer.TTableWindow;
import jexer.TTimer;
import jexer.TWidget;
import jexer.TWindow;
import jexer.event.TCommandEvent;
import jexer.layout.StretchLayoutManager;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This is the main "demo" application window.  It makes use of the TTimer,
 * TProgressBox, TLabel, TButton, and TField widgets.
 */
public class DemoMainWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoMainWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Timer that increments a number.
     */
    private TTimer timer1;

    /**
     * Timer that increments a number.
     */
    private TTimer timer2;

    /**
     * Timer label is updated with timer ticks.
     */
    TLabel timerLabel;

    /**
     * Timer increment used by the timer loop.  Has to be at class scope so
     * that it can be accessed by the anonymous TAction class.
     */
    int timer1I = 0;

    /**
     * Timer increment used by the timer loop.  Has to be at class scope so
     * that it can be accessed by the anonymous TAction class.
     */
    int timer2I = 0;

    /**
     * Progress bar used by the timer loop.  Has to be at class scope so that
     * it can be accessed by the anonymous TAction class.
     */
    TProgressBar progressBar1;

    /**
     * Progress bar used by the timer loop.  Has to be at class scope so that
     * it can be accessed by the anonymous TAction class.
     */
    TProgressBar progressBar2;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Construct demo window.  It will be centered on screen.
     *
     * @param parent the main application
     */
    public DemoMainWindow(final TApplication parent) {
        this(parent, CENTERED | RESIZABLE);
    }

    /**
     * Constructor.
     *
     * @param parent the main application
     * @param flags bitmask of MODAL, CENTERED, or RESIZABLE
     */
    private DemoMainWindow(final TApplication parent, final int flags) {
        // Construct a demo window.  X and Y don't matter because it will be
        // centered on screen.
        super(parent, i18n.getString("windowTitle"), 0, 0, 64, 23, flags);

        setLayoutManager(new StretchLayoutManager(getWidth() - 2,
                getHeight() - 2));

        int row = 1;

        // Add some widgets
        addLabel(i18n.getString("messageBoxLabel"), 1, row);
        TWidget first = addButton(i18n.getString("messageBoxButton"), 35, row,
            new TAction() {
                public void DO() {
                    new DemoMsgBoxWindow(getApplication());
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("openModalLabel"), 1, row);
        addButton(i18n.getString("openModalButton"), 35, row,
            new TAction() {
                public void DO() {
                    new DemoMainWindow(getApplication(), MODAL);
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("textFieldLabel"), 1, row);
        addButton(i18n.getString("textFieldButton"), 35, row,
            new TAction() {
                public void DO() {
                    new DemoTextFieldWindow(getApplication());
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("radioButtonLabel"), 1, row);
        addButton(i18n.getString("radioButtonButton"), 35, row,
            new TAction() {
                public void DO() {
                    new DemoCheckBoxWindow(getApplication());
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("editorLabel"), 1, row);
        addButton(i18n.getString("editorButton1"), 35, row,
            new TAction() {
                public void DO() {
                    new DemoEditorWindow(getApplication());
                }
            }
        );
        addButton(i18n.getString("editorButton2"), 48, row,
            new TAction() {
                public void DO() {
                    new TEditorWindow(getApplication());
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("textAreaLabel"), 1, row);
        addButton(i18n.getString("textAreaButton"), 35, row,
            new TAction() {
                public void DO() {
                    new DemoTextWindow(getApplication());
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("ttableLabel"), 1, row);
        addButton(i18n.getString("ttableButton1"), 35, row,
            new TAction() {
                public void DO() {
                    new DemoTableWindow(getApplication(),
                        i18n.getString("tableWidgetDemo"));
                }
            }
        );
        addButton(i18n.getString("ttableButton2"), 48, row,
            new TAction() {
                public void DO() {
                    new TTableWindow(getApplication(),
                        i18n.getString("tableDemo"));
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("treeViewLabel"), 1, row);
        addButton(i18n.getString("treeViewButton"), 35, row,
            new TAction() {
                public void DO() {
                    try {
                        new DemoTreeViewWindow(getApplication());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("terminalLabel"), 1, row);
        addButton(i18n.getString("terminalButton"), 35, row,
            new TAction() {
                public void DO() {
                    getApplication().openTerminal(0, 0);
                }
            }
        );
        row += 2;

        addLabel(i18n.getString("colorEditorLabel"), 1, row);
        addButton(i18n.getString("colorEditorButton"), 35, row,
            new TAction() {
                public void DO() {
                    new TEditColorThemeWindow(getApplication());
                }
            }
        );

        row = 15;
        progressBar1 = addProgressBar(48, row, 12, 0);
        row++;
        timerLabel = addLabel(i18n.getString("timerLabel"), 48, row);
        timer1 = getApplication().addTimer(250, true,
            new TAction() {

                public void DO() {
                    timerLabel.setLabel(String.format(i18n.
                            getString("timerText"), timer1I));
                    timerLabel.setWidth(timerLabel.getLabel().length());
                    if (timer1I < 100) {
                        timer1I++;
                    } else {
                        timer1.setRecurring(false);
                    }
                    progressBar1.setValue(timer1I);
                }
            }
        );

        row += 2;
        progressBar2 = addProgressBar(48, row, 12, 0);
        progressBar2.setLeftBorderChar('\u255e');
        progressBar2.setRightBorderChar('\u2561');
        progressBar2.setCompletedChar('\u2592');
        progressBar2.setRemainingChar('\u2550');
        row++;
        timer2 = getApplication().addTimer(125, true,
            new TAction() {

                public void DO() {
                    if (timer2I < 100) {
                        timer2I++;
                    } else {
                        timer2.setRecurring(false);
                    }
                    progressBar2.setValue(timer2I);
                }
            }
        );

        /*
        addButton("Exception", 35, row + 3,
            new TAction() {
                public void DO() {
                    try {
                        throw new RuntimeException("FUBAR'd!");
                    } catch (Exception e) {
                        new jexer.TExceptionDialog(getApplication(), e);
                    }
                }
            }
        );
         */

        activate(first);

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
     * We need to override onClose so that the timer will no longer be called
     * after we close the window.  TTimers currently are completely unaware
     * of the rest of the UI classes.
     */
    @Override
    public void onClose() {
        getApplication().removeTimer(timer1);
        getApplication().removeTimer(timer2);
    }

    /**
     * Method that subclasses can override to handle posted command events.
     *
     * @param command command event
     */
    @Override
    public void onCommand(final TCommandEvent command) {
        if (command.equals(cmOpen)) {
            try {
                String filename = fileOpenBox(".");
                if (filename != null) {
                    try {
                        new TEditorWindow(getApplication(),
                            new File(filename));
                    } catch (IOException e) {
                        messageBox(i18n.getString("errorTitle"),
                            MessageFormat.format(i18n.
                                getString("errorReadingFile"), e.getMessage()));
                    }
                }
            } catch (IOException e) {
                        messageBox(i18n.getString("errorTitle"),
                            MessageFormat.format(i18n.
                                getString("errorOpeningFile"), e.getMessage()));
            }
            return;
        }

        // Didn't handle it, let children get it instead
        super.onCommand(command);
    }

}
