package org.jamp.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.jamp.JSONLevelConversionBase;
import org.jamp.JampMessage;
import org.jamp.SkeletonServiceInvoker;

/** Used to invoke services on the side that acts as the server side. */
public class SkeletonServiceInvokerImpl implements SkeletonServiceInvoker {
    Class<?> clazz;
    Object instance;
    JSONLevelConversionBase converter = new JSONLevelConversionBase();

    public SkeletonServiceInvokerImpl() {
    }

    public SkeletonServiceInvokerImpl(Class<?> clazz, Object instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    private final Object thisObject() throws Exception {
        if (instance == null) {
            instance = clazz.newInstance();
        }
        return instance;
    }

    @Override
    public JampMessage invokeMessage(JampMessage message) throws Exception {

        Object thisObject = thisObject();
        Method method = findMethod(message, thisObject);
        List<Object> newArgs = converter.coerceFromListToFinalType(
                message.getArgs(), method.getParameterTypes());

        Object[] parameters = newArgs.toArray(new Object[newArgs.size()]);

        Exception exception = null;
        Object returnObject = null;

        try {
            returnObject = method.invoke(instance, parameters);
        } catch (Exception ex) {
            exception = ex;
        }

        JampMessage returnMessage = null;

        if (message.getMessageType() == JampMessage.Type.SEND
                && exception == null) {
            return null;
        }

        returnMessage = org.jamp.Factory.factory().createJampMessage();

        if (message.getMessageType() == JampMessage.Type.SEND
                && exception != null) {
            returnMessage.setMessageType(JampMessage.Type.ERROR);
        } else if (message.getMessageType() == JampMessage.Type.QUERY
                && exception != null) {
            returnMessage.setMessageType(JampMessage.Type.ERROR_QUERY);
        } else if (message.getMessageType() == JampMessage.Type.QUERY
                && exception == null) {
            returnMessage.setMessageType(JampMessage.Type.REPLY);
            returnMessage.setReplyObject(returnObject);

        }

        // Reverse from/to
        if (returnMessage != null) {
            returnMessage.setTo(message.getFrom());
            returnMessage.setFrom(message.getTo());
            returnMessage.setQueryId(message.getQueryId());
        }
        return returnMessage;

    }

    @SuppressWarnings("nls")
    private Method findMethod(JampMessage message, Object thisObject) {
        Method method = null;

        Method[] methods = thisObject.getClass().getMethods();

        System.out.println(thisObject.getClass().getName());
        System.out.println(message);

        for (Method m : methods) {
            if (m.getName().equals(message.getAction())) {
                System.out.println("method " + m.getName());
                if (m.getParameterTypes().length == message.getArgs().size()) {
                    method = m;
                    break;
                }
            }
        }
        if (method == null) {
            throw new IllegalStateException(
                    "Can't find method " + message.getAction()); //$NON-NLS-1$
        }
        return method;
    }

}
