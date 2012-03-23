package org.jamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jamp.impl.HttpMessageSenderImpl;
import org.jamp.impl.JSONDecoderImpl;
import org.jamp.impl.JampMessageDecoderImpl;
import org.jamp.impl.JampMessageEncoderImpl;
import org.jamp.impl.JampMessageRouterImpl;
import org.jamp.impl.MQMessageReceiverImpl;
import org.jamp.impl.MQMessageRouter;
import org.jamp.impl.MQMessageSenderImpl;
import org.jamp.impl.Messages;
import org.jamp.impl.SkeletonServiceInvokerImpl;
import org.jamp.impl.StompConnection;

public class JampFactoryBase implements JampFactory {

    Map<String, JampMessageSender> mqSenders = Collections
            .synchronizedMap(new HashMap<String, JampMessageSender>());

    protected JampFactoryBase() {

    }

    @Override
    public SkeletonServiceInvoker createJampServerSkeletonFromClass(Class<?> clazz) {
        // Look this up with a service locator design pattern, just as soon as
        // there are two.
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl(
                clazz, null);
        return invoker;
    }

    @Override
    public SkeletonServiceInvoker createJampServerSkeletonFromObject(Object instance) {
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl(
                null, instance);
        return invoker;
    }

    @Override
    public MessageQueueConnection createMQConnection(JampMessageURL url) {

        if (url.getScheme().equals("stomp")) { //$NON-NLS-1$
            return new StompConnection();
        }
        throw new IllegalStateException(Messages.getString("JampFactoryBase.1")); //$NON-NLS-1$

    }

    @Override
    public void registerSender(String connectionString,
            JampMessageSender mqMessageSender) {
        this.mqSenders.put(connectionString, mqMessageSender);
    }

    @Override
    public JampMessageSender lookupSender(String connectionString) {
        return this.mqSenders.get(connectionString);
    }

    @Override
    public JampMessageRouter createRouter() {
        return new JampMessageRouterImpl();
    }

    @Override
    public JampMessageSender createRESTSender() {
        return new HttpMessageSenderImpl();
    }

    @Override
    public JampMessageSender createHTTPSender() {
        return new HttpMessageSenderImpl();
    }

    @Override
    public MQMessageSender createMQMessageSender(String connectionString,
            String login, String passcode)
            throws IOException {
        return new MQMessageSenderImpl(connectionString, login, passcode, null);
    }

    @Override
    public MQMessageReciever createMQReciever(String connectionString,
            String login, String passcode, String destination,
            Class<?> serviceClass, Object instance,
            JampMessageDecoder messageDecoder, JampMessageEncoder messageEncoder)
            throws IOException {
        return new MQMessageReceiverImpl(connectionString, login, passcode,
                destination, serviceClass, instance, messageDecoder,
                messageEncoder);
    }

    @Override
    public MQMessageSender createMQMessageSender(String connectionString,
            String login, String passcode, String destination,
            JampMessageDecoder messageDecoder) throws IOException {

        return new MQMessageSenderImpl(connectionString, login, passcode,
                messageDecoder);
    }

    @Override
    public JampMessageDecoder createJampMessageDecoder() {
        return new JampMessageDecoderImpl();
    }

    @Override
    public JampMessageEncoder createJampMessageEncoder() {
        return new JampMessageEncoderImpl();
    }

    @Override
    public JSONDecoder<Object> createJSONObjectDecoder() {
        return new JSONDecoderImpl<Object>();
    }

    @Override
    public JSONDecoder<List<Object>> createJSONListDecoder() {
        return new JSONDecoderImpl<List<Object>>();
    }

    @Override
    public JampMessage createJampMessageFromBufferedReader(BufferedReader reader)
            throws Exception {
        StringBuilder builder = new StringBuilder(512);
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return this.createJampMessageDecoder().decode(builder);
    }

    @Override
    public JampMessage createJampMessage() {
        return new JampMessage();
    }

    @Override
    public MQMessageRouter createMQMessageRouter() {
        return  new MQMessageRouter();
    }

}
