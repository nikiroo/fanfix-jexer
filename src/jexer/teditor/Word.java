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
package jexer.teditor;

import jexer.bits.CellAttributes;
import jexer.bits.StringUtils;

/**
 * A Word represents text that was entered by the user.  It can be either
 * whitespace or non-whitespace.
 *
 * Very dumb highlighting is supported, it has no sense of parsing (not even
 * comments).  For now this only highlights some Java keywords and
 * puctuation.
 */
public class Word {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The color to render this word as on screen.
     */
    private CellAttributes color = new CellAttributes();

    /**
     * The default color for the TEditor class.
     */
    private CellAttributes defaultColor = null;

    /**
     * The text highlighter to use.
     */
    private Highlighter highlighter = null;

    /**
     * The actual text of this word.  Average word length is 6 characters,
     * with a lot of shorter ones, so start with 3.
     */
    private StringBuilder text = new StringBuilder(3);

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Construct a word with one character.
     *
     * @param ch the first character of the word
     * @param defaultColor the color for unhighlighted text
     * @param highlighter the highlighter to use
     */
    public Word(final int ch, final CellAttributes defaultColor,
        final Highlighter highlighter) {

        this.defaultColor = defaultColor;
        this.highlighter = highlighter;
        text.append(Character.toChars(ch));
    }

    /**
     * Construct a word with an empty string.
     *
     * @param defaultColor the color for unhighlighted text
     * @param highlighter the highlighter to use
     */
    public Word(final CellAttributes defaultColor,
        final Highlighter highlighter) {

        this.defaultColor = defaultColor;
        this.highlighter = highlighter;
    }

    // ------------------------------------------------------------------------
    // Word -------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the color used to display this word on screen.
     *
     * @return the color
     */
    public CellAttributes getColor() {
        return new CellAttributes(color);
    }

    /**
     * Set the color used to display this word on screen.
     *
     * @param color the color
     */
    public void setColor(final CellAttributes color) {
        color.setTo(color);
    }

    /**
     * Get the text to display.
     *
     * @return the text
     */
    public String getText() {
        return text.toString();
    }

    /**
     * Get the on-screen display length.
     *
     * @return the number of cells needed to display this word
     */
    public int getDisplayLength() {
        return StringUtils.width(text.toString());
    }

    /**
     * See if this is a whitespace word.  Note that empty string is
     * considered whitespace.
     *
     * @return true if this word is whitespace
     */
    public boolean isWhitespace() {
        if (text.length() == 0) {
            return true;
        }
        if (Character.isWhitespace(text.charAt(0))) {
            return true;
        }
        return false;
    }

    /**
     * Perform highlighting.
     */
    public void applyHighlight() {
        color.setTo(defaultColor);
        if (highlighter == null) {
            return;
        }
        String key = text.toString();
        CellAttributes newColor = highlighter.getColor(key);
        if (newColor != null) {
            color.setTo(newColor);
        }
    }

    /**
     * Add a character to this word.  If this is a whitespace character
     * adding to a non-whitespace word, create a new word and return that;
     * similarly if this a non-whitespace character adding to a whitespace
     * word, create a new word and return that.  Note package private access:
     * this is only called by Line to figure out highlighting boundaries.
     *
     * @param ch the new character to add
     * @return either this word (if it was added), or a new word that
     * contains ch
     */
    public Word addChar(final int ch) {
        if (text.length() == 0) {
            text.append(Character.toChars(ch));
            return this;
        }

        // Give the highlighter the option to split here.
        if (highlighter != null) {
            if (highlighter.shouldSplit(ch)
                || highlighter.shouldSplit(text.charAt(0))
            ) {
                Word newWord = new Word(ch, defaultColor, highlighter);
                return newWord;
            }
        }

        // Highlighter didn't care, so split at whitespace.
        if (Character.isWhitespace(text.charAt(0))
            && Character.isWhitespace(ch)
        ) {
            // Adding to a whitespace word, keep at it.
            text.append(Character.toChars(ch));
            return this;
        }
        if (!Character.isWhitespace(text.charAt(0))
            && !Character.isWhitespace(ch)
        ) {
            // Adding to a non-whitespace word, keep at it.
            text.append(Character.toChars(ch));
            return this;
        }

        // Switching from whitespace to non-whitespace or vice versa, so
        // split here.
        Word newWord = new Word(ch, defaultColor, highlighter);
        return newWord;
    }

}
