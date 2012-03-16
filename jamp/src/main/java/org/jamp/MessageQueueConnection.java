package org.jamp;

import java.io.Closeable;
import java.io.IOException;

import org.jamp.impl.MQMessageListener;

public interface MessageQueueConnection extends Closeable{

    public abstract void connect(String connectionString, String login, String passcode, Object... config) throws IOException;

    public abstract void send(String destination, Object message)
            throws IOException;

    public abstract void subscribe(String destination,
            MQMessageListener messageListener) throws IOException;

    public abstract void unsubscribe(String destination,
            MQMessageListener messageListener) throws IOException;

    public abstract void close() throws IOException;

}