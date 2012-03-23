package org.jamp.websocket;

import java.net.URI;
import java.io.IOException;

public interface WebSocketConnection {

    void connectWithProtocol(String connectionURL, WebSocketListener listener,
            String... protocols) throws IOException;

    void connectWithProtocol(String connectionURL,
            SimpleWebSocketListener listener, String... protocols)
            throws IOException;

    void connectWithProtocol(URI uri, WebSocketListener listener,
            String... protocols) throws IOException;

    void connectWithProtocol(URI uri, SimpleWebSocketListener listener,
            String... protocols) throws IOException;

    void connect(String connectionURL, WebSocketListener listener)
            throws IOException;

    void connect(String connectionURL, SimpleWebSocketListener listener)
            throws IOException;

    void connect(URI uri, WebSocketListener listener) throws IOException;

    void connect(URI uri, SimpleWebSocketListener listener) throws IOException;

    void close();

}
