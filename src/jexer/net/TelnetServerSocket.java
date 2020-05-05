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
package jexer.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class provides a ServerSocket that return TelnetSocket's in accept().
 */
public class TelnetServerSocket extends ServerSocket {

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------


    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Creates an unbound server socket.
     *
     * @throws IOException if an I/O error occurs
     */
    public TelnetServerSocket() throws IOException {
        super();
    }

    /**
     * Creates a server socket, bound to the specified port.
     *
     * @param port the port number, or 0 to use a port number that is
     * automatically allocated.
     * @throws IOException if an I/O error occurs
     */
    public TelnetServerSocket(final int port) throws IOException {
        super(port);
    }

    /**
     * Creates a server socket and binds it to the specified local port
     * number, with the specified backlog.
     *
     * @param port the port number, or 0 to use a port number that is
     * automatically allocated.
     * @param backlog requested maximum length of the queue of incoming
     * connections.
     * @throws IOException if an I/O error occurs
     */
    public TelnetServerSocket(final int port,
        final int backlog) throws IOException {

        super(port, backlog);
    }

    /**
     * Create a server with the specified port, listen backlog, and local IP
     * address to bind to.
     *
     * @param port the port number, or 0 to use a port number that is
     * automatically allocated.
     * @param backlog requested maximum length of the queue of incoming
     * connections.
     * @param bindAddr the local InetAddress the server will bind to
     * @throws IOException if an I/O error occurs
     */
    public TelnetServerSocket(final int port, final int backlog,
        final InetAddress bindAddr) throws IOException {

        super(port, backlog, bindAddr);
    }

    // ------------------------------------------------------------------------
    // ServerSocket -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Listens for a connection to be made to this socket and accepts it. The
     * method blocks until a connection is made.
     *
     * @return the new Socket
     * @throws IOException if an I/O error occurs
     */
    @Override
    public Socket accept() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (!isBound()) {
            throw new SocketException("Socket is not bound");
        }

        Socket socket = new TelnetSocket();
        implAccept(socket);
        return socket;
    }

}
