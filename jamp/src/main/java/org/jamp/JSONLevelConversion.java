package org.jamp;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JSONLevelConversion {

    Object coerceObject(Map<String, Object> inputArgument, Class<?> paraType)
            throws Exception;

    Object coerceListToArray(List<Object> list, Class<?> paraType)
            throws Exception;

    Collection<Object> coerceListToCollection(List<Object> list,
            Class<?> paraType, Type[] types) throws ClassNotFoundException,
            Exception;

    Object coerceList(List<Object> list, Class<?> paraType, Type[] types)
            throws Exception;

    Object coerceUnknown(Object inputArgument);

    String coerceString(String inputArgument);

    Boolean coerceBoolean(Boolean inputArgument);

    Number coerceNumber(Object inputArgument, Class<?> paraType);

    List<Object> coerceFromListToFinalType(List<Object> list,
            Class<?>[] concreteTypes) throws Exception;

}