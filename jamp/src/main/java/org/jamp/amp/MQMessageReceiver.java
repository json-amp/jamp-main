package org.jamp.amp;

import java.io.IOException;

import org.jamp.amp.encoder.Decoder;
import org.jamp.amp.encoder.JampMessageDecoder;
import org.jamp.amp.stomp.MessageListener;


/** Works with STOMP, AMQP, to receive messages from a queue. */
public class MQMessageReceiver {
    
    MessageQueueConnection connection;
    String destination;
    SkeletonServiceInvoker invoker;
    Decoder<AmpMessage, String>  messageDecoder = null; 
    
    
    MQMessageReceiver(String connectionString, String login, String passcode, String destination, Class<?> serviceClass, Object instance) throws IOException {
        connection = AmpFactory.factory().createMQConnection(new MessageURL(connectionString));
        connection.connect(connectionString, login, passcode);
        this.destination = destination;
        
        
        connection.subscribe(destination, new StompMessageReceiverMessageListener());
        
        if (serviceClass!=null) {
            invoker = AmpFactory.factory().createJampServerSkeleton(serviceClass);
        } else {
            invoker = AmpFactory.factory().createJampServerSkeleton(instance);
            
        }
        
        messageDecoder = new JampMessageDecoder();
        
    }
    
    void handleTextMessage (String text) throws Exception{
        AmpMessage message = messageDecoder.decodeObject(text);
        invoker.invokeMessage(message);
    }
    
    /** This class is a callback like function class. I opted to not use an anonymous inner class to make this
     * easier to understand and port.
     * @author rick
     *
     */
    class StompMessageReceiverMessageListener implements MessageListener {
        
        public void onTextMessage(String text) throws Exception {
                handleTextMessage(text);
        }
        
        public void onBinaryMessage(String text)  throws Exception {
            
        }
    }


}
