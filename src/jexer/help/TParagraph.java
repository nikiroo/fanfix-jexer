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
package jexer.help;

import java.util.List;

import jexer.TWidget;

/**
 * TParagraph contains a reflowable collection of TWords, some of which are
 * clickable links.
 */
public class TParagraph extends TWidget {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Topic text and links converted to words.
     */
    private List<TWord> words;

    /**
     * If true, add one row to height as a paragraph separator.  Note package
     * private access.
     */
    boolean separator = true;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param words the pieces of the paragraph to display
     */
    public TParagraph(final THelpText parent, final List<TWord> words) {

        // Set parent and window
        super(parent, 0, 0, parent.getWidth() - 1, 1);

        this.words = words;
        for (TWord word: words) {
            word.setParent(this, false);
        }

        reflowData();
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TParagraph -------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Reposition the words in this paragraph to reflect the new width, and
     * set the paragraph height.
     */
    public void reflowData() {
        int x = 0;
        int y = 0;
        for (TWord word: words) {
            if (x + word.getWidth() >= getWidth()) {
                x = 0;
                y++;
            }
            word.setX(x);
            word.setY(y);
            x += word.getWidth() + 1;
        }
        if (separator) {
            setHeight(y + 2);
        } else {
            setHeight(y + 1);
        }
    }

    /**
     * Try to select a previous link.
     *
     * @return true if there was a previous link in this paragraph to select
     */
    public boolean up() {
        if (words.size() == 0) {
            return false;
        }
        if (getActiveChild() == this) {
            // No selectable links
            return false;
        }
        TWord firstWord = null;
        TWord lastWord = null;
        for (TWord word: words) {
            if (word.isEnabled()) {
                if (firstWord == null) {
                    firstWord = word;
                }
                lastWord = word;
            }
        }
        if (getActiveChild() == firstWord) {
            return false;
        }
        switchWidget(false);
        return true;
    }

    /**
     * Try to select a next link.
     *
     * @return true if there was a next link in this paragraph to select
     */
    public boolean down() {
        if (words.size() == 0) {
            return false;
        }
        if (getActiveChild() == this) {
            // No selectable links
            return false;
        }
        TWord firstWord = null;
        TWord lastWord = null;
        for (TWord word: words) {
            if (word.isEnabled()) {
                if (firstWord == null) {
                    firstWord = word;
                }
                lastWord = word;
            }
        }
        if (getActiveChild() == lastWord) {
            return false;
        }
        switchWidget(true);
        return true;
    }


}
