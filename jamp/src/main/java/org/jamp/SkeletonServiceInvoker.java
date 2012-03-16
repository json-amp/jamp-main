package org.jamp;

public interface SkeletonServiceInvoker {

    public abstract JampMessage invokeMessage(JampMessage message) throws Exception;

}