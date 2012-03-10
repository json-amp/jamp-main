package org.jamp.amp;

/** Sends an Amp message via REST, STOMP, or WebSockets. */
public interface AmpMessageSender {
    AmpMessage sendMessage(AmpMessage message) throws Exception;
}
