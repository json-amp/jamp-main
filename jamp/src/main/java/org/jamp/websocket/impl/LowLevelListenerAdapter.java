package org.jamp.websocket.impl;

import org.jamp.websocket.impl.Frame.Opcode;

/**
 * @author rick (did some major refactors, basically forked org.java_websocket
 */
abstract class LowLevelListenerAdapter implements LowLevelListener {



    /**
     * This default implementation does not do anything. Go ahead and overwrite
     * it.
     * 
     * @see org.jamp.websocket.impl.LowLevelListener#onMessageText(LowLevelWebSocketConnectionInternalImpl,
     *      String)
     */
    public void onMessageText(LowLevelWebSocketConnectionInternal conn, String message) {
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite
     * it.
     * 
     * @see @see org.java_websocket.WebSocketListener#onWebsocketOpen(WebSocket,
     *      Handshakedata)
     */
    public void onStart(LowLevelWebSocketConnectionInternalImpl conn,
            HttpHeader handshake) {
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite
     * it.
     * 
     * @see @see
     *      org.java_websocket.WebSocketListener#onWebsocketClose(WebSocket,
     *      int, String, boolean)
     */
    public void onWebsocketClose(LowLevelWebSocketConnectionInternal conn, int code,
            String reason, boolean remote) {
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite
     * it.
     * 
     * @see @see
     *      org.java_websocket.WebSocketListener#onWebsocketMessage(WebSocket,
     *      byte[])
     */
    public void onMessageBinary(LowLevelWebSocketConnectionInternal conn, byte[] blob) {
    }

    /**
     * This default implementation will send a pong in response to the received
     * ping. The pong frame will have the same payload as the ping frame.
     * 
     * @see @see org.java_websocket.WebSocketListener#onWebsocketPing(WebSocket,
     *      Framedata)
     */
    public void onPing(LowLevelWebSocketConnectionInternalImpl conn, Frame f) {
        Frame resp = new Frame(f);
        resp.setOptcode(Opcode.PONG);
        try {
            conn.sendFrame(resp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite
     * it.
     * 
     * @see @see org.java_websocket.WebSocketListener#onWebsocketPong(WebSocket,
     *      Framedata)
     */
    public void onPong(LowLevelWebSocketConnectionInternal conn, Frame f) {
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite
     * it.
     * 
     * @see @see
     *      org.java_websocket.WebSocketListener#onWebsocketError(WebSocket,
     *      Exception)
     */
    public void errorHandler(LowLevelWebSocketConnectionInternal conn, Exception ex) {
        ex.printStackTrace(); //add some logging here... TODO
    }
    
    public void onWriteDemand(LowLevelWebSocketConnectionInternal conn) {
    }


}
