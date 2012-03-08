package org.jamp.amp;

import java.io.IOException;

import org.jamp.amp.stomp.StompConnection;


public class StompMessageSender implements AmpMessageSender {

    StompConnection connection;
    String destination;
    
    public StompMessageSender(String connectionString, String login, String passcode, String destination) throws IOException {
        connection = new StompConnection();
        connection.connect(connectionString, login, passcode);
        this.destination = destination;
    }
    
    public void sendMessage(String name, Object payload, String toInvoker,
            String fromInvoker) throws Exception {
        
        connection.send(destination, (String) payload);
        
    }

}
