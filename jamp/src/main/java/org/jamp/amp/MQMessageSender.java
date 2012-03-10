package org.jamp.amp;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jamp.amp.encoder.Decoder;
import org.jamp.amp.encoder.JampMessageDecoder;
import org.jamp.amp.stomp.MessageListener;



/** Works with STOMP or AMQP to send a message. */
public class MQMessageSender implements AmpMessageSender {

    MessageQueueConnection connection;
    Decoder<AmpMessage, String>  messageDecoder = new JampMessageDecoder();

    
    MQMessageSender(String connectionString, String login, String passcode, String destination) throws IOException {
        MessageURL url = new MessageURL(connectionString);
        connection = AmpFactory.factory().createMQConnection(url);
        connection.connect(url.connectionString(), login, passcode);
        AmpFactory.factory().registerSender(url.connectionString(), this);
    }
    
 
    public AmpMessage sendMessage(AmpMessage message) throws Exception {
        connection.send(message.getToURL().getServiceURI(), message.getPayload());
        
        if (message.getMessageType()==AmpMessage.Type.SEND) {
            return null;
        } else {
            return blockUntilReturn(message);
        
        }

        
    }


    private AmpMessage blockUntilReturn(AmpMessage message) throws IOException {
        final BlockingQueue<AmpMessage> queue = new ArrayBlockingQueue<AmpMessage>(1);
        
        connection.subscribe(message.getToURL().getServiceURI() + "_return", new MessageListener() {
            
            public void onTextMessage(String text) throws Exception {
                AmpMessage message = messageDecoder.decodeObject(text);
                queue.offer(message, 1, TimeUnit.SECONDS);

            }
            
            public void onBinaryMessage(String text) throws Exception {
                // TODO Auto-generated method stub
                
            }
        });
        
        
        AmpMessage returnMessage; 
        
        try {
            returnMessage = queue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            returnMessage = null;
        }
        if (returnMessage==null) {
            throw new IOException ("Message return timed out");
        }
        return returnMessage;
        
    }

}
