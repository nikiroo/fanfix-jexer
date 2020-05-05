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

import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import jexer.net.TelnetServerSocket;

/**
 * This class is the main driver for a simple demonstration of Jexer's
 * capabilities.  Rather than run locally, it serves a Jexer UI over a TCP
 * port.
 */
public class Demo2 {

    /**
     * Translated strings.
     */
    private static final ResourceBundle i18n = ResourceBundle.getBundle(Demo2.class.getName());

    /**
     * Main entry point.
     *
     * @param args Command line arguments
     */
    public static void main(final String [] args) {
        ServerSocket server = null;
        try {
            if (args.length == 0) {
                System.err.println(i18n.getString("usageString"));
                return;
            }

            int port = Integer.parseInt(args[0]);
            server = new TelnetServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                System.out.println(MessageFormat.
                    format(i18n.getString("newConnection"), socket));
                DemoApplication app = new DemoApplication(socket.getInputStream(),
                    socket.getOutputStream());
                (new Thread(app)).start();
                Thread.sleep(500);
                System.out.println(MessageFormat.
                    format(i18n.getString("terminal"),
                    ((jexer.net.TelnetInputStream) socket.getInputStream()).
                        getTerminalType()));
                System.out.println(MessageFormat.
                    format(i18n.getString("username"),
                    ((jexer.net.TelnetInputStream) socket.getInputStream()).
                        getUsername()));
                System.out.println(MessageFormat.
                    format(i18n.getString("language"),
                    ((jexer.net.TelnetInputStream) socket.getInputStream()).
                        getLanguage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (Exception e) {
                    // SQUASH
                }
            }
        }
    }

}
