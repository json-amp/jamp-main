package org.jamp.amp;

public interface SkeletonServiceInvoker {

    public abstract AmpMessage invokeMessage(AmpMessage message) throws Exception;

}