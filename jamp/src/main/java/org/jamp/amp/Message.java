package org.jamp.amp;


/** Represents an AMP message. */
public class Message {
    
    
    public enum Type {SEND, QUERY, REPLY, ERROR_QUERY, ERROR};
    Type messageType;
	String to;
	String from;
	String methodName;
	Object args;

	public Message(String messageType, String to, String from,
			String methodName, Object args) {	    
	    this.messageType = Enum.valueOf(Type.class, messageType.toUpperCase().replace('-', '_'));
		this.to = to;
		this.from = from;
		this.methodName = methodName;
		this.args = args;
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

}
