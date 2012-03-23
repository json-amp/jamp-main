package org.jamp.websocket.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.jamp.websocket.BaseWebSocketListener;
import org.jamp.websocket.SimpleWebSocketListener;
import org.jamp.websocket.WebSocketConnection;
import org.jamp.websocket.WebSocketListener;

/**
 * 
 * @author Rick Hightower
 * 
 */
public class WebSocketConnectionImpl implements WebSocketConnection {

    BaseWebSocketListener listener;
    URI uri;
    WebSocketInternal connection = null;
    WebSocketContextImpl context;

    @Override
    public void connect(String connectionURI, WebSocketListener wslistener)
            throws IOException {
        this.doConnect(connectionURI, wslistener);

    }

    @Override
    public void connect(String connectionURL, SimpleWebSocketListener wslistener)
            throws IOException {
        this.doConnect(connectionURL, wslistener);

    }

    @Override
    public void connect(URI auri, WebSocketListener wslistener)
            throws IOException {
        this.doConnect(auri, wslistener);
    }

    @Override
    public void connect(URI auri, SimpleWebSocketListener wslistener)
            throws IOException {
        this.doConnect(auri, wslistener);
    }

    private void doConnect(String connectionURI, BaseWebSocketListener base)
            throws IOException {
        try {
            this.doConnect(new URI(connectionURI), base);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("nls")
    private void doConnect(URI auri, BaseWebSocketListener base)
            throws IOException {

        if (base == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        this.listener = base;
        this.uri = auri;
        connection = WebSocketInternalImpl.createClientWebSocket(
                new LowLevelListenerAdapter() {
                    @Override
                    public void onMessageBinary(WebSocketInternal conn,
                            byte[] blob) {
                        onMessage(blob);

                    }

                    @Override
                    public void onMessageText(WebSocketInternal conn,
                            String text) {
                        onMessage(text);
                    }

                    @Override
                    public void onStart(WebSocketInternalImpl conn,
                            HttpHeader handshake) {
                        try {
                            listener.onStart(context);
                        } catch (IOException e) {
                            e.printStackTrace(); // Log this better TODO
                        }
                    }

                    @Override
                    public void onWebsocketClose(WebSocketInternal conn,
                            int code, String reason, boolean remote) {

                        try {
                            if (code == CloseFrame.NORMAL) {

                                listener.onClose(context);
                                listener.onDisconnect(context);

                            } else {
                                listener.onDisconnect(context);
                            }
                        } catch (IOException e) {
                            e.printStackTrace(); // Log this better TODO
                        }
                    }

                    @Override
                    public void errorHandler(WebSocketInternal conn,
                            Exception ex) {
                        ex.printStackTrace();
                    }

                }, uri, uri.getPort());

        this.context = new WebSocketContextImpl(this.connection);

        connection.startClient();

    }

    private void onMessage(String text) {

        try {

            if (listener instanceof WebSocketListener) {
                WebSocketListener wsListener = (WebSocketListener) listener;
                wsListener.onReadText(null, new StringReader(text));
            } else if (listener instanceof SimpleWebSocketListener) {
                SimpleWebSocketListener simpleListener = (SimpleWebSocketListener) listener;
                simpleListener.onTextMessage(null, text);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // clean up
        }

    }

    private void onMessage(byte[] buffer) {
        try {

            if (listener instanceof WebSocketListener) {
                WebSocketListener wsListener = (WebSocketListener) listener;
                wsListener.onReadBinary(null, new ByteArrayInputStream(buffer));
            } else if (listener instanceof SimpleWebSocketListener) {
                SimpleWebSocketListener simpleListener = (SimpleWebSocketListener) listener;
                simpleListener.onBinaryMessage(null, buffer);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // clean up
        }

    }

    @Override
    public void close() {
        connection.clientClose();
    }

}
