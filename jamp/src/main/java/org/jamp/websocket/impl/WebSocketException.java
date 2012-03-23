package org.jamp.websocket.impl;

@SuppressWarnings("serial")
public class WebSocketException extends RuntimeException {
    private int closecode;

    public WebSocketException(int closecode) {
        this.closecode = closecode;
    }

    public WebSocketException(String s) {
        this(CloseFrame.PROTOCOL_ERROR, s);
    }

    public WebSocketException(int closecode, String s) {
        super(s);
        this.closecode = closecode;
    }

    public WebSocketException(int aclosecode, Throwable t) {
        super(t);
        if (t instanceof WebSocketException) {
            closecode = ((WebSocketException) t).getCloseCode();
        } else {
            closecode = aclosecode;
        }

    }

    public WebSocketException(int aclosecode, String s, Throwable t) {
        super(s, t);
        if (t instanceof WebSocketException) {
            closecode = ((WebSocketException) t).getCloseCode();
        } else {
            closecode = aclosecode;
        }
    }

    public int getCloseCode() {
        return closecode;
    }

}
