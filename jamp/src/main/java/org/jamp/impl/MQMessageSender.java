package org.jamp.impl;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jamp.JampMessage;
import org.jamp.JampMessageSender;
import org.jamp.Decoder;
import org.jamp.MessageQueueConnection;
import org.jamp.JampMessageURL;



/** Works with STOMP or AMQP to send a message. */
public class MQMessageSender implements JampMessageSender {

    MessageQueueConnection connection;
    Decoder<JampMessage, String>  messageDecoder = new JampMessageDecoder();


    
    public MQMessageSender(String connectionString, String login, String passcode, String destination) throws IOException {
        JampMessageURL url = new JampMessageURL(connectionString);
        connection = JampFactoryImpl.factory().createMQConnection(url);
        connection.connect(url.connectionString(), login, passcode);
        JampFactoryImpl.factory().registerSender(url.connectionString(), this);
    }
    
 
    public JampMessage sendMessage(JampMessage message) throws Exception {
        connection.send(message.getToURL().getServiceURI(), message.getPayload());
        
        if (message.getMessageType()==JampMessage.Type.SEND) {
            return null;
        } else {
            return blockUntilReturn(message);
        
        }

        
    }


    private JampMessage blockUntilReturn(final JampMessage message) throws IOException {
        final BlockingQueue<JampMessage> queue = new ArrayBlockingQueue<JampMessage>(1);
        
        connection.subscribe(message.getToURL().getServiceURI() + "_return", new MQMessageListener() {
            
            public void onTextMessage(String text) throws Exception {
                JampMessage replyMessage = messageDecoder.decodeObject(text);
                if (message.getQueryId().equals(replyMessage.getQueryId())) {
                    queue.offer(replyMessage, 1, TimeUnit.SECONDS);                    
                } else {
                    /* This should never happen for example, but could happen in the real world
                     * This is why the test STOMP server should probably support ACK, NACK, but not going to for now.
                     */
                    connection.send(message.getToURL().getServiceURI() + "_return", message.getPayload());
                }

            }
            
            public void onBinaryMessage(String text) throws Exception {
                // TODO Auto-generated method stub
                
            }
        });
        
        
        JampMessage returnMessage = null;
        
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
