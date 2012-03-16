package org.jamp;

/** Sends an Amp message via REST, STOMP, or WebSockets. */
public interface JampMessageSender {
    JampMessage sendMessage(JampMessage message) throws Exception;
}
