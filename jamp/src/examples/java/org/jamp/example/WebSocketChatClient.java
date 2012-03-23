package org.jamp.example;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jamp.websocket.SimpleWebSocketListener;
import org.jamp.websocket.WebSocketConnection;
import org.jamp.websocket.WebSocketConnectionFactory;
import org.jamp.websocket.WebSocketContext;

@SuppressWarnings("nls")
public class WebSocketChatClient extends JFrame implements ActionListener {
	private static final long serialVersionUID = -6056260699202978657L;

	 final JTextField uriField;
	 final JButton connect;
	 final JButton close;
	 final JTextArea ta;
	 final JTextField chatField;
	  WebSocketConnection conneciton;
      WebSocketContext context;

    public WebSocketChatClient( String defaultlocation ) {
		super( "WebSocket Chat Client" );
		Container c = getContentPane();
		GridLayout layout = new GridLayout();
		layout.setColumns( 1 );
		layout.setRows( 6 );
		c.setLayout( layout );


		uriField = new JTextField();
		uriField.setText( defaultlocation );
		c.add( uriField );

		connect = new JButton( "Connect" );
		connect.addActionListener( this );
		c.add( connect );

		close = new JButton( "Close" );
		close.addActionListener( this );
		close.setEnabled( false );
		c.add( close );

		JScrollPane scroll = new JScrollPane();
		ta = new JTextArea();
		scroll.setViewportView( ta );
		c.add( scroll );

		chatField = new JTextField();
		chatField.setText( "enter text here" );
		chatField.addActionListener( this );
		c.add( chatField );

		java.awt.Dimension d = new java.awt.Dimension( 300, 400 );
		setPreferredSize( d );
		setSize( d );

		addWindowListener( new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing( WindowEvent e ) {
				if( conneciton != null ) {
				    conneciton.close();
				}
				dispose();
			}
		} );

		setLocationRelativeTo( null );
		setVisible( true );
	}

	@Override
    public void actionPerformed( ActionEvent e ) {

		if( e.getSource() == chatField ) {
			if( context != null ) {
				try {
				    context.sendText(chatField.getText() );
					chatField.setText( "" );
					chatField.requestFocus();
				} catch ( Exception ex ) {
					ex.printStackTrace();
				}
			}

		} else if( e.getSource() == connect ) {
			    conneciton = WebSocketConnectionFactory.create();
		        
			    try {
                    conneciton.connect("ws://localhost:8887/", new SimpleWebSocketListener() {
                        
                        @Override
                        public void onTextMessage(WebSocketContext acontext, String text)
                                throws IOException {
                            ta.append( "got: " + text + "\n" );
                            ta.setCaretPosition( ta.getDocument().getLength() );
                            
                        }
                        
                        @Override
                        public void onStart(WebSocketContext acontext) throws IOException {
                              ta.append( "You are connected to ChatServer: \n" );
                              ta.setCaretPosition( ta.getDocument().getLength() );
                              WebSocketChatClient.this.context = acontext;

                        }
                        
                        @Override
                        public void onDisconnect(WebSocketContext acontext) throws IOException {
                            
                            if (closed) {
                                ta.append( "You have been disconnected from ChatServer closed properly." );
                                ta.setCaretPosition( ta.getDocument().getLength() );
                            } else {
                                ta.append( "Connection closed due to errors." );
                                ta.setCaretPosition( ta.getDocument().getLength() );
                                
                            }
                            connect.setEnabled( true );
                            uriField.setEditable( true );
                            close.setEnabled( false );
                       }
                        
                        
                        boolean closed;
                        @Override
                        public void onClose(WebSocketContext acontext) throws IOException {
                            System.out.println("onClose ");
                                       this.closed = true;    
                        }
                        
                        @Override
                        public void onBinaryMessage(WebSocketContext acontext, byte[] buffer)
                                throws IOException {
                            System.out.println("onBinaryMessage ");
                                           
                        }
                    });
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }



				close.setEnabled( true );
				connect.setEnabled( false );
				uriField.setEditable( false );
		} else if( e.getSource() == close ) {
			try {
			    conneciton.close();
			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
    public static void main( String[] args ) {
		String location;
		if( args.length != 0 ) {
			location = args[ 0 ];
			System.out.println( "Default server url specified: \'" + location + "\'" );
		} else {
			location = "ws://localhost:8887";
			System.out.println( "Default server url not specified: defaulting to \'" + location + "\'" );
		}
		new WebSocketChatClient( location );
	}

}
