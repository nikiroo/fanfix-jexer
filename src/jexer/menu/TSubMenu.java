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
package jexer.menu;

import jexer.TKeypress;
import jexer.TWidget;
import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.event.TKeypressEvent;
import static jexer.TKeypress.*;

/**
 * TSubMenu is a special case menu item that wraps another TMenu.
 */
public class TSubMenu extends TMenuItem {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The menu window.  Note package private access.
     */
    TMenu menu;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Package private constructor.
     *
     * @param parent parent widget
     * @param title menu title.  Title must contain a keyboard shortcut,
     * denoted by prefixing a letter with "&amp;", e.g. "&amp;File"
     * @param x column relative to parent
     * @param y row relative to parent
     */
    TSubMenu(final TMenu parent, final String title, final int x, final int y) {
        super(parent, TMenu.MID_UNUSED, x, y, title);

        setActive(false);
        setEnabled(true);

        this.menu = new TMenu(parent.getApplication(), x, getAbsoluteY() - 1,
            title);
        setWidth(menu.getWidth() + 2);

        this.menu.isSubMenu = true;
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

        // Open me if they hit my mnemonic.
        if (!keypress.getKey().isFnKey()
            && !keypress.getKey().isAlt()
            && !keypress.getKey().isCtrl()
            && (getMnemonic() != null)
            && (Character.toLowerCase(getMnemonic().getShortcut())
                == Character.toLowerCase(keypress.getKey().getChar()))
        ) {
            dispatch();
            return;
        }

        if (menu.isActive()) {
            menu.onKeypress(keypress);
            return;
        }

        if (keypress.equals(kbEnter)) {
            dispatch();
            return;
        }

        if (keypress.equals(kbRight)) {
            dispatch();
            return;
        }

        if (keypress.equals(kbDown)) {
            getParent().switchWidget(true);
            return;
        }

        if (keypress.equals(kbUp)) {
            getParent().switchWidget(false);
            return;
        }

        if (keypress.equals(kbLeft)) {
            TMenu parentMenu = (TMenu) getParent();
            if (parentMenu.isSubMenu) {
                getApplication().closeSubMenu();
            } else {
                getApplication().switchMenu(false);
            }
            return;
        }

        if (keypress.equals(kbEsc)) {
            getApplication().closeMenu();
            return;
        }
    }

    // ------------------------------------------------------------------------
    // TMenuItem --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the menu title.
     */
    @Override
    public void draw() {
        super.draw();

        CellAttributes menuColor;
        if (isAbsoluteActive()) {
            menuColor = getTheme().getColor("tmenu.highlighted");
        } else {
            if (isEnabled()) {
                menuColor = getTheme().getColor("tmenu");
            } else {
                menuColor = getTheme().getColor("tmenu.disabled");
            }
        }

        // Add the arrow
        putCharXY(getWidth() - 2, 0, GraphicsChars.CP437[0x10], menuColor);
    }

    /**
     * Override dispatch() to do nothing.
     */
    @Override
    public void dispatch() {
        assert (isEnabled());
        if (isAbsoluteActive()) {
            if (!menu.isActive()) {
                getApplication().addSubMenu(menu);
                menu.setActive(true);
            }
        }
    }

    /**
     * Returns my active widget.
     *
     * @return widget that is active, or this if no children
     */
    @Override
    public TWidget getActiveChild() {
        if (menu.isActive()) {
            return menu;
        }
        // Menu not active, return me
        return this;
    }

    // ------------------------------------------------------------------------
    // TSubMenu ---------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Convenience function to add a custom menu item.
     *
     * @param id menu item ID.  Must be greater than 1024.
     * @param label menu item label
     * @param key global keyboard accelerator
     * @return the new menu item
     */
    public TMenuItem addItem(final int id, final String label,
        final TKeypress key) {

        return menu.addItem(id, label, key);
    }

    /**
     * Convenience function to add a custom menu item.
     *
     * @param id menu item ID.  Must be greater than 1024.
     * @param label menu item label
     * @param key global keyboard accelerator
     * @param enabled default state for enabled
     * @return the new menu item
     */
    public TMenuItem addItem(final int id, final String label,
        final TKeypress key, final boolean enabled) {

        return menu.addItem(id, label, key, enabled);
    }

    /**
     * Convenience function to add a menu item.
     *
     * @param id menu item ID.  Must be greater than 1024.
     * @param label menu item label
     * @return the new menu item
     */
    public TMenuItem addItem(final int id, final String label) {
        return menu.addItem(id, label);
    }

    /**
     * Convenience function to add a menu item.
     *
     * @param id menu item ID.  Must be greater than 1024.
     * @param label menu item label
     * @param enabled default state for enabled
     * @return the new menu item
     */
    public TMenuItem addItem(final int id, final String label,
        final boolean enabled) {

        return menu.addItem(id, label, enabled);
    }

    /**
     * Convenience function to add one of the default menu items.
     *
     * @param id menu item ID.  Must be between 0 (inclusive) and 1023
     * (inclusive).
     * @return the new menu item
     */
    public TMenuItem addDefaultItem(final int id) {
        return menu.addDefaultItem(id);
    }

    /**
     * Convenience function to add one of the default menu items.
     *
     * @param id menu item ID.  Must be between 0 (inclusive) and 1023
     * (inclusive).
     * @param enabled default state for enabled
     * @return the new menu item
     */
    public TMenuItem addDefaultItem(final int id, final boolean enabled) {
        return menu.addDefaultItem(id, enabled);
    }

    /**
     * Convenience function to add a menu separator.
     */
    public void addSeparator() {
        menu.addSeparator();
    }

    /**
     * Convenience function to add a sub-menu.
     *
     * @param title menu title.  Title must contain a keyboard shortcut,
     * denoted by prefixing a letter with "&amp;", e.g. "&amp;File"
     * @return the new sub-menu
     */
    public TSubMenu addSubMenu(final String title) {
        return menu.addSubMenu(title);
    }

}
