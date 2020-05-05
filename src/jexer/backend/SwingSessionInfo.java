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

import java.awt.Insets;

/**
 * SwingSessionInfo provides a session implementation with a callback into
 * Swing to support queryWindowSize().  The username is blank, language is
 * "en_US", with a 80x25 text window.
 */
public class SwingSessionInfo implements SessionInfo {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The Swing JFrame or JComponent.
     */
    private SwingComponent swing;

    /**
     * The width of a text cell in pixels.
     */
    private int textWidth = 10;

    /**
     * The height of a text cell in pixels.
     */
    private int textHeight = 10;

    /**
     * User name.
     */
    private String username = "";

    /**
     * Language.
     */
    private String language = "en_US";

    /**
     * Text window width.
     */
    private int windowWidth = 80;

    /**
     * Text window height.
     */
    private int windowHeight = 25;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param swing the Swing JFrame or JComponent
     * @param textWidth the width of a cell in pixels
     * @param textHeight the height of a cell in pixels
     */
    public SwingSessionInfo(final SwingComponent swing, final int textWidth,
        final int textHeight) {

        this.swing      = swing;
        this.textWidth  = textWidth;
        this.textHeight = textHeight;
    }

    /**
     * Public constructor.
     *
     * @param swing the Swing JFrame or JComponent
     * @param textWidth the width of a cell in pixels
     * @param textHeight the height of a cell in pixels
     * @param width the number of columns
     * @param height the number of rows
     */
    public SwingSessionInfo(final SwingComponent swing, final int textWidth,
        final int textHeight, final int width, final int height) {

        this.swing              = swing;
        this.textWidth          = textWidth;
        this.textHeight         = textHeight;
        this.windowWidth        = width;
        this.windowHeight       = height;
    }

    // ------------------------------------------------------------------------
    // SessionInfo ------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Username getter.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Username setter.
     *
     * @param username the value
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Language getter.
     *
     * @return the language
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Language setter.
     *
     * @param language the value
     */
    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
     * Text window width getter.
     *
     * @return the window width
     */
    public int getWindowWidth() {
        return windowWidth;
    }

    /**
     * Text window height getter.
     *
     * @return the window height
     */
    public int getWindowHeight() {
        return windowHeight;
    }

    /**
     * Re-query the text window size.
     */
    public void queryWindowSize() {
        Insets insets = swing.getInsets();
        int width = swing.getWidth() - insets.left - insets.right;
        int height = swing.getHeight() - insets.top - insets.bottom;
        // In theory, if Java reported pixel-perfect dimensions, the
        // expressions above would precisely line up with the requested
        // window size from SwingComponent.setDimensions().  In practice,
        // there appears to be a small difference.  Add half a text cell in
        // both directions before the division to hopefully reach the same
        // result as setDimensions() was supposed to give us.
        width += (textWidth / 2);
        height += (textHeight / 2);
        windowWidth = width / textWidth;
        windowHeight = height / textHeight;

        /*
        System.err.printf("queryWindowSize(): frame %d %d window %d %d\n",
            swing.getWidth(), swing.getHeight(),
            windowWidth, windowHeight);
        */
    }

    // ------------------------------------------------------------------------
    // SwingSessionInfo -------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the dimensions of a single text cell.
     *
     * @param textWidth the width of a cell in pixels
     * @param textHeight the height of a cell in pixels
     */
    public void setTextCellDimensions(final int textWidth,
        final int textHeight) {

        this.textWidth  = textWidth;
        this.textHeight = textHeight;
    }

    /**
     * Getter for the underlying Swing component.
     *
     * @return the SwingComponent
     */
    public SwingComponent getSwingComponent() {
        return swing;
    }

}
