package org.jamp.impl;

import org.jamp.JampMessage;
import org.jamp.JampMessageRouter;
import org.jamp.JampMessageSender;

public class MQMessageRouter implements JampMessageRouter {

    @Override
    public JampMessage routeMessage(JampMessage message) throws Exception {
        JampMessageSender sender = org.jamp.Factory.factory().lookupSender(message.getToURL().connectionString());
        return sender.sendMessage(message);
    }

    @Override
    public void registerServiceInvoker(String name, Object object)
            throws Exception {
        
    }

}
