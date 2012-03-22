package org.jamp.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jamp.JampMessage;
import org.jamp.JampMessageSender;
import org.jamp.JampProxyCreator;


/** Creates a proxy object around an interface to expose method to send to remote hosts via STOMP, REST or websockets. 
 *  Takes a method encoder and an invoker.
 *  
 *  The invoker sends the message remotely.
 *  The encoder encodes the method via Hessian, JSON or BSON.
 **/
public class JampProxyCreatorImpl implements JampProxyCreator {
	JampMessageSender sender;
    static int messageId = 0;

	
	public JampProxyCreatorImpl (JampMessageSender invoker) {
        this.sender = invoker;
 	    
	}
    @Override
    public Object createProxy(Class<?> interface_) throws Exception {
        InvocationHandler handler = new InvocationHandler() {
            @SuppressWarnings("nls")
            @Override
            public Object invoke(Object arg0, Method method, Object[] params)
                    throws Throwable {

                JampMessage.Type messageType = method.getReturnType().equals(Void.TYPE) ? JampMessage.Type.SEND : JampMessage.Type.QUERY;
                JampMessage message = new JampMessage(messageType, null, null, method.getName(), params);
                messageId ++;
                message.setQueryId(System.currentTimeMillis() + "-" + messageId + "-"+ (int)(Math.random() * 100000));              
                sender.sendMessage(message);
                return null;
            }
        };
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interface_}, handler);
    }
	
}
