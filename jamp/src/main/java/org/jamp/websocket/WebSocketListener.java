package org.jamp.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface WebSocketListener extends BaseWebSocketListener {

    @Override
    void onStart(WebSocketContext context) throws IOException;

    void onReadBinary(WebSocketContext context, InputStream is)
            throws IOException;

    void onReadText(WebSocketContext context, Reader is) throws IOException;

    @Override
    void onClose(WebSocketContext context) throws IOException;

    @Override
    void onDisconnect(WebSocketContext context) throws IOException;

}
