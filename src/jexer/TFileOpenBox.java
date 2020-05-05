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
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import jexer.backend.SwingTerminal;
import jexer.bits.GraphicsChars;
import jexer.event.TKeypressEvent;
import jexer.ttree.TDirectoryTreeItem;
import jexer.ttree.TTreeItem;
import jexer.ttree.TTreeViewWidget;
import static jexer.TKeypress.*;

/**
 * TFileOpenBox is a system-modal dialog for selecting a file to open.  Call
 * it like:
 *
 * <pre>
 * {@code
 *     filename = fileOpenBox("/path/to/file.ext",
 *         TFileOpenBox.Type.OPEN);
 *     if (filename != null) {
 *         ... the user selected a file, go open it ...
 *     }
 * }
 * </pre>
 *
 */
public class TFileOpenBox extends TWindow {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(TFileOpenBox.class.getName());

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * TFileOpenBox can be called for either Open or Save actions.
     */
    public enum Type {
        /**
         * Button will be labeled "Open".
         */
        OPEN,

        /**
         * Button will be labeled "Save".
         */
        SAVE,

        /**
         * Button will be labeled "Select".
         */
        SELECT
    }

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * String to return, or null if the user canceled.
     */
    private String filename = null;

    /**
     * The left-side tree view pane.
     */
    private TTreeViewWidget treeView;

    /**
     * The data behind treeView.
     */
    private TDirectoryTreeItem treeViewRoot;

    /**
     * The right-side directory list pane.
     */
    private TDirectoryList directoryList;

    /**
     * The top row text field.
     */
    private TField entryField;

    /**
     * The Open or Save button.
     */
    private TButton openButton;

    /**
     * The type of box this is (OPEN, SAVE, or SELECT).
     */
    private Type type = Type.OPEN;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.  The file open box will be centered on screen.
     *
     * @param application the TApplication that manages this window
     * @param path path of selected file
     * @param type one of the Type constants
     * @throws IOException of a java.io operation throws
     */
    public TFileOpenBox(final TApplication application, final String path,
        final Type type) throws IOException {

        this(application, path, type, null);
    }

    /**
     * Public constructor.  The file open box will be centered on screen.
     *
     * @param application the TApplication that manages this window
     * @param path path of selected file
     * @param type one of the Type constants
     * @param filters a list of strings that files must match to be displayed
     * @throws IOException of a java.io operation throws
     */
    public TFileOpenBox(final TApplication application, final String path,
        final Type type, final List<String> filters) throws IOException {

        // Register with the TApplication
        super(application, "", 0, 0, 76, 22, MODAL);

        // Add text field
        entryField = addField(1, 1, getWidth() - 4, false,
            (new File(path)).getCanonicalPath(),
            new TAction() {
                public void DO() {
                    try {
                        checkFilename(entryField.getText());
                    } catch (IOException e) {
                        // If the backend is Swing, we can emit the stack
                        // trace to stderr.  Otherwise, just squash it.
                        if (getScreen() instanceof SwingTerminal) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);
        entryField.onKeypress(new TKeypressEvent(kbEnd));

        // Add directory treeView
        treeView = addTreeViewWidget(1, 3, 30, getHeight() - 6,
            new TAction() {
                public void DO() {
                    TTreeItem item = treeView.getSelected();
                    File selectedDir = ((TDirectoryTreeItem) item).getFile();
                    try {
                        directoryList.setPath(selectedDir.getCanonicalPath());
                        entryField.setText(selectedDir.getCanonicalPath());
                        if (type == Type.OPEN) {
                            openButton.setEnabled(false);
                        }
                        activate(treeView);
                    } catch (IOException e) {
                        // If the backend is Swing, we can emit the stack
                        // trace to stderr.  Otherwise, just squash it.
                        if (getScreen() instanceof SwingTerminal) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        );
        treeViewRoot = new TDirectoryTreeItem(treeView, path, true);

        // Add directory files list
        directoryList = addDirectoryList(path, 34, 3, 28, getHeight() - 6,
            new TAction() {
                public void DO() {
                    try {
                        File newPath = directoryList.getPath();
                        entryField.setText(newPath.getCanonicalPath());
                        entryField.onKeypress(new TKeypressEvent(kbEnd));
                        openButton.setEnabled(true);
                        activate(entryField);
                        checkFilename(entryField.getText());
                    } catch (IOException e) {
                        // If the backend is Swing, we can emit the stack
                        // trace to stderr.  Otherwise, just squash it.
                        if (getScreen() instanceof SwingTerminal) {
                            e.printStackTrace();
                        }
                    }
                }
            },
            new TAction() {
                public void DO() {
                    try {
                        File newPath = directoryList.getPath();
                        entryField.setText(newPath.getCanonicalPath());
                        entryField.onKeypress(new TKeypressEvent(kbEnd));
                        openButton.setEnabled(true);
                        activate(entryField);
                    } catch (IOException e) {
                        // If the backend is Swing, we can emit the stack
                        // trace to stderr.  Otherwise, just squash it.
                        if (getScreen() instanceof SwingTerminal) {
                            e.printStackTrace();
                        }
                    }
                }
            },
            filters);

        String openLabel = "";
        switch (type) {
        case OPEN:
            openLabel = i18n.getString("openButton");
            setTitle(i18n.getString("openTitle"));
            break;
        case SAVE:
            openLabel = i18n.getString("saveButton");
            setTitle(i18n.getString("saveTitle"));
            break;
        case SELECT:
            openLabel = i18n.getString("selectButton");
            setTitle(i18n.getString("selectTitle"));
            break;
        default:
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        this.type = type;

        // Setup button actions
        openButton = addButton(openLabel, this.getWidth() - 12, 3,
            new TAction() {
                public void DO() {
                    try {
                        checkFilename(entryField.getText());
                    } catch (IOException e) {
                        // If the backend is Swing, we can emit the stack
                        // trace to stderr.  Otherwise, just squash it.
                        if (getScreen() instanceof SwingTerminal) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        );
        if (type == Type.OPEN) {
            openButton.setEnabled(false);
        }

        addButton(i18n.getString("cancelButton"), getWidth() - 12, 5,
            new TAction() {
                public void DO() {
                    filename = null;
                    getApplication().closeWindow(TFileOpenBox.this);
                }
            }
        );

        // Default to the directory list
        activate(directoryList);

        // Set the secondaryFiber to run me
        getApplication().enableSecondaryEventReceiver(this);

        // Yield to the secondary thread.  When I come back from the
        // constructor response will already be set.
        getApplication().yield();
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        // Escape - behave like cancel
        if (keypress.equals(kbEsc)) {
            // Close window
            filename = null;
            getApplication().closeWindow(this);
            return;
        }

        if (treeView.isActive()) {
            if ((keypress.equals(kbEnter))
                || (keypress.equals(kbUp))
                || (keypress.equals(kbDown))
                || (keypress.equals(kbPgUp))
                || (keypress.equals(kbPgDn))
                || (keypress.equals(kbHome))
                || (keypress.equals(kbEnd))
            ) {
                // Tree view will be changing, update the directory list.
                super.onKeypress(keypress);

                // This is the same action as treeView's enter.
                TTreeItem item = treeView.getSelected();
                File selectedDir = ((TDirectoryTreeItem) item).getFile();
                try {
                    directoryList.setPath(selectedDir.getCanonicalPath());
                    if (type == Type.OPEN) {
                        openButton.setEnabled(false);
                    }
                    activate(treeView);
                } catch (IOException e) {
                    // If the backend is Swing, we can emit the stack trace
                    // to stderr.  Otherwise, just squash it.
                    if (getScreen() instanceof SwingTerminal) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        }

        // Pass to my parent
        super.onKeypress(keypress);
    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw me on screen.
     */
    @Override
    public void draw() {
        super.draw();
        vLineXY(33, 4, getHeight() - 6, GraphicsChars.WINDOW_SIDE,
            getBackground());
    }

    // ------------------------------------------------------------------------
    // TFileOpenBox -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the return string.
     *
     * @return the filename the user selected, or null if they canceled.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * See if there is a valid filename to return.  If the filename is a
     * directory, then
     *
     * @param newFilename the filename to check and return
     * @throws IOException of a java.io operation throws
     */
    private void checkFilename(final String newFilename) throws IOException {
        File newFile = new File(newFilename);
        if (newFile.exists()) {
            if (newFile.isFile() || (type == Type.SELECT)) {
                filename = newFilename;
                getApplication().closeWindow(this);
                return;
            }
            if (newFile.isDirectory()) {
                treeViewRoot = new TDirectoryTreeItem(treeView,
                    newFilename, true);
                treeView.setTreeRoot(treeViewRoot, true);
                if (type == Type.OPEN) {
                    openButton.setEnabled(false);
                }
                directoryList.setPath(newFilename);
            }
        } else if (type != Type.OPEN) {
            filename = newFilename;
            getApplication().closeWindow(this);
            return;
        }
    }

}
