package org.jamp.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;

import org.jamp.websocket.impl.HttpHeader;
import org.jamp.websocket.impl.WebSocketInternal;
import org.jamp.websocket.impl.WebSocketServer;
import org.jamp.websocket.impl.WebSocketException;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
@SuppressWarnings("nls")
public class WebSocketChatServer extends WebSocketServer {

    public WebSocketChatServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( InetAddress.getByName( "localhost" ), port ) );
	}
	

	@Override
	public void onOpen( WebSocketInternal conn, HttpHeader handshake ) {
		try {
			this.sendToAll( conn + " entered the room!" );
		} catch ( InterruptedException ex ) {
			ex.printStackTrace();
		}
		System.out.println( conn + " entered the room!" );
	}

	@Override
	public void onClose( WebSocketInternal conn, int code, String reason, boolean remote ) {
		try {
			this.sendToAll( conn + " has left the room!" );
		} catch ( InterruptedException ex ) {
			ex.printStackTrace();
		}
		System.out.println( conn + " has left the room!" );
	}

	@Override
	public void onMessage( WebSocketInternal conn, String message ) {
		try {
			this.sendToAll( conn + ": " + message );
		} catch ( InterruptedException ex ) {
			ex.printStackTrace();
		}
		System.out.println( conn + ": " + message );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		int port = 8887;
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		WebSocketChatServer s = new WebSocketChatServer( port );
		s.start();
		System.out.println( "ChatServer started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			s.sendToAll( in );
		}
	}

	@Override
	public void onError( WebSocketInternal conn, Exception ex ) {
		ex.printStackTrace();
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 * @throws WebSocketException 
	 * @throws IllegalArgumentException 
	 * @throws NotYetConnectedException 
	 */
	public void sendToAll( String text ) throws InterruptedException, NotYetConnectedException, IllegalArgumentException, WebSocketException {
		for( WebSocketInternal c : connections() ) {
			c.send( text );
		}
	}
}
