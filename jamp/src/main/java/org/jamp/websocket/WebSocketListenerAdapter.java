package org.jamp.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class WebSocketListenerAdapter implements WebSocketListener{

    public void onStart(WebSocketContext context) throws IOException {
    }

    public void onReadBinary(WebSocketContext context, InputStream is)
            throws IOException {
    }

    public void onReadText(WebSocketContext context, Reader is)
            throws IOException {
    }

    public void onClose(WebSocketContext context) throws IOException {
    }

    public void onDisconnect(WebSocketContext context) throws IOException {
    }


}
