package org.jamp.amp.stomp;

import java.io.IOException;

import org.jamp.MessageQueueConnection;
import org.jamp.impl.MQMessageListener;
import org.jamp.impl.StompConnection;
import org.junit.Test;


@SuppressWarnings("nls")
public class StompConnectionTest {

     @Test(timeout=15000)    
    public void testSend () throws IOException {
        MessageQueueConnection connection = new StompConnection();
        connection.connect("stomp://localhost:6666/foo", "rick", "rick");
        
        connection.send("queue/bob", "love_rocket");
        
        MQMessageListener messageListener = new MQMessageListener() {
            
            @Override
            public void onTextMessage(String text) {
                System.out.println("GOT MESSAGE    " + text);
                
            }
            
            @Override
            public void onBinaryMessage(String text) {
                 
            }
        };
        connection.subscribe("queue/bob", messageListener);
        
        for (int index=0; index < 10; index++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection.send("queue/bob", "love_rocket");
        }
        
        connection.unsubscribe("queue/bob", messageListener);
        
        for (int index=0; index < 10; index++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection.send("queue/bob", "smurf");
        }


        connection.close();
        
        
        connection.connect("stomp://localhost:6666/foo", "rick", "rick");
        
        connection.subscribe("queue/bob", messageListener);
       
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
 
        connection.close();

    }
    
}
