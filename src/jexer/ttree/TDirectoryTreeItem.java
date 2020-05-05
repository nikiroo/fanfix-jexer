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
package jexer.ttree;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import jexer.TWidget;

/**
 * TDirectoryTreeItem is a single item in a disk directory tree view.
 */
public class TDirectoryTreeItem extends TTreeItem {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * File corresponding to this list item.
     */
    private File file;

    /**
     * The TTreeViewWidget containing this directory tree.
     */
    private TTreeViewWidget treeViewWidget;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param view root TTreeViewWidget
     * @param text text for this item
     * @param expanded if true, have it expanded immediately
     * @throws IOException if a java.io operation throws
     */
    public TDirectoryTreeItem(final TTreeViewWidget view, final String text,
        final boolean expanded) throws IOException {

        this(view, text, expanded, true);
    }

    /**
     * Public constructor.
     *
     * @param view root TTreeViewWidget
     * @param text text for this item
     * @param expanded if true, have it expanded immediately
     * @param openParents if true, expand all paths up the root path and
     * return the root path entry
     * @throws IOException if a java.io operation throws
     */
    public TDirectoryTreeItem(final TTreeViewWidget view, final String text,
        final boolean expanded, final boolean openParents) throws IOException {

        super(view.getTreeView(), text, false);

        this.treeViewWidget = view;

        List<String> parentFiles = new LinkedList<String>();
        boolean oldExpanded = expanded;

        // Convert to canonical path
        File rootFile = new File(text);
        rootFile = rootFile.getCanonicalFile();

        if (openParents) {
            setExpanded(true);

            // Go up the directory tree
            File parent = rootFile.getParentFile();
            while (parent != null) {
                parentFiles.add(rootFile.getName());
                rootFile = rootFile.getParentFile();
                parent = rootFile.getParentFile();
            }
        }
        file = rootFile;
        if (rootFile.getParentFile() == null) {
            // This is a filesystem root, use its full name
            setText(rootFile.getCanonicalPath());
        } else {
            // This is a relative path.  We got here because openParents was
            // false.
            assert (!openParents);
            setText(rootFile.getName());
        }
        onExpand();

        if (openParents) {
            TDirectoryTreeItem childFile = this;
            Collections.reverse(parentFiles);
            for (String p: parentFiles) {
                for (TWidget widget: childFile.getChildren()) {
                    TDirectoryTreeItem child = (TDirectoryTreeItem) widget;
                    if (child.getText().equals(p)) {
                        childFile = child;
                        childFile.setExpanded(true);
                        childFile.onExpand();
                        break;
                    }
                }
            }
            unselect();
            getTreeView().setSelected(childFile, true);
            setExpanded(oldExpanded);
        }

        view.reflowData();
    }

    // ------------------------------------------------------------------------
    // TTreeItem --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the File corresponding to this list item.
     *
     * @return the File
     */
    public final File getFile() {
        return file;
    }

    /**
     * Called when this item is expanded or collapsed.  this.expanded will be
     * true if this item was just expanded from a mouse click or keypress.
     */
    @Override
    public final void onExpand() {
        // System.err.printf("onExpand() %s\n", file);

        if (file == null) {
            return;
        }
        getChildren().clear();

        // Make sure we can read it before trying to.
        if (file.canRead()) {
            setSelectable(true);
        } else {
            setSelectable(false);
        }
        assert (file.isDirectory());
        setExpandable(true);

        if (!isExpanded() || !isExpandable()) {
            return;
        }

        File [] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File f: listFiles) {
                // System.err.printf("   -> file %s %s\n", file, file.getName());

                if (f.getName().startsWith(".")) {
                    // Hide dot-files
                    continue;
                }
                if (!f.isDirectory()) {
                    continue;
                }

                try {
                    TDirectoryTreeItem item = new TDirectoryTreeItem(treeViewWidget,
                        f.getCanonicalPath(), false, false);

                    item.level = this.level + 1;
                    getChildren().add(item);
                } catch (IOException e) {
                    continue;
                }
            }
        }
        Collections.sort(getChildren());
    }

}
