package org.jamp.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jamp.JampFactory;
import org.jamp.JampMessageRouter;
import org.jamp.JampMessageSender;
import org.jamp.JampMessageURL;
import org.jamp.MQMessageReceiver;
import org.jamp.MessageQueueConnection;
import org.jamp.SkeletonServiceInvoker;

public class JampFactoryImpl implements JampFactory {
    
    static JampFactoryImpl factory = new JampFactoryImpl();
    Map <String, JampMessageSender> mqSenders = Collections.synchronizedMap(new HashMap<String, JampMessageSender>());
    
    
    public static JampFactoryImpl factory() {
        //Look up in service locator as soon as there are two. 
        return factory;
    }
    
    public SkeletonServiceInvoker createJampServerSkeleton(Class<?> clazz) {
        //Look this up with a service locator design pattern, just as soon as there are two.
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl(clazz, null, null, null);
        return invoker;
    }
    
    public SkeletonServiceInvoker createJampServerSkeleton(Object instance) {
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl(null, instance, null, null);
        return invoker;
    }

    
    public MessageQueueConnection createMQConnection (JampMessageURL url) {
        
        if (url.getScheme().equals("stomp")) {
            return new StompConnection();
        } else {
            throw new IllegalStateException("Unsupported MQ connection type");
        }
    }
    
    public MQMessageReceiver createMQReciever (String connectionString, String login, String passcode, String destination, Class<?> serviceClass, Object instance) throws IOException {
        return new MQMessageReceiver(connectionString, login, passcode, destination, serviceClass, instance);
    }
    
    public MQMessageSender createMQMessageSender (String connectionString, String login, String passcode, String destination) throws IOException {
        return new MQMessageSender(connectionString, login, passcode, destination);
    }

    public void registerSender(String connectionString,
            JampMessageSender mqMessageSender) {
        this.mqSenders.put(connectionString, mqMessageSender);
    }
    
    public JampMessageSender lookupSender (String connectionString) {
        return this.mqSenders.get(connectionString);
    }
    
    public JampMessageRouter createRouter() {
        return new AmpMessageRouterImpl();
    }
}
