package org.jamp.amp;

public interface SkeletonServiceInvoker {

    public abstract Message invokeMessage(Object payload) throws Exception;

}