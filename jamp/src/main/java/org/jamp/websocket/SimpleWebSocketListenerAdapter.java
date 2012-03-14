package org.jamp.websocket;

import java.io.IOException;

public class SimpleWebSocketListenerAdapter implements SimpleWebSocketListener {

    public void onStart(WebSocketContext context) throws IOException {
    }

    public void onTextMessage(WebSocketContext context, String text)
            throws IOException {
    }

    public void onBinaryMessage(WebSocketContext context, byte[] buffer)
            throws IOException {
    }

    public void onClose(WebSocketContext context) throws IOException {
    }

    public void onDisconnect(WebSocketContext context) throws IOException {
    }


}
