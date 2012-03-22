package org.jamp;

public interface JampMessageSender {
    JampMessage sendMessage(JampMessage message) throws Exception;
}
