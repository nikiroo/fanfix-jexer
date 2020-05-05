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

/**
 * TInputBox is a system-modal dialog with an OK button and a text input
 * field.  Call it like:
 *
 * <pre>
 * {@code
 *     box = inputBox(title, caption);
 *     if (box.getText().equals("yes")) {
 *         ... the user entered "yes", do stuff ...
 *     }
 * }
 * </pre>
 *
 */
public class TInputBox extends TMessageBox {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The input field.
     */
    private TField field;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  The input box will be centered on screen.
     *
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     */
    public TInputBox(final TApplication application, final String title,
        final String caption) {

        this(application, title, caption, "", Type.OK);
    }

    /**
     * Public constructor.  The input box will be centered on screen.
     *
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     * @param text initial text to seed the field with
     */
    public TInputBox(final TApplication application, final String title,
        final String caption, final String text) {

        this(application, title, caption, text, Type.OK);
    }

    /**
     * Public constructor.  The input box will be centered on screen.
     *
     * @param application TApplication that manages this window
     * @param title window title, will be centered along the top border
     * @param caption message to display.  Use embedded newlines to get a
     * multi-line box.
     * @param text initial text to seed the field with
     * @param type one of the Type constants.  Default is Type.OK.
     */
    public TInputBox(final TApplication application, final String title,
        final String caption, final String text, final Type type) {

        super(application, title, caption, type, false);

        for (TWidget widget: getChildren()) {
            if (widget instanceof TButton) {
                widget.setY(widget.getY() + 2);
            }
        }

        setHeight(getHeight() + 2);
        field = addField(1, getHeight() - 6, getWidth() - 4, false, text);

        // Set the secondaryThread to run me
        getApplication().enableSecondaryEventReceiver(this);

        // Yield to the secondary thread.  When I come back from the
        // constructor response will already be set.
        getApplication().yield();
    }

    // ------------------------------------------------------------------------
    // TMessageBox ------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TInputBox --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Retrieve the answer text.
     *
     * @return the answer text
     */
    public String getText() {
        return field.getText();
    }

}
