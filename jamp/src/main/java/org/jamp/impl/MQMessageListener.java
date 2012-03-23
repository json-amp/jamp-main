package org.jamp.impl;

/** Used to listen to messages from a queue. */
public interface MQMessageListener {

    void onTextMessage(String text) throws Exception;

    void onBinaryMessage(String text) throws Exception;
}
