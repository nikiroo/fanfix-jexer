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

import java.util.Date;

/**
 * TTimer implements a simple timer.
 */
public class TTimer {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * If true, re-schedule after every tick.  Note package private access.
     */
    boolean recurring = false;

    /**
     * Duration (in millis) between ticks if this is a recurring timer.
     */
    private long duration = 0;

    /**
     * The next time this timer needs to be ticked.
     */
    private Date nextTick;

    /**
     * The action to perfom on a tick.
     */
    private TAction action;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Package private constructor.
     *
     * @param duration number of milliseconds to wait between ticks
     * @param recurring if true, re-schedule this timer after every tick
     * @param action to perform on next tick
     */
    TTimer(final long duration, final boolean recurring, final TAction action) {

        this.recurring = recurring;
        this.duration  = duration;
        this.action    = action;

        Date now = new Date();
        nextTick = new Date(now.getTime() + duration);
    }

    // ------------------------------------------------------------------------
    // TTimer -----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the next time this timer needs to be ticked.  Note package private
     * access.
     *
     * @return time at which action should be called
     */
    Date getNextTick() {
        return nextTick;
    }

    /**
     * Set the recurring flag.
     *
     * @param recurring if true, re-schedule this timer after every tick
     */
    public void setRecurring(final boolean recurring) {
        this.recurring = recurring;
    }

    /**
     * Tick this timer.  Note package private access.
     */
    void tick() {
        if (action != null) {
            action.DO();
        }
        // Set next tick
        Date ticked = new Date();
        if (recurring) {
            nextTick = new Date(ticked.getTime() + duration);
        }
    }

}
