package org.jamp.amp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jamp.amp.encoder.JampMethodEncoder;
import org.jamp.amp.encoder.MethodEncoder;


/** Creates a proxy object around an interface to expose method to send to remote hosts via STOMP, REST or websockets. 
 *  Takes a method encoder and an invoker.
 *  
 *  The invoker sends the message remotely.
 *  The encoder encodes the method via Hessian, JSON or BSON.
 **/
public class AmpProxyCreator {
	@SuppressWarnings("rawtypes")
    MethodEncoder methodEncoder;
	AmpMessageSender invoker;
	
	public AmpProxyCreator (AmpMessageSender invoker) {
        this.methodEncoder = new JampMethodEncoder();
        this.invoker = invoker;
 	    
	}
	public AmpProxyCreator(@SuppressWarnings("rawtypes") MethodEncoder methodEncoder, AmpMessageSender invoker) {
		this.methodEncoder = methodEncoder;
		this.invoker = invoker;
	}
	
	public Object createProxy(final String interface_, final String toURL, final String fromURL) throws Exception {
		return createProxy(Class.forName(interface_), toURL, fromURL);
	}
	
	public Object createProxy(final Class<?> interface_, final String toURL, final String fromURL) throws Exception {

		InvocationHandler handler = new InvocationHandler() {
			public Object invoke(Object arg0, Method method, Object[] method_params)
					throws Throwable {
				Object payload = methodEncoder.encodeMethodForSend(method, method_params, toURL, fromURL);
				System.out.println(payload);
				invoker.sendMessage(method.getName(), payload, toURL, fromURL);
				return null;
			}
		};
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interface_}, handler);
	}
	
}
