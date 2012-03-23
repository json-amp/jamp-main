package org.jamp.impl;

import java.util.HashMap;
import java.util.Map;

import org.jamp.JampMessage;
import org.jamp.JampMessageRouter;
import org.jamp.JampMessageSender;
import org.jamp.SkeletonServiceInvoker;

@SuppressWarnings("nls")
public class JampMessageRouterImpl implements JampMessageRouter {

    Map<String, JampMessageSender> senders = new HashMap<String, JampMessageSender>();
    Map<String, SkeletonServiceInvoker> serviceInvokers = new HashMap<String, SkeletonServiceInvoker>();
    Map<String, JampMessageRouter> routers = new HashMap<String, JampMessageRouter>();

    {
        MQMessageRouter router = org.jamp.Factory.factory().createMQMessageRouter();
        routers.put("stomp", router);
        routers.put("amqp", router);
        
        JampMessageSender httpSender = org.jamp.Factory.factory().createHTTPSender();
        JampMessageSender restSender = org.jamp.Factory.factory().createRESTSender();
        
        senders.put("http", httpSender);
        senders.put("rest", restSender);

    }

    @Override
    public void registerServiceInvoker(String name, Object object)
            throws Exception {
        
        SkeletonServiceInvoker serverSkeleton;
        if (object instanceof SkeletonServiceInvoker) {
            serviceInvokers.put(name, (SkeletonServiceInvoker) object);
        } else {
            if (object instanceof Class<?>) {
                serverSkeleton = org.jamp.Factory.factory()
                    .createJampServerSkeletonFromClass((Class<?>)object);
            } else {
                serverSkeleton = org.jamp.Factory.factory()
                .createJampServerSkeletonFromObject(object);
                
            }
            serviceInvokers.put(name, serverSkeleton);
        }
    }

    @Override
    public JampMessage routeMessage(JampMessage message) throws Exception {

        String scheme = message.getToURL().getScheme();

        if (scheme.equals("soa")) {
            SkeletonServiceInvoker invoker = serviceInvokers.get(message.getToURL().getServiceName());
            return invoker.invokeMessage(message);
        } else if (scheme.equals("rest") || scheme.equals("http") )  {
            JampMessageSender messageSender = senders.get(scheme);
            return messageSender.sendMessage(message);
        }
        JampMessageRouter messageRouter = routers.get(scheme);
        if (messageRouter != null) {
            return messageRouter.routeMessage(message);
        }
        throw new IllegalStateException("Unknown scheme=" + scheme);

    }

}
