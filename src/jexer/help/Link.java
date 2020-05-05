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

import java.util.HashSet;
import java.util.Set;
import java.util.ResourceBundle;

/**
 * A Link is a section of text with a reference to a Topic.
 */
public class Link {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The topic id that this link points to.
     */
    private String topic;

    /**
     * The text inside the link tag.
     */
    private String text;

    /**
     * The number of words in this link.
     */
    private int wordCount;

    /**
     * The word number (from the beginning of topic text) that corresponds to
     * the first word of this link.
     */
    private int index;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param topic the topic to point to
     * @param text the text inside the link tag
     * @param index the word count index
     */
    public Link(final String topic, final String text, final int index) {
        this.topic = topic;
        this.text = text;
        this.index = index;
        this.wordCount = text.split("\\s+").length;
    }

    // ------------------------------------------------------------------------
    // Link -------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the topic.
     *
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Get the link text.
     *
     * @return the text inside the link tag
     */
    public String getText() {
        return text;
    }

    /**
     * Get the word index for this link.
     *
     * @return the word number (from the beginning of topic text) that
     * corresponds to the first word of this link
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the number of words in this link.
     *
     * @return the number of words in this link
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Generate a human-readable string for this widget.
     *
     * @return a human-readable string
     */
    @Override
    public String toString() {
        return String.format("%s(%8x) topic %s link text %s word # %d count %d",
            getClass().getName(), hashCode(), topic, text, index, wordCount);
    }

}
