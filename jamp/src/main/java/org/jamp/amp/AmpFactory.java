package org.jamp.amp;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jamp.amp.encoder.Decoder;
import org.jamp.amp.stomp.StompConnection;

public class AmpFactory {
    
    static AmpFactory factory = new AmpFactory();
    Map <String, AmpMessageSender> mqSenders = Collections.synchronizedMap(new HashMap<String, AmpMessageSender>());
    
    
    public static AmpFactory factory() {
        //Look up in service locator as soon as there are two. 
        return factory;
    }
    
    public SkeletonServiceInvoker createJampServerSkeleton(Class<?> clazz) {
        //Look this up with a service locator design pattern, just as soon as there are two.
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl();
        invoker.clazz = clazz;
        return invoker;
    }
    
    public SkeletonServiceInvoker createJampServerSkeleton(Object instance) {
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl();
        invoker.instance = instance;
        return invoker;
    }


    public SkeletonServiceInvoker createCustomServerSkeleton(Object instance, Decoder<Object,Object> arguments) {
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl();
        invoker.instance = instance;
        invoker.argumentDecoder = arguments;
        return invoker;
    }

    public SkeletonServiceInvoker createCustomServerSkeleton(Class<?> clazz, Decoder<Object,Object> arguments) {
        SkeletonServiceInvokerImpl invoker = new SkeletonServiceInvokerImpl();
        invoker.clazz = clazz;
        invoker.argumentDecoder = arguments;
        return invoker;
    }
    
    public MessageQueueConnection createMQConnection (MessageURL url) {
        
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
            AmpMessageSender mqMessageSender) {
        this.mqSenders.put(connectionString, mqMessageSender);
    }
    
    public AmpMessageSender lookupSender (String connectionString) {
        return this.mqSenders.get(connectionString);
    }
    
    public AmpMessageRouter createRouter() {
        return new AmpMessageRouterImpl();
    }
}
