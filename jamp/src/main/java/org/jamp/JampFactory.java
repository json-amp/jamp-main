package org.jamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public interface JampFactory {

    JampMessageRouter createRouter();

    JampMessageSender lookupSender(String connectionString);

    void registerSender(String connectionString,
            JampMessageSender mqMessageSender);

    MQMessageSender createMQMessageSender(String connectionString,
            String login, String passcode, String destination)
            throws IOException;

    MQMessageReciever createMQReciever(String connectionString, String login,
            String passcode, String destination, Class<?> serviceClass,
            Object instance, JampMessageDecoder messageDecoder,
            JampMessageEncoder messageEncoder) throws IOException;

    MessageQueueConnection createMQConnection(JampMessageURL url);

    SkeletonServiceInvoker createJampServerSkeleton(Object instance);

    SkeletonServiceInvoker createJampServerSkeleton(Class<?> clazz);

    JampMessageSender createRESTSender();

    JampMessageSender createHTTPSender();

    MQMessageSender createMQMessageSender(String connectionString,
            String login, String passcode, String destination,
            JampMessageDecoder messageDecoder) throws IOException;

    JampMessageDecoder createJampMessageDecoder();

    JampMessageEncoder createJampMessageEncoder();

    JSONDecoder<Object> createJSONObjectDecoder();

    JSONDecoder<List<Object>> createJSONListDecoder();

    JampMessage createJampMessage();

    JampMessage createJampMessageFromBufferedReader(BufferedReader reader)
            throws Exception;

}