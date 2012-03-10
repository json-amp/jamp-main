package org.jamp.amp;

import java.util.HashMap;
import java.util.Map;

public class AmpMessageRouterImpl implements AmpMessageRouter {
    
    Map<String, SkeletonServiceInvoker> serviceInvokers = new HashMap<String, SkeletonServiceInvoker>();
    Map<String, AmpMessageRouter> routers = new HashMap<String, AmpMessageRouter>();
    
    {
        MQMessageRouter router = new MQMessageRouter();
        routers.put("stomp", router);
        routers.put("amqp", router);
    }
    

    
    public void registerServiceInvoker(String name, Object object) throws Exception {
        if (object instanceof SkeletonServiceInvoker) {
            serviceInvokers.put(name, (SkeletonServiceInvoker) object);
        } else {
            SkeletonServiceInvoker serverSkeleton = AmpFactory.factory().createJampServerSkeleton(object);
            serviceInvokers.put(name, serverSkeleton);
        }
    }

    public void registerServiceInvoker(String name, Class <?> clazz)  throws Exception {
        registerServiceInvoker(name, clazz.newInstance());
    }

    public AmpMessage routeMessage(AmpMessage message) throws Exception {
        
            String scheme = message.getToURL().getScheme();

            if (scheme.equals("http") || scheme.equals("ws")) {
                return serviceInvokers.get(message.getToURL().getServiceName()).invokeMessage(message);
            } else {
                AmpMessageRouter messageRouter = routers.get(scheme);
                if (messageRouter!=null) {
                    return messageRouter.routeMessage(message);
                } else {
                    throw new IllegalStateException("Unknown scheme=" + scheme);
                }
            }
    
        
    }

}
