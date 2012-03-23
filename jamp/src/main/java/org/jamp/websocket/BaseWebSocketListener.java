package org.jamp.websocket;

import java.io.IOException;

public interface BaseWebSocketListener {

    void onStart(WebSocketContext context) throws IOException;

    void onClose(WebSocketContext context) throws IOException;

    void onDisconnect(WebSocketContext context) throws IOException;

}
