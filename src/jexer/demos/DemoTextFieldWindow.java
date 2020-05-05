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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TCalendar;
import jexer.TField;
import jexer.TLabel;
import jexer.TMessageBox;
import jexer.TWindow;
import jexer.layout.StretchLayoutManager;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This window demonstates the TField and TPasswordField widgets.
 */
public class DemoTextFieldWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoTextFieldWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Calendar.  Has to be at class scope so that it can be accessed by the
     * anonymous TAction class.
     */
    TCalendar calendar = null;

    /**
     * Day of week label is updated with TSpinner clicks.
     */
    TLabel dayOfWeekLabel;

    /**
     * Day of week to demonstrate TSpinner.  Has to be at class scope so that
     * it can be accessed by the anonymous TAction class.
     */
    GregorianCalendar dayOfWeekCalendar = new GregorianCalendar();

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent the main application
     */
    DemoTextFieldWindow(final TApplication parent) {
        this(parent, TWindow.CENTERED | TWindow.RESIZABLE);
    }

    /**
     * Constructor.
     *
     * @param parent the main application
     * @param flags bitmask of MODAL, CENTERED, or RESIZABLE
     */
    DemoTextFieldWindow(final TApplication parent, final int flags) {
        // Construct a demo window.  X and Y don't matter because it
        // will be centered on screen.
        super(parent, i18n.getString("windowTitle"), 0, 0, 60, 20, flags);

        setLayoutManager(new StretchLayoutManager(getWidth() - 2,
                getHeight() - 2));

        int row = 1;

        addLabel(i18n.getString("textField1"), 1, row);
        addField(35, row++, 15, false, "Field text");
        addLabel(i18n.getString("textField2"), 1, row);
        addField(35, row++, 15, true);
        addLabel(i18n.getString("textField3"), 1, row);
        addPasswordField(35, row++, 15, false);
        addLabel(i18n.getString("textField4"), 1, row);
        addPasswordField(35, row++, 15, true, "hunter2");
        addLabel(i18n.getString("textField5"), 1, row);
        TField selected = addField(35, row++, 40, false,
            i18n.getString("textField6"));
        row += 1;

        calendar = addCalendar(1, row++,
            new TAction() {
                public void DO() {
                    getApplication().messageBox(i18n.getString("calendarTitle"),
                        MessageFormat.format(i18n.getString("calendarMessage"),
                            new Date(calendar.getValue().getTimeInMillis())),
                        TMessageBox.Type.OK);
                }
            }
        );

        dayOfWeekLabel = addLabel("Wednesday-", 35, row - 1, "tmenu", false);
        dayOfWeekLabel.setLabel(String.format("%-10s",
                dayOfWeekCalendar.getDisplayName(Calendar.DAY_OF_WEEK,
                    Calendar.LONG, Locale.getDefault())));

        addSpinner(35 + dayOfWeekLabel.getWidth(), row - 1,
            new TAction() {
                public void DO() {
                    dayOfWeekCalendar.add(Calendar.DAY_OF_WEEK, 1);
                    dayOfWeekLabel.setLabel(String.format("%-10s",
                            dayOfWeekCalendar.getDisplayName(
                            Calendar.DAY_OF_WEEK, Calendar.LONG,
                            Locale.getDefault())));
                }
            },
            new TAction() {
                public void DO() {
                    dayOfWeekCalendar.add(Calendar.DAY_OF_WEEK, -1);
                    dayOfWeekLabel.setLabel(String.format("%-10s",
                            dayOfWeekCalendar.getDisplayName(
                            Calendar.DAY_OF_WEEK, Calendar.LONG,
                            Locale.getDefault())));
                }
            }
        );


        addButton(i18n.getString("closeWindow"),
            (getWidth() - 14) / 2, getHeight() - 4,
            new TAction() {
                public void DO() {
                    getApplication().closeWindow(DemoTextFieldWindow.this);
                }
            }
        );

        activate(selected);

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
