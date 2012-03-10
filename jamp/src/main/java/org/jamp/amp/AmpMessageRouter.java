package org.jamp.amp;

public interface AmpMessageRouter {
    AmpMessage routeMessage(AmpMessage message) throws Exception; 
    void registerServiceInvoker(String name, Object object) throws Exception;
    void registerServiceInvoker(String name, Class <?> clazz)  throws Exception;

}
