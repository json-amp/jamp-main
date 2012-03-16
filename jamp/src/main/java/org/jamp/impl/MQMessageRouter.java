package org.jamp.impl;

import org.jamp.JampMessage;
import org.jamp.JampMessageRouter;
import org.jamp.JampMessageSender;

public class MQMessageRouter implements JampMessageRouter {

    public JampMessage routeMessage(JampMessage message) throws Exception {
        JampMessageSender sender = JampFactoryImpl.factory().lookupSender(message.getToURL().connectionString());
        return sender.sendMessage(message);
    }

    public void registerServiceInvoker(String name, Object object)
            throws Exception {
        
    }

    public void registerServiceInvoker(String name, Class<?> clazz)
            throws Exception {
        
    }

}
