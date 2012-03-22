package org.jamp;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jamp.impl.Messages;


public class JSONLevelConversionBase implements JSONLevelConversion {

    
    @Override
    public List<Object> coerceFromListToFinalType(List<Object> list,
            Class<?>[] concreteTypes)  throws Exception  {
        
        List<Object> parameters = new ArrayList<Object>(concreteTypes.length);
        
        for (int index=0; index<concreteTypes.length; index++) {
            Object inputArgument = list.get(index);
            Class<?> paraType = concreteTypes[index];
            parameters.add(coerceArgument(inputArgument, paraType, null));
        }
        
        return parameters;
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object coerceArgument(Object inputArgument, Class<?> paraType, Type[] types)  throws Exception {
        if (inputArgument instanceof Map) {
            return coerceObject((Map<String, Object>) inputArgument, paraType);
        } else if (inputArgument instanceof List) {
            return coerceList((List<Object>)inputArgument, paraType, types);  
        } else if (inputArgument instanceof String) {
            return coerceString((String)inputArgument);
        } else if (inputArgument instanceof Boolean) {
            return coerceBoolean((Boolean)inputArgument);
        } else if (paraType.isPrimitive() || inputArgument instanceof Number){
                return coerceNumber(inputArgument, paraType);
        } else {
            return coerceUnknown(inputArgument);
        }
    }

    @Override
    public Number coerceNumber(Object inputArgument, Class<?> paraType) {
        Number number = (Number) inputArgument;
        if (paraType==int.class || paraType==Integer.class) {
            return number.intValue();
        } else if (paraType==double.class || paraType==Double.class){
            return number.doubleValue();
        } else if (paraType==float.class  || paraType==Float.class){
            return number.floatValue();
        } else if (paraType==short.class  || paraType==Short.class) {
            return number.shortValue();
        } else if (paraType==byte.class  || paraType==Byte.class) {
            return number.byteValue();
        }
        return null;
    }

    @Override
    public Boolean coerceBoolean(Boolean inputArgument) {
        return inputArgument;
    }

    @Override
    public String coerceString(String inputArgument) {
        return inputArgument;
    }
    
    @Override
    public Object coerceUnknown(Object inputArgument) {
        return inputArgument;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object coerceList(List<Object> list, Class<?> paraType, Type[] types) throws Exception {
        if (paraType.isArray()) {
            return coerceListToArray(list, paraType);
        } 
        return coerceListToCollection(list, paraType, types);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Object> coerceListToCollection(List<Object> list,
            Class<?> paraType, Type[] types) throws ClassNotFoundException,
            Exception {
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
            }
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
        
        if (collection == null) {
            return null;
        }
            
        
        Class<?> componentType = null;
        
        if (types==null && list.size()>=1 && list.get(0) instanceof Map<?, ?>) {
            Map <String, Object> map = (Map<String, Object>) list.get(0);
            String className = (String) map.get("java_type"); //$NON-NLS-1$
            componentType = Class.forName(className);
        } else if (types!=null) {
            ParameterizedType pType = (ParameterizedType) types[0];
            componentType = (Class<?>) pType.getActualTypeArguments()[0];
        } else if (list.size()>=1){
            componentType = list.get(0).getClass();
        }
        for (Object object : list) {
            object = coerceArgument(object, componentType, null);
            collection.add(object);
        }
        
        return collection;
    }

    @Override
    public Object coerceListToArray(List<Object> list, Class<?> paraType)
            throws Exception {
        Class<?> componentType = paraType.getComponentType();
        Object array = Array.newInstance(componentType, list.size());
        int index=0;
        for (Object object : list) {
            object = coerceArgument(object, componentType, null);
            Array.set(array, index, object);
            index++;
        }
        return array;
    }
    
    @Override
    public Object coerceObject(Map<String, Object> inputArgument, Class<?> paraType) throws Exception {
        Object instance = null;
        if (paraType.isInterface()) {
            String jclass = (String) inputArgument.get("java_type"); //$NON-NLS-1$
            instance = Class.forName(jclass).newInstance();
        } else {
            instance = paraType.newInstance();
        }
        
        Set<String> props = new HashSet<String>(inputArgument.keySet());
        props.remove("java_type"); //$NON-NLS-1$
        
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
                if (field!=null) {
                    field.setAccessible(true);
                    field.set(instance, coerceArgument(inputArgument.get(field.getName()), field.getType(), new Type[]{field.getGenericType()}));
                    props.remove(field.getName());
                }
            } catch (Exception ex) {
                // ok if it did not work
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
            Object value, Type[] types) throws Exception {
   
        Object coercedValue = null;
        try {
            coercedValue = coerceArgument(value, type, types);
            m.invoke(instance, new Object[]{coercedValue});
        }catch (Exception ex) {
            
            throw new IllegalStateException(
            String.format(Messages.getString("JSONLevelConversionBase.0"),   //$NON-NLS-1$
                    m.getName(), value == null ? "null": value.getClass(),  //$NON-NLS-1$
                            coercedValue==null? "null" : coercedValue.getClass(), value, coercedValue),   //$NON-NLS-1$
                    ex); 
        }
    }
    
    @SuppressWarnings("nls")
    private Method[] getSetterMethods(Class<?> paraType) {
        List<Method> setters = new ArrayList<Method>(12); 
        Method[] methods = paraType.getMethods();
        for (int index=0; index < methods.length; index++){
            Method m = methods[index];
            String name = m.getName() + "safe no null"; //$NON-NLS-1$
            if (m.getReturnType()==void.class && Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length==1 && name.startsWith("set")){ //$NON-NLS-1$
                setters.add(m);
            }
        }
        return setters.toArray(new Method[setters.size()]);
    }

}
