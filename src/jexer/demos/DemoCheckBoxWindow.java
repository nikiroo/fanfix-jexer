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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jexer.TAction;
import jexer.TApplication;
import jexer.TComboBox;
import jexer.TMessageBox;
import jexer.TRadioGroup;
import jexer.TWindow;
import jexer.layout.StretchLayoutManager;
import static jexer.TCommand.*;
import static jexer.TKeypress.*;

/**
 * This window demonstates the TRadioGroup, TRadioButton, and TCheckBox
 * widgets.
 */
public class DemoCheckBoxWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DemoCheckBoxWindow.class.getName());

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Combo box.  Has to be at class scope so that it can be accessed by the
     * anonymous TAction class.
     */
    TComboBox comboBox = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent the main application
     */
    DemoCheckBoxWindow(final TApplication parent) {
        this(parent, CENTERED | RESIZABLE);
    }

    /**
     * Constructor.
     *
     * @param parent the main application
     * @param flags bitmask of MODAL, CENTERED, or RESIZABLE
     */
    DemoCheckBoxWindow(final TApplication parent, final int flags) {
        // Construct a demo window.  X and Y don't matter because it will be
        // centered on screen.
        super(parent, i18n.getString("windowTitle"), 0, 0, 60, 17, flags);

        setLayoutManager(new StretchLayoutManager(getWidth() - 2,
                getHeight() - 2));

        int row = 1;

        // Add some widgets
        addLabel(i18n.getString("checkBoxLabel1"), 1, row);
        addCheckBox(35, row++, i18n.getString("checkBoxText1"), false);
        addLabel(i18n.getString("checkBoxLabel2"), 1, row);
        addCheckBox(35, row++, i18n.getString("checkBoxText2"), true);
        row += 2;

        TRadioGroup group = addRadioGroup(1, row,
            i18n.getString("radioGroupTitle"));
        group.addRadioButton(i18n.getString("radioOption1"));
        group.addRadioButton(i18n.getString("radioOption2"), true);
        group.addRadioButton(i18n.getString("radioOption3"));
        group.setRequiresSelection(true);

        List<String> comboValues = new ArrayList<String>();
        comboValues.add(i18n.getString("comboBoxString0"));
        comboValues.add(i18n.getString("comboBoxString1"));
        comboValues.add(i18n.getString("comboBoxString2"));
        comboValues.add(i18n.getString("comboBoxString3"));
        comboValues.add(i18n.getString("comboBoxString4"));
        comboValues.add(i18n.getString("comboBoxString5"));
        comboValues.add(i18n.getString("comboBoxString6"));
        comboValues.add(i18n.getString("comboBoxString7"));
        comboValues.add(i18n.getString("comboBoxString8"));
        comboValues.add(i18n.getString("comboBoxString9"));
        comboValues.add(i18n.getString("comboBoxString10"));

        comboBox = addComboBox(35, row, 12, comboValues, 2, 6,
            new TAction() {
                public void DO() {
                    getApplication().messageBox(i18n.getString("messageBoxTitle"),
                        MessageFormat.format(i18n.getString("messageBoxPrompt"),
                            comboBox.getText()),
                        TMessageBox.Type.OK);
                }
            }
        );

        addButton(i18n.getString("closeWindow"),
            (getWidth() - 14) / 2, getHeight() - 4,
            new TAction() {
                public void DO() {
                    DemoCheckBoxWindow.this.getApplication()
                        .closeWindow(DemoCheckBoxWindow.this);
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
