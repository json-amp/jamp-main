package org.jamp.impl;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jamp.JampMessage;
import org.jamp.JampMessageDecoder;
import org.jamp.MQMessageSender;
import org.jamp.MessageQueueConnection;
import org.jamp.JampMessageURL;

/** Works with STOMP or AMQP to send a message. */
public class MQMessageSenderImpl implements MQMessageSender {

    MessageQueueConnection connection;
    JampMessageDecoder messageDecoder;

    public MQMessageSenderImpl(String connectionString, String login,
            String passcode, JampMessageDecoder aMessageDecoder)
            throws IOException {
        JampMessageURL url = new JampMessageURL(connectionString);
        connection = org.jamp.Factory.factory().createMQConnection(url);
        connection.connect(url.connectionString(), login, passcode);
        org.jamp.Factory.factory().registerSender(url.connectionString(), this);
        if (aMessageDecoder == null) {
            messageDecoder = org.jamp.Factory.factory()
                    .createJampMessageDecoder();
        } else {
            messageDecoder = aMessageDecoder;
        }
    }

    @Override
    public JampMessage sendMessage(JampMessage message) throws Exception {
        connection.send(message.getToURL().getServiceURI(),
                message.getPayload());

        if (message.getMessageType() == JampMessage.Type.SEND) {
            return null;
        }
        return blockUntilReturn(message);

    }

    @SuppressWarnings("nls")
    private JampMessage blockUntilReturn(final JampMessage message)
            throws IOException {
        final BlockingQueue<JampMessage> queue = new ArrayBlockingQueue<JampMessage>(
                1);

        connection.subscribe(message.getToURL().getServiceURI() + "_return",
                new MQMessageListener() {

                    @Override
                    public void onTextMessage(String text) throws Exception {
                        JampMessage replyMessage = messageDecoder.decode(text);
                        if (message.getQueryId().equals(
                                replyMessage.getQueryId())) {
                            queue.offer(replyMessage, 1, TimeUnit.SECONDS);
                        } else {
                            /*
                             * This should never happen for example, but could
                             * happen in the real world This is why the test
                             * STOMP server should probably support ACK, NACK,
                             * but not going to for now.
                             */
                            connection.send(message.getToURL().getServiceURI()
                                    + "_return", message.getPayload());
                        }

                    }

                    @Override
                    public void onBinaryMessage(String text) throws Exception {
                    }
                });

        JampMessage returnMessage = null;

        try {
            returnMessage = queue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("Message return timed out");
        }
        return returnMessage;

    }

}
