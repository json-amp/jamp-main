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
    LowLevelWebSocketConnectionInternal connection = null;
    WebSocketContextImpl context;

    public void connect(String connectionURI, WebSocketListener listener)
            throws IOException {
        this.doConnect(connectionURI, listener);

    }

    public void connect(String connectionURL, SimpleWebSocketListener listener)
            throws IOException {
        this.doConnect(connectionURL, listener);

    }

    public void connect(URI uri, WebSocketListener listener) throws IOException {
        this.doConnect(uri, listener);
    }

    public void connect(URI uri, SimpleWebSocketListener listener)
            throws IOException {
        this.doConnect(uri, listener);
    }

    private void doConnect(String connectionURI, BaseWebSocketListener base)
            throws IOException {
        try {
            this.doConnect(new URI(connectionURI), base);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private void doConnect(URI uri, BaseWebSocketListener base) throws IOException {
        
        if (base == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        this.listener = base;
        this.uri = uri;
        connection = LowLevelWebSocketConnectionInternalImpl.createClientWebSocket(
                new LowLevelListenerAdapter() {
                    public void onMessageBinary(
                            LowLevelWebSocketConnectionInternal conn, byte[] blob) {
                        onMessage(conn, blob);

                    }

                    public void onMessageText(
                            LowLevelWebSocketConnectionInternal conn, String text) {
                        onMessage(conn, text);
                    }
                    public void onStart(LowLevelWebSocketConnectionInternalImpl conn,
                            HttpHeader handshake) {
                       try {
                            listener.onStart(context);
                        } catch (IOException e) {
                            e.printStackTrace(); //Log this better TODO
                        }
                    }
                    public void onWebsocketClose(LowLevelWebSocketConnectionInternal conn, int code,
                            String reason, boolean remote) {
                         
                         try {
                             if (code == CloseFrame.NORMAL) {
                                 
                                 listener.onClose(context);
                                 listener.onDisconnect(context);

                             } else {
                                 listener.onDisconnect(context);
                             }
                        } catch (IOException e) {
                            e.printStackTrace(); //Log this better TODO
                        }
                     }
                    
                    
                    public void errorHandler(LowLevelWebSocketConnectionInternal conn, Exception ex) {
                        ex.printStackTrace();
                    }



                }, uri, uri.getPort());
        
        this.context = new WebSocketContextImpl(this.connection);

        connection.startClient();
        

    }

    private void onMessage(LowLevelWebSocketConnectionInternal conn, String text) {

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
            //clean up
        }

    }

    private void onMessage(LowLevelWebSocketConnectionInternal conn, byte[] buffer) {
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
            //clean up
        }

    }

    public void close() {
        connection.clientClose();
    }

}
