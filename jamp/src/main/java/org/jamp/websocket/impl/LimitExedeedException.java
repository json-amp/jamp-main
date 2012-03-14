package org.jamp.websocket.impl;


@SuppressWarnings("serial")
public class LimitExedeedException extends InvalidDataException {

	public LimitExedeedException() {
		super( CloseFrame.TOOBIG );
	}

	public LimitExedeedException( String s ) {
		super( CloseFrame.TOOBIG, s );
	}

}
