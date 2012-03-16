package org.jamp.websocket.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetConnectedException;

public interface WebSocketInternal {

    /**
     * sends the closing handshake. may be send in response to an other
     * handshake.
     */
    public abstract void close(int code, String message);

    public abstract void closeDirect(int code, String message)
            throws IOException;

    public abstract void close(int code);

    /**
     * Send Text data to the other end.
     * 
     */
    public abstract void send(String text) ;

    /**
     * Send Binary data (plain bytes) to the other end.
     * 
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws NotYetConnectedException
     * @throws WebSocketException 
     */
    public abstract void send(byte[] bytes);

    public abstract InetSocketAddress getRemoteSocketAddress();

    public abstract InetSocketAddress getLocalSocketAddress();

    public abstract boolean isConnecting();

    public abstract boolean isOpen();

    public abstract boolean isClosing();

    public abstract boolean isClosed();

    /**
     * Retrieve the WebSocket 'readyState'. This represents the state of the
     * connection. It returns a numerical value, as per W3C WebSockets specs.
     * 
     * @return Returns '0 = CONNECTING', '1 = OPEN', '2 = CLOSING' or '3 =
     *         CLOSED'
     */
    public abstract int getReadyState();

    public abstract void reset();

    public abstract void runClient();

    public abstract void startClient();

    public void clientClose();
    
    public void flush() throws IOException;


}