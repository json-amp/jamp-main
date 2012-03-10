package org.jamp.amp;



/** Represents an AMP message. */
public class AmpMessage {
    
    
    public enum Type {SEND, QUERY, REPLY, ERROR_QUERY, ERROR};

    Type messageType;
	String to;
	String from;
	String methodName;
	Object args;
	Object returnObject;
	MessageURL toURL;
	MessageURL fromURL;
    Object payload;


    public AmpMessage() {
        
    }
    
    public AmpMessage(String sMessageType, String to, String from,
			String methodName, Object args, Object payload) {	    
        this(Enum.valueOf(Type.class, sMessageType.toUpperCase().replace('-', '_')),to,from,methodName, args, payload);
	}

    public AmpMessage(Type messageType, String to, String from,
            String methodName, Object args, Object payload) {       
        this.messageType = messageType;
        this.to = to;
        this.from = from;
        this.methodName = methodName;
        this.args = args;
        this.toURL = new MessageURL(to);
        this.fromURL = new MessageURL(from);
        this.payload = payload;
    }

	@Override
	public String toString() {
		return "Message |||messageType=" + messageType + ", to=" + to + ", from="
				+ from + ", methodName=" + methodName + ", args=" + args + "|||";
	}

	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Object getArgs() {
		return args;
	}
	public void setArgs(Object args) {
		this.args = args;
	}
	public Type getMessageType() {
		return messageType;
	}
	public void setMessageType(Type messageType) {
		this.messageType = messageType;
	}
	
	public Object getReturnObject() {
	        return returnObject;
	}

	public void setReturnObject(Object returnObject) {
	        this.returnObject = returnObject;
	}


    public MessageURL getFromURL() {
        return fromURL;
    }

    public void setFromURL(MessageURL fromURL) {
        this.fromURL = fromURL;
    }

    public MessageURL getToURL() {
        return toURL;
    }

    public void setToURL(MessageURL toURL) {
        this.toURL = toURL;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }


}
