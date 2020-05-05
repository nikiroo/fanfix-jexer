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
package jexer.bits;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * ColorTheme is a collection of colors keyed by string.  A default theme is
 * also provided that matches the blue-and-white theme used by Turbo Vision.
 */
public class ColorTheme {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The current theme colors.
     */
    private SortedMap<String, CellAttributes> colors;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor sets the theme to the default.
     */
    public ColorTheme() {
        colors = new TreeMap<String, CellAttributes>();
        setDefaultTheme();
    }

    // ------------------------------------------------------------------------
    // ColorTheme -------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Retrieve the CellAttributes for a named theme color.
     *
     * @param name theme color name, e.g. "twindow.border"
     * @return color associated with name, e.g. bold yellow on blue
     */
    public CellAttributes getColor(final String name) {
        CellAttributes attr = colors.get(name);
        return attr;
    }

    /**
     * Retrieve all the names in the theme.
     *
     * @return a list of names
     */
    public List<String> getColorNames() {
        Set<String> keys = colors.keySet();
        List<String> names = new ArrayList<String>(keys.size());
        names.addAll(keys);
        return names;
    }

    /**
     * Set the color for a named theme color.
     *
     * @param name theme color name, e.g. "twindow.border"
     * @param color the new color to associate with name, e.g. bold yellow on
     * blue
     */
    public void setColor(final String name, final CellAttributes color) {
        colors.put(name, color);
    }

    /**
     * Save the color theme mappings to an ASCII file.
     *
     * @param filename file to write to
     * @throws IOException if the I/O fails
     */
    public void save(final String filename) throws IOException {
        FileWriter file = new FileWriter(filename);
        for (String key: colors.keySet()) {
            CellAttributes color = getColor(key);
            file.write(String.format("%s = %s\n", key, color));
        }
        file.close();
    }

    /**
     * Read color theme mappings from an ASCII file.
     *
     * @param filename file to read from
     * @throws IOException if the I/O fails
     */
    public void load(final String filename) throws IOException {
        load(new FileReader(filename));
    }

    /**
     * Set a color based on a text string.  Color text string is of the form:
     * <code>[ bold ] [ blink ] { foreground on background }</code>
     *
     * @param key the color key string
     * @param text the text string
     */
    public void setColorFromString(final String key, final String text) {
        boolean bold = false;
        boolean blink = false;
        String foreColor;
        String backColor;
        String token;

        StringTokenizer tokenizer = new StringTokenizer(text);
        token = tokenizer.nextToken();

        if (token.toLowerCase().equals("rgb:")) {
            // Foreground
            int foreColorRGB = -1;
            try {
                foreColorRGB = Integer.parseInt(tokenizer.nextToken(), 16);
            } catch (NumberFormatException e) {
                // Default to white on black
                foreColorRGB = 0xFFFFFF;
            }

            // "on"
            if (!tokenizer.nextToken().toLowerCase().equals("on")) {
                // Invalid line.
                return;
            }

            // Background
            int backColorRGB = -1;
            try {
                backColorRGB = Integer.parseInt(tokenizer.nextToken(), 16);
            } catch (NumberFormatException e) {
                backColorRGB = 0;
            }

            CellAttributes color = new CellAttributes();
            color.setForeColorRGB(foreColorRGB);
            color.setBackColorRGB(backColorRGB);
            colors.put(key, color);
            return;
        }

        while (token.equals("bold")
            || token.equals("bright")
            || token.equals("blink")
        ) {
            if (token.equals("bold") || token.equals("bright")) {
                bold = true;
                token = tokenizer.nextToken();
            }
            if (token.equals("blink")) {
                blink = true;
                token = tokenizer.nextToken();
            }
        }

        // What's left is "blah on blah"
        foreColor = token.toLowerCase();

        if (!tokenizer.nextToken().toLowerCase().equals("on")) {
            // Invalid line.
            return;
        }
        backColor = tokenizer.nextToken().toLowerCase();

        CellAttributes color = new CellAttributes();
        if (bold) {
            color.setBold(true);
        }
        if (blink) {
            color.setBlink(true);
        }
        color.setForeColor(Color.getColor(foreColor));
        color.setBackColor(Color.getColor(backColor));
        colors.put(key, color);
    }

    /**
     * Read color theme mappings from a Reader.  The reader is closed at the
     * end.
     *
     * @param reader the reader to read from
     * @throws IOException if the I/O fails
     */
    public void load(final Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        for (; line != null; line = bufferedReader.readLine()) {
            // Look for lines that resemble:
            //     "key = blah on blah"
            //     "key = bold blah on blah"
            //     "key = blink bold blah on blah"
            //     "key = bold blink blah on blah"
            //     "key = blink blah on blah"
            if (line.indexOf('=') == -1) {
                // Invalid line.
                continue;
            }
            String key = line.substring(0, line.indexOf('=')).trim();
            String text = line.substring(line.indexOf('=') + 1);
            setColorFromString(key, text);
        }
        // All done.
        bufferedReader.close();
    }

    /**
     * Sets to defaults that resemble the Borland IDE colors.
     */
    public void setDefaultTheme() {
        CellAttributes color;

        // TWindow border
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("twindow.border", color);

        // TWindow background
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("twindow.background", color);

        // TWindow border - inactive
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("twindow.border.inactive", color);

        // TWindow background - inactive
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("twindow.background.inactive", color);

        // TWindow border - modal
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.WHITE);
        color.setBold(true);
        colors.put("twindow.border.modal", color);

        // TWindow background - modal
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("twindow.background.modal", color);

        // TWindow border - modal + inactive
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(true);
        colors.put("twindow.border.modal.inactive", color);

        // TWindow background - modal + inactive
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("twindow.background.modal.inactive", color);

        // TWindow border - during window movement - modal
        color = new CellAttributes();
        color.setForeColor(Color.GREEN);
        color.setBackColor(Color.WHITE);
        color.setBold(true);
        colors.put("twindow.border.modal.windowmove", color);

        // TWindow border - during window movement
        color = new CellAttributes();
        color.setForeColor(Color.GREEN);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("twindow.border.windowmove", color);

        // TWindow background - during window movement
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("twindow.background.windowmove", color);

        // TDesktop background
        color = new CellAttributes();
        color.setForeColor(Color.BLUE);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tdesktop.background", color);

        // TButton text
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.GREEN);
        color.setBold(false);
        colors.put("tbutton.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.CYAN);
        color.setBackColor(Color.GREEN);
        color.setBold(true);
        colors.put("tbutton.active", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(true);
        colors.put("tbutton.disabled", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.GREEN);
        color.setBold(true);
        colors.put("tbutton.mnemonic", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.GREEN);
        color.setBold(true);
        colors.put("tbutton.mnemonic.highlighted", color);

        // TLabel text
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("tlabel", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("tlabel.mnemonic", color);

        // TText text
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("ttext", color);

        // TField text
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tfield.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("tfield.active", color);

        // TCheckBox
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tcheckbox.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLACK);
        color.setBold(true);
        colors.put("tcheckbox.active", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("tcheckbox.mnemonic", color);
        color = new CellAttributes();
        color.setForeColor(Color.RED);
        color.setBackColor(Color.BLACK);
        color.setBold(true);
        colors.put("tcheckbox.mnemonic.highlighted", color);

        // TComboBox
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tcombobox.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLUE);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("tcombobox.active", color);

        // TSpinner
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tspinner.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLUE);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("tspinner.active", color);

        // TCalendar
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tcalendar.background", color);
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tcalendar.day", color);
        color = new CellAttributes();
        color.setForeColor(Color.RED);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tcalendar.day.selected", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLUE);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("tcalendar.arrow", color);
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("tcalendar.title", color);

        // TRadioButton
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tradiobutton.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLACK);
        color.setBold(true);
        colors.put("tradiobutton.active", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("tradiobutton.mnemonic", color);
        color = new CellAttributes();
        color.setForeColor(Color.RED);
        color.setBackColor(Color.BLACK);
        color.setBold(true);
        colors.put("tradiobutton.mnemonic.highlighted", color);

        // TRadioGroup
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tradiogroup.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("tradiogroup.active", color);

        // TMenu
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tmenu", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.GREEN);
        color.setBold(false);
        colors.put("tmenu.highlighted", color);
        color = new CellAttributes();
        color.setForeColor(Color.RED);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tmenu.mnemonic", color);
        color = new CellAttributes();
        color.setForeColor(Color.RED);
        color.setBackColor(Color.GREEN);
        color.setBold(false);
        colors.put("tmenu.mnemonic.highlighted", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(true);
        colors.put("tmenu.disabled", color);

        // TProgressBar
        color = new CellAttributes();
        color.setForeColor(Color.BLUE);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("tprogressbar.complete", color);
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tprogressbar.incomplete", color);

        // THScroller / TVScroller
        color = new CellAttributes();
        color.setForeColor(Color.CYAN);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tscroller.bar", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLUE);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("tscroller.arrows", color);

        // TTreeView
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("ttreeview", color);
        color = new CellAttributes();
        color.setForeColor(Color.GREEN);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("ttreeview.expandbutton", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("ttreeview.selected", color);
        color = new CellAttributes();
        color.setForeColor(Color.RED);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("ttreeview.unreadable", color);
        color = new CellAttributes();
        // color.setForeColor(Color.BLACK);
        // color.setBackColor(Color.BLUE);
        // color.setBold(true);
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("ttreeview.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("ttreeview.selected.inactive", color);

        // TList
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tlist", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("tlist.selected", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("tlist.unreadable", color);
        color = new CellAttributes();
        // color.setForeColor(Color.BLACK);
        // color.setBackColor(Color.BLUE);
        // color.setBold(true);
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tlist.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tlist.selected.inactive", color);

        // TStatusBar
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tstatusbar.text", color);
        color = new CellAttributes();
        color.setForeColor(Color.RED);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("tstatusbar.button", color);
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tstatusbar.selected", color);

        // TEditor
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("teditor", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("teditor.selected", color);

        // TTable
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("ttable.inactive", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.CYAN);
        color.setBold(false);
        colors.put("ttable.active", color);
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.CYAN);
        color.setBold(true);
        colors.put("ttable.selected", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLACK);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("ttable.label", color);
        color = new CellAttributes();
        color.setForeColor(Color.BLUE);
        color.setBackColor(Color.WHITE);
        color.setBold(false);
        colors.put("ttable.label.selected", color);
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("ttable.border", color);

        // TSplitPane
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("tsplitpane", color);

        // THelpWindow border - during window movement
        color = new CellAttributes();
        color.setForeColor(Color.GREEN);
        color.setBackColor(Color.CYAN);
        color.setBold(true);
        colors.put("thelpwindow.windowmove", color);

        // THelpWindow border
        color = new CellAttributes();
        color.setForeColor(Color.GREEN);
        color.setBackColor(Color.CYAN);
        color.setBold(true);
        colors.put("thelpwindow.border", color);

        // THelpWindow background
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.CYAN);
        color.setBold(true);
        colors.put("thelpwindow.background", color);

        // THelpWindow text
        color = new CellAttributes();
        color.setForeColor(Color.WHITE);
        color.setBackColor(Color.BLUE);
        color.setBold(false);
        colors.put("thelpwindow.text", color);

        // THelpWindow link
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.BLUE);
        color.setBold(true);
        colors.put("thelpwindow.link", color);

        // THelpWindow link - active
        color = new CellAttributes();
        color.setForeColor(Color.YELLOW);
        color.setBackColor(Color.CYAN);
        color.setBold(true);
        colors.put("thelpwindow.link.active", color);

    }

    /**
     * Make human-readable description of this Cell.
     *
     * @return displayable String
     */
    @Override
    public String toString() {
        return colors.toString();
    }

}
