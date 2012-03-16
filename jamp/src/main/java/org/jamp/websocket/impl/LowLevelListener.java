package org.jamp.websocket.impl;


/**
 * Implemented by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>. The
 * methods within are called by <tt>WebSocket</tt>. Almost every method takes a
 * first parameter conn which represents the source of the respective event.
 */
interface LowLevelListener {


    /**
     * Called when an entire text frame has been received. Do whatever you want
     * here...
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occurring on.
     * @param message
     *            The UTF-8 decoded message that was received.
     */
    public void onMessageText(WebSocketInternal conn, String message);

    /**
     * Called when an entire binary frame has been received. Do whatever you
     * want here...
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occurring on.
     * @param blob
     *            The binary message that was received.
     */
    public void onMessageBinary(WebSocketInternal conn, byte[] buffer);

    /**
     * Called after <var>onHandshakeReceived</var> returns <var>true</var>.
     * Indicates that a complete WebSocket connection has been established, and
     * we are ready to send/receive data.
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occuring on.
     */
    public void onStart(WebSocketInternalImpl conn, HttpHeader d);

    /**
     * Called after <tt>WebSocket#close</tt> is explicity called, or when the
     * other end of the WebSocket connection is closed.
     * 
     * @param conn
     *            The <tt>WebSocket</tt> instance this event is occuring on.
     */
    public void onWebsocketClose(WebSocketInternal conn, int code,
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
    public void errorHandler(WebSocketInternal conn, Exception ex);

    /**
     * Called a ping frame has been received. This method must send a
     * corresponding pong by itself.
     * 
     * @param f
     *            The ping frame. Control frames may contain payload.
     */
    public void onPing(WebSocketInternalImpl conn, Frame f);

    /**
     * Called when a pong frame is received.
     **/
    public void onPong(WebSocketInternal conn, Frame f);

    /**
     * This method is used to inform the selector thread that there is data
     * queued to be written to the socket.
     */
    public void onWriteDemand(WebSocketInternal conn);
}
