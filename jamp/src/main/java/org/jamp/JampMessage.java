package org.jamp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JampMessage {

    public enum Type {
        SEND, QUERY, REPLY, ERROR_QUERY, ERROR
    };

    Type messageType;
    String to;
    String from;
    String action;
    List<Object> args;
    Object replyObject;
    JampMessageURL toURL;
    JampMessageURL fromURL;
    Object payload;
    String queryId;
    Map<String, Object> errorObject;

    JampMessage() {

    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "Message |||messageType=" + messageType + ", to=" + to
                + ", from=" + from + ", methodName=" + action + ", args="
                + args + "|||";
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        if (to != null)
            this.toURL = new JampMessageURL(to);
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        if (from != null)
            this.fromURL = new JampMessageURL(from);
        this.from = from;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String methodName) {
        this.action = methodName;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    public void setArgs(Object[] args) {
        this.args = Arrays.asList(args);
    }

    public Type getMessageType() {
        return messageType;
    }

    public void setMessageType(Type messageType) {
        this.messageType = messageType;
    }

    public void setMessageTypeFromString(String sMessageType) {
        this.messageType = Enum.valueOf(Type.class, sMessageType.toUpperCase()
                .replace('-', '_'));
    }

    public Object getReplyObject() {
        return replyObject;
    }

    public void setReplyObject(Object returnObject) {
        this.replyObject = returnObject;
    }

    public JampMessageURL getFromURL() {
        return fromURL;
    }

    public void setFromURL(JampMessageURL fromURL) {
        this.fromURL = fromURL;
    }

    public JampMessageURL getToURL() {
        return toURL;
    }

    public void setToURL(JampMessageURL toURL) {
        this.toURL = toURL;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public Map<String, Object> getErrorObject() {
        return errorObject;
    }

    public void setErrorObject(Map<String, Object> errorObject) {
        this.errorObject = errorObject;
    }

}
