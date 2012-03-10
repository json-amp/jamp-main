package org.jamp.amp;

public class MQMessageRouter implements AmpMessageRouter {

    public AmpMessage routeMessage(AmpMessage message) throws Exception {
        AmpMessageSender sender = AmpFactory.factory().lookupSender(message.getToURL().connectionString());
        return sender.sendMessage(message);
    }

    public void registerServiceInvoker(String name, Object object)
            throws Exception {
        
    }

    public void registerServiceInvoker(String name, Class<?> clazz)
            throws Exception {
        
    }

}
