package org.jamp.websocket;

import java.io.IOException;

public class SimpleWebSocketListenerAdapter implements SimpleWebSocketListener {

    @Override
    public void onStart(WebSocketContext context) throws IOException {
    }

    @Override
    public void onTextMessage(WebSocketContext context, String text)
            throws IOException {
    }

    @Override
    public void onBinaryMessage(WebSocketContext context, byte[] buffer)
            throws IOException {
    }

    @Override
    public void onClose(WebSocketContext context) throws IOException {
    }

    @Override
    public void onDisconnect(WebSocketContext context) throws IOException {
    }


}
