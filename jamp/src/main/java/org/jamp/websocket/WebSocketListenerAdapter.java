package org.jamp.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class WebSocketListenerAdapter implements WebSocketListener{

    @Override
    public void onStart(WebSocketContext context) throws IOException {
    }

    @Override
    public void onReadBinary(WebSocketContext context, InputStream is)
            throws IOException {
    }

    @Override
    public void onReadText(WebSocketContext context, Reader is)
            throws IOException {
    }

    @Override
    public void onClose(WebSocketContext context) throws IOException {
    }

    @Override
    public void onDisconnect(WebSocketContext context) throws IOException {
    }


}
