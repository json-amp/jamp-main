package org.jamp.websocket.impl;

@SuppressWarnings("serial")
public class WebSocketException extends RuntimeException {
	private int closecode;
	public WebSocketException( int closecode ) {
		this.closecode = closecode;
	}

    public WebSocketException( String s ) {
	        this( CloseFrame.PROTOCOL_ERROR, s );
	}

	public WebSocketException( int closecode , String s ) {
		super( s );
		this.closecode = closecode;
	}

	public WebSocketException( int closecode , Throwable t ) {
		super( t );
		if( t instanceof WebSocketException ) {
			closecode = ( (WebSocketException) t ).getCloseCode();
		}
	}

	public WebSocketException( int closecode , String s , Throwable t ) {
		super( s, t );
		if( t instanceof WebSocketException ) {
			closecode = ( (WebSocketException) t ).getCloseCode();
		}
	}

	public int getCloseCode() {
		return closecode;
	}

}
