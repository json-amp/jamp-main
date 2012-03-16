package org.jamp;

public interface JampMessageRouter {
    JampMessage routeMessage(JampMessage message) throws Exception; 
    void registerServiceInvoker(String name, Object object) throws Exception;
    void registerServiceInvoker(String name, Class <?> clazz)  throws Exception;

}
