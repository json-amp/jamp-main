package org.jamp.websocket;

import java.net.URI;
import java.io.IOException;

public interface WebSocketConnection {

    void connect(String connectionURL, WebSocketListener listener) throws IOException;
    void connect(String connectionURL, SimpleWebSocketListener listener) throws IOException;
    void connect(URI uri, WebSocketListener listener) throws IOException;
    void connect(URI uri, SimpleWebSocketListener listener) throws IOException;
    void close();

}
