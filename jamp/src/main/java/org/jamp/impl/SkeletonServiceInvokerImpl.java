package org.jamp.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jamp.JampMessage;
import org.jamp.Decoder;
import org.jamp.SkeletonServiceInvoker;



/** Used to invoke services on the side that acts as the server side. */
public class SkeletonServiceInvokerImpl implements SkeletonServiceInvoker {
    Class<?> clazz;
    Object instance;
    JSONLevelConversion converter = new JSONLevelConversion();

    public SkeletonServiceInvokerImpl() {
    }

    public SkeletonServiceInvokerImpl(Class<?> clazz, Object instance,
            Decoder<JampMessage, String> messageDecoder, Decoder<Object, Object> argumentDecoder) {
        this.clazz = clazz;
        this.instance = instance;
    }
	

	private final Object thisObject() throws Exception {
	    if (instance==null) {
	        instance = clazz.newInstance();
	    }
	    return instance;
	}
	
	

    public JampMessage invokeMessage(JampMessage message) throws Exception {
 	    
	    	    
	    Object thisObject = thisObject();
	    Method method = findMethod(message, thisObject);
	    Object [] parameters = converter.coerceFromListToFinalType(message.getArgs(), method.getParameterTypes());
	    Exception exception = null;
	    Object returnObject = null;
	    
	    try {
	        returnObject = method.invoke(instance, parameters);
	    } catch (Exception ex) {
	        exception = ex;
	    }
	    
	    JampMessage returnMessage = null;
	    
	    if (message.getMessageType()==JampMessage.Type.SEND && exception == null) {
	        returnMessage = null;
	        return null;
	    } else if (message.getMessageType()==JampMessage.Type.SEND && exception != null) {
	        returnMessage = new JampMessage();
	        returnMessage.setMessageType(JampMessage.Type.ERROR);
        } else if (message.getMessageType()==JampMessage.Type.QUERY && exception != null) {
            returnMessage = new JampMessage();
            returnMessage.setMessageType(JampMessage.Type.ERROR_QUERY);
        } else if (message.getMessageType()==JampMessage.Type.QUERY && exception == null) {
            returnMessage = new JampMessage();
            returnMessage.setMessageType(JampMessage.Type.REPLY);
            returnMessage.setReplyObject(returnObject);
            
        } 
	        
	    
	    //Reverse from/to
	    returnMessage.setTo(message.getFrom());
	    returnMessage.setFrom(message.getTo());
	    returnMessage.setQueryId(message.getQueryId());
	    	    
	    return returnMessage;
	    
	}
    
    private Method findMethod(JampMessage message,
            Object thisObject) {
        Method method = null;
        
        Method[] methods = thisObject.getClass().getMethods();
        
        System.out.println(message);

        for (Method m : methods) {
            if (m.getName().equals(message.getAction())){
                if (m.getParameterTypes().length==message.getArgs().size()) {
                    method = m;
                    break;
                }
            }
        }
        if (method == null) {
            throw new IllegalStateException("Method for message not found");
        }
        return method;
    }

}
