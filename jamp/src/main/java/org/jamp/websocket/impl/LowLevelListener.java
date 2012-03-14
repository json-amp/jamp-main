package org.jamp.websocket.impl;


/**
 * Implemented by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>. The
 * methods within are called by <tt>WebSocket</tt>. Almost every method takes a
 * first parameter conn which represents the source of the respective event.
 */
interface LowLevelListener {

    /**
     * Called on the server side when the socket connection is first
     * established, and the WebSocket handshake has been received.
     * 
     * @param conn
     *            The WebSocket related to this event
     * @param draft
     *            The protocol draft the client uses to connect
     * @param request
     *            The opening http message send by the client. Can be used to
     *            access additional fields like cookies.
     * @return Returns an incomplete handshake containing all optional fields
     * @throws InvalidDataException
     *             Throwing this exception will cause this handshake to be
     *             rejected
     */
    public HttpHeader onConnectionIsServer(
            LowLevelWebSocketConnectionInternal conn, HttpHeader request)
            throws InvalidDataException;


    /**
     * Called when an entire text frame has been received. Do whatever you want
     * here...
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occurring on.
     * @param message
     *            The UTF-8 decoded message that was received.
     */
    public void onMessageText(LowLevelWebSocketConnectionInternal conn, String message);

    /**
     * Called when an entire binary frame has been received. Do whatever you
     * want here...
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occurring on.
     * @param blob
     *            The binary message that was received.
     */
    public void onMessageBinary(LowLevelWebSocketConnectionInternal conn, byte[] buffer);

    /**
     * Called after <var>onHandshakeReceived</var> returns <var>true</var>.
     * Indicates that a complete WebSocket connection has been established, and
     * we are ready to send/receive data.
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occuring on.
     */
    public void onStart(LowLevelWebSocketConnectionInternalImpl conn, HttpHeader d);

    /**
     * Called after <tt>WebSocket#close</tt> is explicity called, or when the
     * other end of the WebSocket connection is closed.
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occuring on.
     */
    public void onWebsocketClose(LowLevelWebSocketConnectionInternal conn, int code,
            String reason, boolean remote);

    /**
     * Called if an exception worth noting occurred. If an error causes the
     * connection to fail onClose will be called additionally afterwards.
     * 
     * @param ex
     *            The exception that occurred. <br>
     *            Might be null if the exception is not related to any specific
     *            connection. For example if the server port could not be bound.
     */
    public void errorHandler(LowLevelWebSocketConnectionInternal conn, Exception ex);

    /**
     * Called a ping frame has been received. This method must send a
     * corresponding pong by itself.
     * 
     * @param f
     *            The ping frame. Control frames may contain payload.
     */
    public void onPing(LowLevelWebSocketConnectionInternalImpl conn, Frame f);

    /**
     * Called when a pong frame is received.
     **/
    public void onPong(LowLevelWebSocketConnectionInternal conn, Frame f);

    /**
     * This method is used to inform the selector thread that there is data
     * queued to be written to the socket.
     */
    public void onWriteDemand(LowLevelWebSocketConnectionInternal conn);
}
