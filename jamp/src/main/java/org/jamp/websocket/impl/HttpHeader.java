package org.jamp.websocket.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class HttpHeader {

    enum Type {
        CLIENT, SERVER
    };

    Type type;
    byte[] content;
    String resourcedescriptor;
    LinkedHashMap<String, String> headers;
    private String httpstatusmessage;

    HttpHeader(Type aType) {
        this.type = aType;
        headers = new LinkedHashMap<String, String>();
    }

    public static HttpHeader createClientRequest() {
        return new HttpHeader(Type.CLIENT);
    }

    public static HttpHeader createServerRequest() {
        return new HttpHeader(Type.SERVER);
    }

    public String getHttpStatusMessage() {
        return httpstatusmessage;
    }

    public void setHttpStatusMessage(String message) {
        this.httpstatusmessage = message;
    }

    public Iterator<String> getHeaderNames() {
        return Collections.unmodifiableSet(headers.keySet()).iterator();// Safety
                                                                        // first
    }

    public String getHeader(String name) {
        String value = headers.get(name);
        return value == null ? "" : value;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void putHeader(String name, String value) {
        headers.put(name, value);
    }

    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    public void setResourceDescriptor(String resourcedescriptor)
            throws IllegalArgumentException {
        this.resourcedescriptor = resourcedescriptor;
    }

    public String getResourceDescriptor() {
        return resourcedescriptor;
    }

    public boolean isClient() {
        return type == Type.CLIENT;
    }

    public boolean isServer() {
        return type == Type.SERVER;
    }

}
