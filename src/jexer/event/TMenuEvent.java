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
package jexer.event;

/**
 * This class encapsulates a menu selection event.
 * TApplication.getMenuItem(id) can be used to obtain the TMenuItem itself,
 * say for setting enabled/disabled/checked/etc.
 */
public class TMenuEvent extends TInputEvent {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * MenuItem ID.
     */
    private int id;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public contructor.
     *
     * @param id the MenuItem ID
     */
    public TMenuEvent(final int id) {
        this.id = id;
    }

    // ------------------------------------------------------------------------
    // TInputEvent ------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Make human-readable description of this TMenuEvent.
     *
     * @return displayable String
     */
    @Override
    public String toString() {
        return String.format("MenuEvent: %d", id);
    }

    // ------------------------------------------------------------------------
    // TMenuEvent -------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Get the MenuItem ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

}
