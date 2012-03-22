package org.jamp.websocket;

import java.io.IOException;

public interface SimpleWebSocketListener extends BaseWebSocketListener {

    @Override
    void onStart(WebSocketContext context) throws IOException;

    void onTextMessage(WebSocketContext context, String text)
            throws IOException;

    void onBinaryMessage(WebSocketContext context, byte[] buffer)
            throws IOException;

    @Override
    void onClose(WebSocketContext context) throws IOException;

    @Override
    void onDisconnect(WebSocketContext context) throws IOException;

}
