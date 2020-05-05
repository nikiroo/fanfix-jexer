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

import java.util.Calendar;
import java.util.GregorianCalendar;

import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.bits.StringUtils;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;
import static jexer.TKeypress.*;

/**
 * TCalendar is a date picker widget.
 */
public class TCalendar extends TWidget {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The calendar being displayed.
     */
    private GregorianCalendar displayCalendar = new GregorianCalendar();

    /**
     * The calendar with the selected day.
     */
    private GregorianCalendar calendar = new GregorianCalendar();

    /**
     * The action to perform when the user changes the value of the calendar.
     */
    private TAction updateAction = null;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param x column relative to parent
     * @param y row relative to parent
     * @param updateAction action to call when the user changes the value of
     * the calendar
     */
    public TCalendar(final TWidget parent, final int x, final int y,
        final TAction updateAction) {

        // Set parent and window
        super(parent, x, y, 28, 8);

        this.updateAction = updateAction;
    }

    // ------------------------------------------------------------------------
    // Event handlers ---------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns true if the mouse is currently on the left arrow.
     *
     * @param mouse mouse event
     * @return true if the mouse is currently on the left arrow
     */
    private boolean mouseOnLeftArrow(final TMouseEvent mouse) {
        if ((mouse.getY() == 0)
            && (mouse.getX() == 1)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the mouse is currently on the right arrow.
     *
     * @param mouse mouse event
     * @return true if the mouse is currently on the right arrow
     */
    private boolean mouseOnRightArrow(final TMouseEvent mouse) {
        if ((mouse.getY() == 0)
            && (mouse.getX() == getWidth() - 2)
        ) {
            return true;
        }
        return false;
    }

    /**
     * Handle mouse down clicks.
     *
     * @param mouse mouse button down event
     */
    @Override
    public void onMouseDown(final TMouseEvent mouse) {
        if ((mouseOnLeftArrow(mouse)) && (mouse.isMouse1())) {
            displayCalendar.add(Calendar.MONTH, -1);
        } else if ((mouseOnRightArrow(mouse)) && (mouse.isMouse1())) {
            displayCalendar.add(Calendar.MONTH, 1);
        } else if (mouse.isMouse1()) {
            // Find the day this might correspond to, and set it.
            int index = (mouse.getY() - 2) * 7 + (mouse.getX() / 4) + 1;
            // System.err.println("index: " + index);

            int lastDayNumber = displayCalendar.getActualMaximum(
                    Calendar.DAY_OF_MONTH);
            GregorianCalendar firstOfMonth = new GregorianCalendar();
            firstOfMonth.setTimeInMillis(displayCalendar.getTimeInMillis());
            firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
            int dayOf1st = firstOfMonth.get(Calendar.DAY_OF_WEEK) - 1;
            // System.err.println("dayOf1st: " + dayOf1st);

            int day = index - dayOf1st;
            // System.err.println("day: " + day);

            if ((day < 1) || (day > lastDayNumber)) {
                return;
            }
            calendar.setTimeInMillis(displayCalendar.getTimeInMillis());
            calendar.set(Calendar.DAY_OF_MONTH, day);
        }
    }

    /**
     * Handle mouse double click.
     *
     * @param mouse mouse double click event
     */
    @Override
    public void onMouseDoubleClick(final TMouseEvent mouse) {
        if (updateAction != null) {
            updateAction.DO(this);
        }
    }

    /**
     * Handle keystrokes.
     *
     * @param keypress keystroke event
     */
    @Override
    public void onKeypress(final TKeypressEvent keypress) {
        int increment = 0;

        if (keypress.equals(kbUp)) {
            increment = -7;
        } else if (keypress.equals(kbDown)) {
            increment = 7;
        } else if (keypress.equals(kbLeft)) {
            increment = -1;
        } else if (keypress.equals(kbRight)) {
            increment = 1;
        } else if (keypress.equals(kbEnter)) {
            if (updateAction != null) {
                updateAction.DO(this);
            }
            return;
        } else {
            // Pass to parent for the things we don't care about.
            super.onKeypress(keypress);
            return;
        }

        if (increment != 0) {
            calendar.add(Calendar.DAY_OF_YEAR, increment);

            if ((displayCalendar.get(Calendar.MONTH) != calendar.get(
                    Calendar.MONTH))
                || (displayCalendar.get(Calendar.YEAR) != calendar.get(
                    Calendar.YEAR))
            ) {
                if (increment < 0) {
                    displayCalendar.add(Calendar.MONTH, -1);
                } else {
                    displayCalendar.add(Calendar.MONTH, 1);
                }
            }
        }

    }

    // ------------------------------------------------------------------------
    // TWidget ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Draw the combobox down arrow.
     */
    @Override
    public void draw() {
        CellAttributes backgroundColor = getTheme().getColor(
                "tcalendar.background");
        CellAttributes dayColor = getTheme().getColor(
                "tcalendar.day");
        CellAttributes selectedDayColor = getTheme().getColor(
                "tcalendar.day.selected");
        CellAttributes arrowColor = getTheme().getColor(
                "tcalendar.arrow");
        CellAttributes titleColor = getTheme().getColor(
                "tcalendar.title");

        // Fill in the interior background
        for (int i = 0; i < getHeight(); i++) {
            hLineXY(0, i, getWidth(), ' ', backgroundColor);
        }

        // Draw the title
        String title = String.format("%tB %tY", displayCalendar,
            displayCalendar);
        // This particular title is always single-width (see format string
        // above), but for completeness let's treat it the same as every
        // other window title string.
        int titleLeft = (getWidth() - StringUtils.width(title) - 2) / 2;
        putCharXY(titleLeft, 0, ' ', titleColor);
        putStringXY(titleLeft + 1, 0, title, titleColor);
        putCharXY(titleLeft + StringUtils.width(title) + 1, 0, ' ',
            titleColor);

        // Arrows
        putCharXY(1, 0, GraphicsChars.LEFTARROW, arrowColor);
        putCharXY(getWidth() - 2, 0, GraphicsChars.RIGHTARROW,
            arrowColor);

        /*
         * Now draw out the days.
         */
        putStringXY(0, 1, "  S   M   T   W   T   F   S ", dayColor);
        int lastDayNumber = displayCalendar.getActualMaximum(
                Calendar.DAY_OF_MONTH);
        GregorianCalendar firstOfMonth = new GregorianCalendar();
        firstOfMonth.setTimeInMillis(displayCalendar.getTimeInMillis());
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        int dayOf1st = firstOfMonth.get(Calendar.DAY_OF_WEEK) - 1;
        int dayColumn = dayOf1st * 4;
        int row = 2;

        int dayOfMonth = 1;
        while (dayOfMonth <= lastDayNumber) {
            if (dayColumn == 4 * 7) {
                dayColumn = 0;
                row++;
            }
            if ((dayOfMonth == calendar.get(Calendar.DAY_OF_MONTH))
                && (displayCalendar.get(Calendar.MONTH) == calendar.get(
                    Calendar.MONTH))
                && (displayCalendar.get(Calendar.YEAR) == calendar.get(
                    Calendar.YEAR))
            ) {
                putStringXY(dayColumn, row,
                    String.format(" %2d ", dayOfMonth), selectedDayColor);
            } else {
                putStringXY(dayColumn, row,
                    String.format(" %2d ", dayOfMonth), dayColor);
            }
            dayColumn += 4;
            dayOfMonth++;
        }

    }

    // ------------------------------------------------------------------------
    // TCalendar --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get calendar value.
     *
     * @return the current calendar value (clone instance)
     */
    public Calendar getValue() {
        return (Calendar) calendar.clone();
    }

    /**
     * Set calendar value.
     *
     * @param calendar the new value to use
     */
    public final void setValue(final Calendar calendar) {
        this.calendar.setTimeInMillis(calendar.getTimeInMillis());
    }

    /**
     * Set calendar value.
     *
     * @param millis the millis to set to
     */
    public final void setValue(final long millis) {
        this.calendar.setTimeInMillis(millis);
    }

}
