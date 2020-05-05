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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jexer.bits.StringUtils;

/**
 * TDirectoryList shows the files within a directory.
 */
public class TDirectoryList extends TList {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Files in the directory.
     */
    private Map<String, File> files;

    /**
     * Root path containing files to display.
     */
    private File path;

    /**
     * The list of filters that a file must match in order to be displayed.
     */
    private List<String> filters;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     */
    public TDirectoryList(final TWidget parent, final String path, final int x,
        final int y, final int width, final int height) {

        this(parent, path, x, y, width, height, null, null, null);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param action action to perform when an item is selected (enter or
     * double-click)
     */
    public TDirectoryList(final TWidget parent, final String path, final int x,
        final int y, final int width, final int height, final TAction action) {

        this(parent, path, x, y, width, height, action, null, null);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param action action to perform when an item is selected (enter or
     * double-click)
     * @param singleClickAction action to perform when an item is selected
     * (single-click)
     */
    public TDirectoryList(final TWidget parent, final String path, final int x,
        final int y, final int width, final int height, final TAction action,
        final TAction singleClickAction) {

        this(parent, path, x, y, width, height, action, singleClickAction,
            null);
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param path directory path, must be a directory
     * @param x column relative to parent
     * @param y row relative to parent
     * @param width width of text area
     * @param height height of text area
     * @param action action to perform when an item is selected (enter or
     * double-click)
     * @param singleClickAction action to perform when an item is selected
     * (single-click)
     * @param filters a list of strings that files must match to be displayed
     */
    public TDirectoryList(final TWidget parent, final String path, final int x,
        final int y, final int width, final int height, final TAction action,
        final TAction singleClickAction, final List<String> filters) {

        super(parent, null, x, y, width, height, action);
        files = new HashMap<String, File>();
        this.filters = filters;
        this.singleClickAction = singleClickAction;

        setPath(path);
    }

    // ------------------------------------------------------------------------
    // TList ------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // TDirectoryList ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Set the new path to display.
     *
     * @param path new path to list files for
     */
    public void setPath(final String path) {
        this.path = new File(path);

        List<String> newStrings = new ArrayList<String>();
        files.clear();

        // Build a list of files in this directory
        File [] newFiles = this.path.listFiles();
        if (newFiles != null) {
            for (int i = 0; i < newFiles.length; i++) {
                if (newFiles[i].getName().startsWith(".")) {
                    continue;
                }
                if (newFiles[i].isDirectory()) {
                    continue;
                }
                if (filters != null) {
                    for (String pattern: filters) {

                        /*
                        System.err.println("newFiles[i] " +
                            newFiles[i].getName() + " " + pattern +
                            " " + newFiles[i].getName().matches(pattern));
                        */

                        if (newFiles[i].getName().matches(pattern)) {
                            String key = renderFile(newFiles[i]);
                            files.put(key, newFiles[i]);
                            newStrings.add(key);
                            break;
                        }
                    }
                } else {
                    String key = renderFile(newFiles[i]);
                    files.put(key, newFiles[i]);
                    newStrings.add(key);
                }
            }
        }
        setList(newStrings);

        // Select the first entry
        if (getMaxSelectedIndex() >= 0) {
            setSelectedIndex(0);
        }
    }

    /**
     * Get the path that is being displayed.
     *
     * @return the path
     */
    public File getPath() {
        path = files.get(getSelected());
        return path;
    }

    /**
     * Format one of the entries for drawing on the screen.
     *
     * @param file the File
     * @return the line to draw
     */
    private String renderFile(final File file) {
        String name = file.getName();
        if (StringUtils.width(name) > 20) {
            name = name.substring(0, 17) + "...";
        }
        return String.format("%-20s %5dk", name, (file.length() / 1024));
    }

}
