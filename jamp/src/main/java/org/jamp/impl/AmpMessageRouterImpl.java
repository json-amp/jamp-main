package org.jamp.impl;

import java.util.HashMap;
import java.util.Map;

import org.jamp.JampMessage;
import org.jamp.JampMessageRouter;
import org.jamp.SkeletonServiceInvoker;

public class AmpMessageRouterImpl implements JampMessageRouter {
    
    Map<String, SkeletonServiceInvoker> serviceInvokers = new HashMap<String, SkeletonServiceInvoker>();
    Map<String, JampMessageRouter> routers = new HashMap<String, JampMessageRouter>();
    
    {
        MQMessageRouter router = new MQMessageRouter();
        routers.put("stomp", router);
        routers.put("amqp", router);
    }
    

    
    public void registerServiceInvoker(String name, Object object) throws Exception {
        if (object instanceof SkeletonServiceInvoker) {
            serviceInvokers.put(name, (SkeletonServiceInvoker) object);
        } else {
            SkeletonServiceInvoker serverSkeleton = JampFactoryImpl.factory().createJampServerSkeleton(object);
            serviceInvokers.put(name, serverSkeleton);
        }
    }

    public void registerServiceInvoker(String name, Class <?> clazz)  throws Exception {
        registerServiceInvoker(name, clazz.newInstance());
    }

    public JampMessage routeMessage(JampMessage message) throws Exception {
        
            String scheme = message.getToURL().getScheme();

            if (scheme.equals("http") || scheme.equals("ws")) {
                return serviceInvokers.get(message.getToURL().getServiceName()).invokeMessage(message);
            } else {
                JampMessageRouter messageRouter = routers.get(scheme);
                if (messageRouter!=null) {
                    return messageRouter.routeMessage(message);
                } else {
                    throw new IllegalStateException("Unknown scheme=" + scheme);
                }
            }
    
        
    }

}
