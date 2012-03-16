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
	    Object [] parameters = coerceArgumentList(message.getArgs(), method.getParameterTypes());
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
    private Object[] coerceArgumentList(List<Object> args,
            Class<?>[] parameterTypes)  throws Exception  {
        
        List<Object> parameters = new ArrayList<Object>(parameterTypes.length);
        
        for (int index=0; index<parameterTypes.length; index++) {
            Object inputArgument = args.get(index);
            Class<?> paraType = parameterTypes[index];
            parameters.add(coerceArgument(inputArgument, paraType, null));
        }
        
        return parameters.toArray(new Object[parameterTypes.length]);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object coerceArgument(Object inputArgument, Class<?> paraType, Type[] types)  throws Exception {
        if (inputArgument instanceof Map && paraType.isAssignableFrom(Map.class)) {
            return new HashMap((Map)inputArgument);
        } else if (inputArgument instanceof Map) {
            return coerceObject((Map<String, Object>) inputArgument, paraType);
        } else if (inputArgument instanceof List) {
            return coerceList((List<Object>)inputArgument, paraType, types);  
        } else if (inputArgument instanceof String) {
            return inputArgument;
        } else if (inputArgument instanceof Boolean) {
            return inputArgument;
        } else if (paraType.isPrimitive()){
            if (paraType == boolean.class){
                return inputArgument;
            } else {
                Number number = (Number) inputArgument;
                if (paraType==int.class) {
                    return number.intValue();
                } else if (paraType==double.class){
                    return number.doubleValue();
                } else if (paraType==float.class){
                    return number.floatValue();
                } else if (paraType==short.class) {
                    return number.shortValue();
                } else if (paraType==byte.class) {
                    return number.byteValue();
                }
            }
        } else {
            return inputArgument;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Object coerceList(List<Object> list, Class<?> paraType, Type[] types) throws Exception {
        if (paraType.isArray()) {
            Class<?> componentType = paraType.getComponentType();
            Object array = Array.newInstance(componentType, list.size());
            int index=0;
            for (Object object : list) {
                object = coerceArgument(object, componentType, null);
                Array.set(array, index, object);
                index++;
            }
            return array;
        } else {
            Collection<Object> collection = null;
            
            if (paraType.isInterface()) { 
                if (paraType.isAssignableFrom(List.class)) {
                    collection = new ArrayList<Object>(list.size());  
                } else if (paraType.isAssignableFrom(Set.class)) {
                    collection = new HashSet<Object>(list.size());
                }  
            } else {
                if (Modifier.isAbstract( paraType.getModifiers() )) {
                    return null;
                } else {
                    Object object = null;
                    try {
                        object = paraType.newInstance();
                    }catch (Exception ex) {
                        object = null;
                    }              
                    if (object!=null && object instanceof Collection){
                        collection = (Collection<Object>) object;
                    }
                }
            }
                
            //    new ArrayList<Object>(list.size());
            
            Class<?> componentType = null;
            
            if (types==null && list.size()>=1) {
                Map <String, Object> map = (Map<String, Object>) list.get(0);
                String className = (String) map.get("java_type");
                componentType = Class.forName(className);
            } else {
                ParameterizedType pType = (ParameterizedType) types[0];
                componentType = (Class<?>) pType.getActualTypeArguments()[0];
            }
            for (Object object : list) {
                object = coerceArgument(object, componentType, null);
                collection.add(object);
            }
            
            return collection;
            
        }
    }
    private Object coerceObject(Map<String, Object> inputArgument, Class<?> paraType) throws Exception {
        Object instance = null;
        if (paraType.isInterface()) {
            String jclass = (String) inputArgument.get("java_class");
            instance = Class.forName(jclass).newInstance();
        } else {
            instance = paraType.newInstance();
        }
        
        Set<String> props = new HashSet<String>(inputArgument.keySet());
        props.remove("java_type");
        
        Method[] setterMethods = getSetterMethods(paraType);
        for (Method m : setterMethods) {
            String propName = m.getName().substring(3);
            propName = propName.substring(0,1).toLowerCase() + propName.substring(1);
            props.remove(propName); //remove it if we found the setter
            Object value = inputArgument.get(propName);
            Class<?> type = m.getParameterTypes()[0];
            Type[] types = m.getGenericParameterTypes();
            invokeSetterMethod(type, instance, m, value, types);
        }
        
        
        /* Remaining props that do not have setter methods */
        for (String propName : props) {
            Field field = paraType.getDeclaredField(propName);
       
            Class<?> current = paraType;
            while (field==null) {
                if (current==Object.class) {
                    break;
                }
                try {
                    field = current.getDeclaredField(propName);
                } catch (Exception e) {
                    
                }
                if (field==null) {
                    current = current.getSuperclass();
                }
            }
            try {           
                field.setAccessible(true);
                field.set(instance, coerceArgument(inputArgument.get(field.getName()), field.getType(), new Type[]{field.getGenericType()}));
                props.remove(field.getName());
            } catch (Exception ex) {
                // ok if it did not work. Should maybe do log warn.
            }
        }
        
        if (instance instanceof Map) {
            @SuppressWarnings("unchecked")
            Map <String, Object> map = (Map <String, Object>) instance;
            for (String propName : props) {
                props.remove(propName);
                map.put(propName, inputArgument.get(propName));
            }
        }
        return instance;
    }
    private void invokeSetterMethod(Class<?> type, Object instance, Method m,
            Object value, Type[] types) throws IllegalAccessException,
            InvocationTargetException, Exception {
   
        Object coercedValue = null;
        try {
            coercedValue = coerceArgument(value, type, types);
            m.invoke(instance, new Object[]{coercedValue});
        }catch (Exception ex) {
            System.out.printf("Method name = %s, valueType=%s, corercedValueType=%s, value=%s, cvalue=%s \n", 
                    m.getName(), value == null ? "null": value.getClass(), coercedValue==null? "null" : coercedValue.getClass(), value, coercedValue);
            
            throw ex;
        }
    }
    
    private Method[] getSetterMethods(Class<?> paraType) {
        List<Method> setters = new ArrayList<Method>(12); 
        Method[] methods = paraType.getMethods();
        for (int index=0; index < methods.length; index++){
            Method m = methods[index];
            String name = m.getName() + "safe no null";
            if (m.getReturnType()==void.class && Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length==1 && name.startsWith("set")){
                setters.add(m);
            }
        }
        return setters.toArray(new Method[setters.size()]);
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
