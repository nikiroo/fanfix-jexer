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
package jexer.backend;

import java.awt.Font;
import javax.swing.JComponent;

/**
 * This class uses standard Swing calls to handle screen, keyboard, and mouse
 * I/O.
 */
public class SwingBackend extends GenericBackend {

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  The window will be 80x25 with font size 20 pts.
     */
    public SwingBackend() {
        this(null, 80, 25, 20);
    }

    /**
     * Public constructor.  The window will be 80x25 with font size 20 pts.
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     */
    public SwingBackend(final Object listener) {
        this(listener, 80, 25, 20);
    }

    /**
     * Public constructor will spawn a new JFrame with font size 20 pts.
     *
     * @param windowWidth the number of text columns to start with
     * @param windowHeight the number of text rows to start with
     */
    public SwingBackend(final int windowWidth, final int windowHeight) {
        this(null, windowWidth, windowHeight, 20);
    }

    /**
     * Public constructor will spawn a new JFrame.
     *
     * @param windowWidth the number of text columns to start with
     * @param windowHeight the number of text rows to start with
     * @param fontSize the size in points.  Good values to pick are: 16, 20,
     * 22, and 24.
     */
    public SwingBackend(final int windowWidth, final int windowHeight,
        final int fontSize) {

        this(null, windowWidth, windowHeight, fontSize);
    }

    /**
     * Public constructor will spawn a new JFrame.
     *
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param windowWidth the number of text columns to start with
     * @param windowHeight the number of text rows to start with
     * @param fontSize the size in points.  Good values to pick are: 16, 20,
     * 22, and 24.
     */
    public SwingBackend(final Object listener, final int windowWidth,
        final int windowHeight, final int fontSize) {

        // Create a Swing backend using a JFrame
        terminal = new SwingTerminal(windowWidth, windowHeight, fontSize,
            listener);

        // Hang onto the session info
        this.sessionInfo = ((SwingTerminal) terminal).getSessionInfo();

        // SwingTerminal is the screen too
        screen = (SwingTerminal) terminal;
    }

    /**
     * Public constructor will render onto a JComponent.
     *
     * @param component the Swing component to render to
     * @param listener the object this backend needs to wake up when new
     * input comes in
     * @param windowWidth the number of text columns to start with
     * @param windowHeight the number of text rows to start with
     * @param fontSize the size in points.  Good values to pick are: 16, 20,
     * 22, and 24.
     */
    public SwingBackend(final JComponent component, final Object listener,
        final int windowWidth, final int windowHeight, final int fontSize) {

        // Create a Swing backend using a JComponent
        terminal = new SwingTerminal(component, windowWidth, windowHeight,
            fontSize, listener);

        // Hang onto the session info
        this.sessionInfo = ((SwingTerminal) terminal).getSessionInfo();

        // SwingTerminal is the screen too
        screen = (SwingTerminal) terminal;
    }

    // ------------------------------------------------------------------------
    // SwingBackend -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set to a new font, and resize the screen to match its dimensions.
     *
     * @param font the new font
     */
    public void setFont(final Font font) {
        ((SwingTerminal) terminal).setFont(font);
    }

    /**
     * Get the number of millis to wait before switching the blink from
     * visible to invisible.
     *
     * @return the number of milli to wait before switching the blink from
     * visible to invisible
     */
    public long getBlinkMillis() {
        return ((SwingTerminal) terminal).getBlinkMillis();
    }

    /**
     * Getter for the underlying Swing component.
     *
     * @return the SwingComponent
     */
    public SwingComponent getSwingComponent() {
        return ((SwingTerminal) terminal).getSwingComponent();
    }

}
