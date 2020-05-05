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
package jexer.demos;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Scanner;

import jexer.TAction;
import jexer.TApplication;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

/**
 * The demo application itself.
 */
public class DesktopDemoApplication extends TApplication {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(DesktopDemoApplication.class.getName());

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param backendType one of the TApplication.BackendType values
     * @throws Exception if TApplication can't instantiate the Backend.
     */
    public DesktopDemoApplication(final BackendType backendType) throws Exception {
        super(backendType);
        addAllWidgets();
        getBackend().setTitle(i18n.getString("applicationTitle"));
    }

    // ------------------------------------------------------------------------
    // TApplication -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Handle menu events.
     *
     * @param menu menu event
     * @return if true, the event was processed and should not be passed onto
     * a window
     */
    @Override
    public boolean onMenu(final TMenuEvent menu) {

        if (menu.getId() == TMenu.MID_OPEN_FILE) {
            try {
                String filename = fileOpenBox(".");
                 if (filename != null) {
                     try {
                         File file = new File(filename);
                         StringBuilder fileContents = new StringBuilder();
                         Scanner scanner = new Scanner(file);
                         String EOL = System.getProperty("line.separator");

                         try {
                             while (scanner.hasNextLine()) {
                                 fileContents.append(scanner.nextLine() + EOL);
                             }
                             new DemoTextWindow(this, filename,
                                 fileContents.toString());
                         } finally {
                             scanner.close();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onMenu(menu);
    }

    // ------------------------------------------------------------------------
    // DesktopDemoApplication -------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Add all the widgets of the demo.
     */
    private void addAllWidgets() {

        // Add the menus
        addFileMenu();
        addEditMenu();
        addWindowMenu();
        addHelpMenu();

        final DesktopDemo desktop = new DesktopDemo(this);
        setDesktop(desktop);

        desktop.addButton(i18n.getString("removeHatch"), 2, 5,
            new TAction() {
                public void DO() {
                    desktop.drawHatch = false;
                }
            }
        );
        desktop.addButton(i18n.getString("showHatch"), 2, 8,
            new TAction() {
                public void DO() {
                    desktop.drawHatch = true;
                }
            }
        );

        final TWindow windowA = addWindow(i18n.getString("windowATitle"),
            25, 14);
        final TWindow windowB = addWindow(i18n.getString("windowBTitle"),
            25, 14);
        windowA.addButton(i18n.getString("showWindowB"), 2, 2,
            new TAction() {
                public void DO() {
                    windowB.show();
                }
            }
        );
        windowA.addButton(i18n.getString("hideWindowB"), 2, 4,
            new TAction() {
                public void DO() {
                    windowB.hide();
                }
            }
        );
        windowA.addButton(i18n.getString("maximizeWindowB"), 2, 6,
            new TAction() {
                public void DO() {
                    windowB.maximize();
                }
            }
        );
        windowA.addButton(i18n.getString("restoreWindowB"), 2, 8,
            new TAction() {
                public void DO() {
                    windowB.restore();
                }
            }
        );
        windowB.addButton(i18n.getString("showWindowA"), 2, 2,
            new TAction() {
                public void DO() {
                    windowA.show();
                }
            }
        );
        windowB.addButton(i18n.getString("hideWindowA"), 2, 4,
            new TAction() {
                public void DO() {
                    windowA.hide();
                }
            }
        );
        windowB.addButton(i18n.getString("maximizeWindowA"), 2, 6,
            new TAction() {
                public void DO() {
                    windowA.maximize();
                }
            }
        );
        windowB.addButton(i18n.getString("restoreWindowA"), 2, 8,
            new TAction() {
                public void DO() {
                    windowA.restore();
                }
            }
        );

        desktop.addButton(i18n.getString("showWindowB"), 25, 2,
            new TAction() {
                public void DO() {
                    windowB.show();
                }
            }
        );
        desktop.addButton(i18n.getString("hideWindowB"), 25, 5,
            new TAction() {
                public void DO() {
                    windowB.hide();
                }
            }
        );
        desktop.addButton(i18n.getString("showWindowA"), 25, 8,
            new TAction() {
                public void DO() {
                    windowA.show();
                }
            }
        );
        desktop.addButton(i18n.getString("hideWindowA"), 25, 11,
            new TAction() {
                public void DO() {
                    windowA.hide();
                }
            }
        );
        desktop.addButton(i18n.getString("createWindowC"), 25, 15,
            new TAction() {
                public void DO() {
                    final TWindow windowC = desktop.getApplication().addWindow(
                        i18n.getString("windowCTitle"), 30, 20,
                        TWindow.NOCLOSEBOX);
                    windowC.addButton(i18n.getString("closeMe"), 5, 5,
                        new TAction() {
                            public void DO() {
                                windowC.close();
                            }
                        }
                    );
                }
            }
        );

        desktop.addButton(i18n.getString("enableFFM"), 25, 18,
            new TAction() {
                public void DO() {
                    DesktopDemoApplication.this.setFocusFollowsMouse(true);
                }
            }
        );
        desktop.addButton(i18n.getString("disableFFM"), 25, 21,
            new TAction() {
                public void DO() {
                    DesktopDemoApplication.this.setFocusFollowsMouse(false);
                }
            }
        );
    }

}
