package org.jamp.amp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageURL {

    String scheme;        
    String host;
    String serviceURI;
    String port;
    String serviceName;
    //stomp                                      scheme 1           host 2          port 3, 4
    static Pattern urlPattern = Pattern.compile("^([a-z][a-z+]*)://([a-zA-Z0-9_.]*)(:)?([0-9]*)/?([//a-zA-Z0-9_]*)");
    //Pattern.compile("^stomp://([a-zA-Z0-9_]*):([0-9]*)//(*)");

    
    public MessageURL() {
    }
    
    public String toString() {
        return String.format("%s://%s%s%s/%s", scheme, host,  port==null || port.trim().equals("") ?  "" : ":", port, serviceURI);
    }

    public String connectionString() {
        return String.format("%s://%s%s%s", scheme, host,  port==null || port.trim().equals("") ?  "" : ":", port);
    }

    
    public MessageURL(String url) {
        Matcher matcher = urlPattern.matcher(url);
        if (matcher.matches()) {
            scheme = matcher.group(1);        
            host = matcher.group(2);
            port = matcher.group(4);
            serviceURI = matcher.group(5);
            String[] strings = serviceURI.split("/");
            this.serviceName = strings[strings.length-1];

        } else {
            throw new IllegalStateException("Not valid URL " + url);
        }
        
    }
    public String getScheme() {
        return scheme;
    }
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getServiceURI() {
        return serviceURI;
    }
    public void setServiceURI(String serviceURI) {
        this.serviceURI = serviceURI;
    }    
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }
    
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


}
