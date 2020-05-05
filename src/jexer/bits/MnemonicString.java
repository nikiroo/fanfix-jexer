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
package jexer.bits;

/**
 * MnemonicString is used to render a string like "&amp;File" into a
 * highlighted 'F' and the rest of 'ile'.  To insert a literal '&amp;', use
 * two '&amp;&amp;' characters, e.g. "&amp;File &amp;&amp; Stuff" would be
 * "File &amp; Stuff" with the first 'F' highlighted.
 */
public class MnemonicString {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Keyboard shortcut to activate this item.
     */
    private int shortcut;

    /**
     * Location of the highlighted character.
     */
    private int shortcutIdx = -1;

    /**
     * Screen location of the highlighted character (number of text cells
     * required to display from the beginning to shortcutIdx).
     */
    private int screenShortcutIdx = -1;

    /**
     * The raw (uncolored) string.
     */
    private String rawLabel;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param label widget label or title.  Label must contain a keyboard
     * shortcut, denoted by prefixing a letter with "&amp;", e.g. "&amp;File"
     */
    public MnemonicString(final String label) {

        // Setup the menu shortcut
        StringBuilder newLabel = new StringBuilder();
        boolean foundAmp = false;
        boolean foundShortcut = false;
        int scanShortcutIdx = 0;
        int scanScreenShortcutIdx = 0;
        for (int i = 0; i < label.length();) {
            int c = label.codePointAt(i);
            i += Character.charCount(c);

            if (c == '&') {
                if (foundAmp) {
                    newLabel.append('&');
                    scanShortcutIdx++;
                    scanScreenShortcutIdx++;
                } else {
                    foundAmp = true;
                }
            } else {
                newLabel.append(Character.toChars(c));
                if (foundAmp) {
                    if (!foundShortcut) {
                        shortcut = c;
                        foundAmp = false;
                        foundShortcut = true;
                        shortcutIdx = scanShortcutIdx;
                        screenShortcutIdx = scanScreenShortcutIdx;
                    }
                } else {
                    scanShortcutIdx++;
                    scanScreenShortcutIdx += StringUtils.width(c);
                }
            }
        }
        this.rawLabel = newLabel.toString();
    }

    // ------------------------------------------------------------------------
    // MnemonicString ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the keyboard shortcut character.
     *
     * @return the highlighted character
     */
    public int getShortcut() {
        return shortcut;
    }

    /**
     * Get location of the highlighted character.
     *
     * @return location of the highlighted character
     */
    public int getShortcutIdx() {
        return shortcutIdx;
    }

    /**
     * Get the screen location of the highlighted character.
     *
     * @return the number of text cells required to display from the
     * beginning of the label to shortcutIdx
     */
    public int getScreenShortcutIdx() {
        return screenShortcutIdx;
    }

    /**
     * Get the raw (uncolored) string.
     *
     * @return the raw (uncolored) string
     */
    public String getRawLabel() {
        return rawLabel;
    }

}
