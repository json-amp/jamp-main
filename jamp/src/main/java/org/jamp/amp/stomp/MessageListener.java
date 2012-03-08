package org.jamp.amp.stomp;

public interface  MessageListener {
    
    void onTextMessage(String text) throws Exception;
    
    void onBinaryMessage(String text) throws Exception;
}
