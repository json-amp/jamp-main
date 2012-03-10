package org.jamp.amp;

import java.io.Closeable;
import java.io.IOException;

import org.jamp.amp.stomp.MessageListener;

public interface MessageQueueConnection extends Closeable{

    public abstract void connect(String connectionString, String login, String passcode, Object... config) throws IOException;

    public abstract void send(String destination, Object message)
            throws IOException;

    public abstract void subscribe(String destination,
            MessageListener messageListener) throws IOException;

    public abstract void unsubscribe(String destination,
            MessageListener messageListener) throws IOException;

    public abstract void close() throws IOException;

}