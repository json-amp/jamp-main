package org.jamp;

import java.io.Closeable;
import java.io.IOException;

import org.jamp.impl.MQMessageListener;

public interface MessageQueueConnection extends Closeable {

    void connect(String connectionString, String login, String passcode,
            Object... config) throws IOException;

    void send(String destination, Object message) throws IOException;

    void subscribe(String destination, MQMessageListener messageListener)
            throws IOException;

    void unsubscribe(String destination, MQMessageListener messageListener)
            throws IOException;

    @Override
    void close() throws IOException;

}