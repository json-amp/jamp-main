package org.jamp.impl;

import java.io.IOException;

import org.jamp.Decoder;
import org.jamp.Encoder;
import org.jamp.JampMessage;
import org.jamp.JampMessageDecoder;
import org.jamp.JampMessageEncoder;
import org.jamp.JampMessageURL;
import org.jamp.MQMessageReciever;
import org.jamp.MessageQueueConnection;
import org.jamp.SkeletonServiceInvoker;

/** Works with STOMP, AMQP, to receive messages from a queue. */
public class MQMessageReceiverImpl implements MQMessageReciever {

    MessageQueueConnection connection;
    String destination;
    SkeletonServiceInvoker invoker;
    Decoder<JampMessage, CharSequence> messageDecoder = null;
    Encoder<String, JampMessage> messageEncoder;

    public MQMessageReceiverImpl(String connectionString, String login,
            String passcode, String destination, Class<?> serviceClass,
            Object instance, JampMessageDecoder aMessageDecoder,
            JampMessageEncoder aMessageEncoder) throws IOException {
        connection = org.jamp.Factory.factory().createMQConnection(
                new JampMessageURL(connectionString));
        connection.connect(connectionString, login, passcode);
        this.destination = destination;
        this.messageDecoder = aMessageDecoder;
        this.messageEncoder = aMessageEncoder;

        if (serviceClass != null) {
            invoker = org.jamp.Factory.factory().createJampServerSkeleton(
                    serviceClass);
        } else {
            invoker = org.jamp.Factory.factory().createJampServerSkeleton(
                    instance);
        }
        if (aMessageDecoder == null) {
            this.messageDecoder = org.jamp.Factory.factory()
                    .createJampMessageDecoder();
        }
        if (aMessageEncoder == null) {
            this.messageEncoder = org.jamp.Factory.factory()
                    .createJampMessageEncoder();
        }

        connection.subscribe(destination, new InternalMQListener());

    }

    @SuppressWarnings("nls")
    void handleTextMessage(String text) throws Exception {
        JampMessage message = messageDecoder.decode(text);
        JampMessage replyMessage = invoker.invokeMessage(message);

        if (message.getMessageType() == JampMessage.Type.QUERY) {
            connection.send(message.getToURL().getServiceURI() + "_return",
                    messageEncoder.encode(replyMessage));
        }
    }

    /**
     * This class is a callback like function class. I opted to not use an
     * anonymous inner class to make this easier to understand and port.
     * 
     * @author rick
     * 
     */
    class InternalMQListener implements MQMessageListener {

        @Override
        public void onTextMessage(String text) throws Exception {
            handleTextMessage(text);
        }

        @Override
        public void onBinaryMessage(String text) throws Exception {

        }
    }

}
