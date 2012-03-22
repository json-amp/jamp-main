package org.jamp;

import java.io.IOException;
import java.io.Reader;

import org.jamp.impl.JampFactoryImpl;
import org.jamp.impl.JampMessageDecoder;
import org.jamp.impl.JampMessageEncoder;
import org.jamp.impl.MQMessageListener;


/** Works with STOMP, AMQP, to receive messages from a queue. */
public class MQMessageReceiver {
    
    MessageQueueConnection connection;
    String destination;
    SkeletonServiceInvoker invoker;
    Decoder<JampMessage, CharSequence>  messageDecoder = null; 
    
    
    public MQMessageReceiver(String connectionString, String login, String passcode, String destination, Class<?> serviceClass, Object instance) throws IOException {
        connection = JampFactoryImpl.factory().createMQConnection(new JampMessageURL(connectionString));
        connection.connect(connectionString, login, passcode);
        this.destination = destination;
        
        
        connection.subscribe(destination, new InternalMQListener());
        
        if (serviceClass!=null) {
            invoker = JampFactoryImpl.factory().createJampServerSkeleton(serviceClass);
        } else {
            invoker = JampFactoryImpl.factory().createJampServerSkeleton(instance);
            
        }
        
        messageDecoder = new JampMessageDecoder();
        
    }
    
    void handleTextMessage (String text) throws Exception{
        JampMessage message = messageDecoder.decode(text);
        JampMessage replyMessage = invoker.invokeMessage(message);
        
        JampMessageEncoder encoder = new JampMessageEncoder();
        if (message.getMessageType()==JampMessage.Type.QUERY) {
            connection.send(message.getToURL().getServiceURI() + "_return", encoder.encodeObject(replyMessage));
        }
    }
    
    /** This class is a callback like function class. I opted to not use an anonymous inner class to make this
     * easier to understand and port.
     * @author rick
     *
     */
    class InternalMQListener implements MQMessageListener {
        
        public void onTextMessage(String text) throws Exception {
                handleTextMessage(text);
        }
        
        public void onBinaryMessage(String text)  throws Exception {
            
        }
    }


}
