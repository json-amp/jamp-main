package org.jamp.websocket;

import org.jamp.websocket.impl.WebSocketConnectionImpl;

public class WebSocketConnectionFactory {
    public static WebSocketConnection create() {
        return new WebSocketConnectionImpl();
    }
}
