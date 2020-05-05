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

import java.util.ResourceBundle;

import jexer.bits.CellAttributes;
import jexer.event.TResizeEvent;
import jexer.help.THelpText;
import jexer.help.Topic;

/**
 * THelpWindow
 */
public class THelpWindow extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(THelpWindow.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // Default help topic keys.  Note package private access.
    static String HELP_HELP                     = "Help On Help";

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The help text window.
     */
    private THelpText helpText;

    /**
     * The "Contents" button.
     */
    private TButton contentsButton;

    /**
     * The "Index" button.
     */
    private TButton indexButton;

    /**
     * The "Previous" button.
     */
    private TButton previousButton;

    /**
     * The "Close" button.
     */
    private TButton closeButton;

    /**
     * The X position for the buttons.
     */
    private int buttonOffset = 14;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param application TApplication that manages this window
     * @param topic the topic to start on
     */
    public THelpWindow(final TApplication application, final String topic) {
        this (application, application.helpFile.getTopic(topic));
    }

    /**
     * Public constructor.
     *
     * @param application TApplication that manages this window
     * @param topic the topic to start on
     */
    public THelpWindow(final TApplication application, final Topic topic) {
        super(application, i18n.getString("windowTitle"),
            1, 1, 78, 22, CENTERED | RESIZABLE);

        setMinimumWindowHeight(16);
        setMinimumWindowWidth(30);

        helpText = new THelpText(this, topic, 1, 1,
            getWidth() - buttonOffset - 4, getHeight() - 4);

        setHelpTopic(topic);

        // Buttons
        previousButton = addButton(i18n.getString("previousButton"),
            getWidth() - buttonOffset, 4,
            new TAction() {
                public void DO() {
                    if (application.helpTopics.size() > 1) {
                        Topic previous = application.helpTopics.remove(
                            application.helpTopics.size() - 2);
                        application.helpTopics.remove(application.
                            helpTopics.size() - 1);
                        setHelpTopic(previous);
                    }
                }
            });

        contentsButton = addButton(i18n.getString("contentsButton"),
            getWidth() - buttonOffset, 6,
            new TAction() {
                public void DO() {
                    setHelpTopic(application.helpFile.getTableOfContents());
                }
            });

        indexButton = addButton(i18n.getString("indexButton"),
            getWidth() - buttonOffset, 8,
            new TAction() {
                public void DO() {
                    setHelpTopic(application.helpFile.getIndex());
                }
            });

        closeButton = addButton(i18n.getString("closeButton"),
            getWidth() - buttonOffset, 10,
            new TAction() {
                public void DO() {
                    // Don't copy anything, just close the window.
                    THelpWindow.this.close();
                }
            });

        // Save this for last: make the close button default action.
        activate(closeButton);

    }

    /**
     * Public constructor.
     *
     * @param application TApplication that manages this window
     */
    public THelpWindow(final TApplication application) {
        this(application, HELP_HELP);
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle window/screen resize events.
     *
     * @param event resize event
     */
    @Override
    public void onResize(final TResizeEvent event) {
        if (event.getType() == TResizeEvent.Type.WIDGET) {

            previousButton.setX(getWidth() - buttonOffset);
            contentsButton.setX(getWidth() - buttonOffset);
            indexButton.setX(getWidth() - buttonOffset);
            closeButton.setX(getWidth() - buttonOffset);

            helpText.setDimensions(1, 1, getWidth() - buttonOffset - 4,
                getHeight() - 4);
            helpText.onResize(new TResizeEvent(TResizeEvent.Type.WIDGET,
                    helpText.getWidth(), helpText.getHeight()));

            return;
        } else {
            super.onResize(event);
        }
    }

    // ------------------------------------------------------------------------
    // TWindow ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Retrieve the background color.
     *
     * @return the background color
     */
    @Override
    public final CellAttributes getBackground() {
        return getTheme().getColor("thelpwindow.background");
    }

    /**
     * Retrieve the border color.
     *
     * @return the border color
     */
    @Override
    public CellAttributes getBorder() {
        if (inWindowMove) {
            return getTheme().getColor("thelpwindow.windowmove");
        }
        return getTheme().getColor("thelpwindow.background");
    }

    /**
     * Retrieve the color used by the window movement/sizing controls.
     *
     * @return the color used by the zoom box, resize bar, and close box
     */
    @Override
    public CellAttributes getBorderControls() {
        return getTheme().getColor("thelpwindow.border");
    }

    // ------------------------------------------------------------------------
    // THelpWindow ------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the topic to display.
     *
     * @param topic the topic to display
     */
    public void setHelpTopic(final String topic) {
        setHelpTopic(getApplication().helpFile.getTopic(topic));
    }

    /**
     * Set the topic to display.
     *
     * @param topic the topic to display
     */
    private void setHelpTopic(final Topic topic) {
        boolean separator = true;
        if ((topic == getApplication().helpFile.getTableOfContents())
            || (topic == getApplication().helpFile.getIndex())
        ) {
            separator = false;
        }

        getApplication().helpTopics.add(topic);
        helpText.setTopic(topic, separator);
    }

}
