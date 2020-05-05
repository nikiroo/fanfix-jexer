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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import jexer.bits.CellAttributes;

/**
 * TExceptionDialog displays an exception and its stack trace to the user,
 * and provides a means to save a troubleshooting report for support.
 */
public class TExceptionDialog extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(TExceptionDialog.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The exception.  We will actually make it Throwable, for the unlikely
     * event we catch an Error rather than an Exception.
     */
    private Throwable exception;

    /**
     * The exception's stack trace.
     */
    private TList stackTrace;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param application TApplication that manages this window
     * @param exception the exception to display
     */
    public TExceptionDialog(final TApplication application,
        final Throwable exception) {

        super(application, i18n.getString("windowTitle"),
            1, 1, 78, 22, CENTERED | MODAL);

        this.exception = exception;

        addLabel(i18n.getString("captionLine1"), 1, 1,
            "twindow.background.modal");
        addLabel(i18n.getString("captionLine2"), 1, 2,
            "twindow.background.modal");
        addLabel(i18n.getString("captionLine3"), 1, 3,
            "twindow.background.modal");
        addLabel(i18n.getString("captionLine4"), 1, 4,
            "twindow.background.modal");

        addLabel(MessageFormat.format(i18n.getString("exceptionString"),
                exception.getClass().getName(), exception.getMessage()),
            2, 6, "ttext", false);

        ArrayList<String> stackTraceStrings = new ArrayList<String>();
        stackTraceStrings.add(exception.getMessage());
        StackTraceElement [] stack = exception.getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            stackTraceStrings.add(stack[i].toString());
        }
        stackTrace = addList(stackTraceStrings, 2, 7, getWidth() - 6, 10);

        // Buttons
        addButton(i18n.getString("saveButton"), 21, getHeight() - 4,
            new TAction() {
                public void DO() {
                    saveToFile();
                }
            });

        TButton closeButton = addButton(i18n.getString("closeButton"),
            37, getHeight() - 4,
            new TAction() {
                public void DO() {
                    // Don't do anything, just close the window.
                    TExceptionDialog.this.close();
                }
            });

        // Save this for last: make the close button default action.
        activate(closeButton);
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the exception message background.
     */
    @Override
    public void draw() {
        // Draw window and border.
        super.draw();

        CellAttributes boxColor = getTheme().getColor("ttext");
        hLineXY(3, 7, getWidth() - 6, ' ', boxColor);
    }

    // ------------------------------------------------------------------------
    // TExceptionDialog -------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Save a troubleshooting report to file.  Note that we do NOT translate
     * the strings within the error report.
     */
    private void saveToFile() {
        // Prompt for filename.
        PrintWriter writer = null;
        try {
            String filename = fileSaveBox(".");
            if (filename == null) {
                // User cancelled, bail out.
                return;
            }
            writer = new PrintWriter(new FileWriter(filename));
            writer.write("Date: " + new Date(System.currentTimeMillis())
                + "\n");

            // System properties
            writer.write("System properties:\n");
            writer.write("-----------------------------------\n");
            System.getProperties().store(writer, null);
            writer.write("-----------------------------------\n");
            writer.write("\n");

            // The exception we caught
            writer.write("Caught exception:\n");
            writer.write("-----------------------------------\n");
            exception.printStackTrace(writer);
            writer.write("-----------------------------------\n");
            writer.write("\n");
            // The exception's cause, if it was set
            if (exception.getCause() != null) {
                writer.write("Caught exception's cause:\n");
                writer.write("-----------------------------------\n");
                exception.getCause().printStackTrace(writer);
                writer.write("-----------------------------------\n");
            }
            writer.write("\n");

            // The UI stack trace
            writer.write("UI stack trace:\n");
            writer.write("-----------------------------------\n");
            (new Throwable("UI Thread")).printStackTrace(writer);
            writer.write("-----------------------------------\n");
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            messageBox(i18n.getString("errorDialogTitle"),
                MessageFormat.format(i18n.
                    getString("errorSavingFile"), e.getMessage()));
        } finally {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }
    }
}
