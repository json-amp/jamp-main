package org.jamp.amp;

import java.io.IOException;

import org.jamp.amp.stomp.MessageListener;
import org.jamp.amp.stomp.StompConnection;


public class StompMessageReceiver {
    
    StompConnection connection;
    String destination;
    SkeletonServiceInvoker invoker;
    
    public StompMessageReceiver(String connectionString, String login, String passcode, String destination, Class<?> serviceClass, Object instance) throws IOException {
        connection = new StompConnection();
        connection.connect(connectionString, login, passcode);
        this.destination = destination;
        
        
        connection.subscribe(destination, new MessageListener() {
            
            public void onTextMessage(String text) throws Exception {
                    handleMessage(text);
            }
            
            public void onBinaryMessage(String text)  throws Exception {
                
            }
        });
        
        if (serviceClass!=null) {
            invoker = AmpFactory.factory().createJampServerSkeleton(serviceClass);
        } else {
            invoker = AmpFactory.factory().createJampServerSkeleton(instance);
            
        }
        
    }
    
    void handleMessage (String text) throws Exception{
        invoker.invokeMessage(text);
    }


}
