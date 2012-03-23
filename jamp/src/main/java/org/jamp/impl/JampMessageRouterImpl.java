package org.jamp.impl;

import java.util.HashMap;
import java.util.Map;

import org.jamp.JampMessage;
import org.jamp.JampMessageRouter;
import org.jamp.SkeletonServiceInvoker;

@SuppressWarnings("nls")
public class JampMessageRouterImpl implements JampMessageRouter {

    Map<String, SkeletonServiceInvoker> serviceInvokers = new HashMap<String, SkeletonServiceInvoker>();
    Map<String, JampMessageRouter> routers = new HashMap<String, JampMessageRouter>();

    {
        MQMessageRouter router = new MQMessageRouter(); // should not be new
                                                        // TODO put into factory
        routers.put("stomp", router);
        routers.put("amqp", router);
    }

    @Override
    public void registerServiceInvoker(String name, Object object)
            throws Exception {
        if (object instanceof SkeletonServiceInvoker) {
            serviceInvokers.put(name, (SkeletonServiceInvoker) object);
        } else {
            SkeletonServiceInvoker serverSkeleton = org.jamp.Factory.factory()
                    .createJampServerSkeleton(object);
            serviceInvokers.put(name, serverSkeleton);
        }
    }

    @Override
    public JampMessage routeMessage(JampMessage message) throws Exception {

        if (message.getToURL() == null) {
            // What do you do here?
            return null;
        }
        String scheme = message.getToURL().getScheme();

        if (scheme.equals("http") || scheme.equals("ws")) {
            return serviceInvokers.get(message.getToURL().getServiceName())
                    .invokeMessage(message);
        }
        JampMessageRouter messageRouter = routers.get(scheme);
        if (messageRouter != null) {
            return messageRouter.routeMessage(message);
        }
        throw new IllegalStateException("Unknown scheme=" + scheme);

    }

}
