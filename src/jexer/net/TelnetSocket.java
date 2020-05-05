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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class provides a Socket that performs the telnet protocol to both
 * establish an 8-bit clean no echo channel and expose window resize events
 * to the Jexer ECMA48 backend.
 */
public class TelnetSocket extends Socket {

    // ------------------------------------------------------------------------
    // Constants --------------------------------------------------------------
    // ------------------------------------------------------------------------

    // Telnet protocol special characters.  Note package private access.
    static final int TELNET_SE         = 240;
    static final int TELNET_NOP        = 241;
    static final int TELNET_DM         = 242;
    static final int TELNET_BRK        = 243;
    static final int TELNET_IP         = 244;
    static final int TELNET_AO         = 245;
    static final int TELNET_AYT        = 246;
    static final int TELNET_EC         = 247;
    static final int TELNET_EL         = 248;
    static final int TELNET_GA         = 249;
    static final int TELNET_SB         = 250;
    static final int TELNET_WILL       = 251;
    static final int TELNET_WONT       = 252;
    static final int TELNET_DO         = 253;
    static final int TELNET_DONT       = 254;
    static final int TELNET_IAC        = 255;
    static final int C_NUL             = 0x00;
    static final int C_LF              = 0x0A;
    static final int C_CR              = 0x0D;

    // ------------------------------------------------------------------------
    // Variables --------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The telnet-aware socket InputStream.
     */
    private TelnetInputStream input;

    /**
     * The telnet-aware socket OutputStream.
     */
    private TelnetOutputStream output;


    /**
     * If true, this is a server socket (i.e. created by accept()).
     */
    boolean isServer = true;

    /**
     * If true, telnet ECHO mode is set such that local echo is off and
     * remote echo is on.  This is appropriate for server sockets.
     */
    boolean echoMode = false;

    /**
     * If true, telnet BINARY mode is enabled.  We always want this to
     * ensure a Unicode-safe stream.
     */
    boolean binaryMode = false;

    /**
     * If true, the SUPPRESS-GO-AHEAD option is enabled.  We always want
     * this.
     */
    boolean goAhead = true;

    /**
     * If true, request the client terminal type.
     */
    boolean doTermType = true;

    /**
     * If true, request the client terminal speed.
     */
    boolean doTermSpeed = true;

    /**
     * If true, request the Negotiate About Window Size option to
     * determine the client text width/height.
     */
    boolean doNAWS = true;

    /**
     * If true, request the New Environment option to obtain the client
     * LOGNAME, USER, and LANG variables.
     */
    boolean doEnvironment = true;

    /**
     * The terminal type reported by the client.
     */
    String terminalType = "";

    /**
     * The terminal speed reported by the client.
     */
    String terminalSpeed = "";

    // ------------------------------------------------------------------------
    // Constructors -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Creates a Socket that knows the telnet protocol.  Note package private
     * access, this is only used by TelnetServerSocket.
     *
     * @throws IOException if an I/O error occurs
     */
    TelnetSocket() throws IOException {
        super();
    }

    // ------------------------------------------------------------------------
    // Socket -----------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Returns an input stream for this socket.
     *
     * @return the input stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (input == null) {
            assert (output == null);
            output = new TelnetOutputStream(this, super.getOutputStream());
            input = new TelnetInputStream(this, super.getInputStream(), output);
            input.telnetSendOptions();
        }
        return input;
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return the output stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (output == null) {
            assert (input == null);
            output = new TelnetOutputStream(this, super.getOutputStream());
            input = new TelnetInputStream(this, super.getInputStream(), output);
            input.telnetSendOptions();
        }
        return output;
    }

    // ------------------------------------------------------------------------
    // TelnetSocket -----------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * See if telnet server/client is in ASCII mode.
     *
     * @return if true, this connection is in ASCII mode
     */
    public boolean isAscii() {
        return (!binaryMode);
    }

}
