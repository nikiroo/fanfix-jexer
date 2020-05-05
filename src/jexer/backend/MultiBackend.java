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
package jexer.backend;

import java.util.ArrayList;
import java.util.List;

import jexer.event.TCommandEvent;
import jexer.event.TInputEvent;
import static jexer.TCommand.*;

/**
 * MultiBackend mirrors its I/O to several backends.
 */
public class MultiBackend implements Backend {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The screen to use.
     */
    private MultiScreen multiScreen;

    /**
     * The list of backends to use.
     */
    private List<Backend> backends = new ArrayList<Backend>();

    /**
     * The SessionInfo to return.
     */
    private SessionInfo sessionInfo;

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Public constructor requires one backend.  Note that this backend's
     * screen will be replaced with a MultiScreen.
     *
     * @param backend the backend to add
     */
    public MultiBackend(final Backend backend) {
        backends.add(backend);
        if (backend instanceof TWindowBackend) {
            multiScreen = new MultiScreen(((TWindowBackend) backend).getOtherScreen());
        } else {
            multiScreen = new MultiScreen(backend.getScreen());
        }
        if (backend instanceof GenericBackend) {
            ((GenericBackend) backend).abortOnDisconnect = false;
        }
        sessionInfo = backend.getSessionInfo();
    }

    // ------------------------------------------------------------------------
    // Backend ----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Getter for sessionInfo.
     *
     * @return the SessionInfo
     */
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    /**
     * Getter for screen.
     *
     * @return the Screen
     */
    public Screen getScreen() {
        return multiScreen;
    }

    /**
     * Subclasses must provide an implementation that syncs the logical
     * screen to the physical device.
     */
    public void flushScreen() {
        for (Backend backend: backends) {
            backend.flushScreen();
        }
    }

    /**
     * Check if there are events in the queue.
     *
     * @return if true, getEvents() has something to return to the application
     */
    public boolean hasEvents() {
        if (backends.size() == 0) {
            return true;
        }
        for (Backend backend: backends) {
            if (backend.hasEvents()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Subclasses must provide an implementation to get keyboard, mouse, and
     * screen resize events.
     *
     * @param queue list to append new events to
     */
    public void getEvents(List<TInputEvent> queue) {
        List<Backend> backendsToRemove = null;
        for (Backend backend: backends) {
            if (backend.hasEvents()) {
                backend.getEvents(queue);

                // This default backend assumes a single user, and if that
                // user becomes disconnected we should terminate the
                // application.
                if (queue.size() > 0) {
                    TInputEvent event = queue.get(queue.size() - 1);
                    if (event instanceof TCommandEvent) {
                        TCommandEvent command = (TCommandEvent) event;
                        if (command.equals(cmBackendDisconnect)) {
                            if (backendsToRemove == null) {
                                backendsToRemove = new ArrayList<Backend>();
                            }
                            backendsToRemove.add(backend);
                        }
                    }
                }
            }
        }
        if (backendsToRemove != null) {
            for (Backend backend: backendsToRemove) {
                multiScreen.removeScreen(backend.getScreen());
                backends.remove(backend);
                backend.shutdown();
            }
        }
        if (backends.size() == 0) {
            queue.add(new TCommandEvent(cmAbort));
        }
    }

    /**
     * Subclasses must provide an implementation that closes sockets,
     * restores console, etc.
     */
    public void shutdown() {
        for (Backend backend: backends) {
            backend.shutdown();
        }
    }

    /**
     * Subclasses must provide an implementation that sets the window title.
     *
     * @param title the new title
     */
    public void setTitle(final String title) {
        for (Backend backend: backends) {
            backend.setTitle(title);
        }
    }

    /**
     * Set listener to a different Object.
     *
     * @param listener the new listening object that run() wakes up on new
     * input
     */
    public void setListener(final Object listener) {
        for (Backend backend: backends) {
            backend.setListener(listener);
        }
    }

    /**
     * Reload backend options from System properties.
     */
    public void reloadOptions() {
        for (Backend backend: backends) {
            backend.reloadOptions();
        }
    }

    // ------------------------------------------------------------------------
    // MultiBackend -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Add a backend to the list.
     *
     * @param backend the backend to add
     */
    public void addBackend(final Backend backend) {
        backends.add(backend);
        if (backend instanceof TWindowBackend) {
            multiScreen.addScreen(((TWindowBackend) backend).getOtherScreen());
        } else {
            multiScreen.addScreen(backend.getScreen());
        }
        if (backend instanceof GenericBackend) {
            ((GenericBackend) backend).abortOnDisconnect = false;
        }
    }

    /**
     * Remove a backend from the list.
     *
     * @param backend the backend to remove
     */
    public void removeBackend(final Backend backend) {
        if (backends.size() > 1) {
            if (backend instanceof TWindowBackend) {
                multiScreen.removeScreen(((TWindowBackend) backend).getOtherScreen());
            } else {
                multiScreen.removeScreen(backend.getScreen());
            }
            backends.remove(backend);
        }
    }

}
