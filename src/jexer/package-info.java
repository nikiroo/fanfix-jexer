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

/**
 * Jexer - Java Text User Interface library
 *
 * <p>
 * This library is a text-based windowing system loosely reminiscent of
 * Borland's <a href="http://en.wikipedia.org/wiki/Turbo_Vision">Turbo
 * Vision</a> library.  Jexer's goal is to enable people to get up and
 * running with minimum hassle and lots of polish.  A very quick "Hello
 * World" application can be created as simply as this:
 *
 * <pre>
 * {@code
 * import jexer.TApplication;
 *
 * public class MyApplication extends TApplication {
 *
 *     public MyApplication() throws Exception {
 *         super(BackendType.XTERM);
 *
 *         // Create standard menus for Tool, File, and Window.
 *         addToolMenu();
 *         addFileMenu();
 *         addWindowMenu();
 *     }
 *
 *     public static void main(String [] args) throws Exception {
 *         MyApplication app = new MyApplication();
 *         app.run();
 *     }
 * }
 * }
 * </pre>
 */
package jexer;
