package org.jamp;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jamp.impl.Messages;

public class JampMessageURL {

    String scheme;        
    String host;
    String serviceURI;
    String port;
    String serviceName;
    //                                           scheme 1           host 2          port 3, 4
    static Pattern urlPattern = Pattern.compile("^([a-z][a-z+]*)://([a-zA-Z0-9_.]*)(:)?([0-9]*)/?([//a-zA-Z0-9_]*)"); //$NON-NLS-1$

    
    public JampMessageURL() {
    }
    
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return String.format("%s://%s%s%s/%s", scheme, host,  port==null || port.trim().equals("") ?  "" : ":", port, serviceURI); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @SuppressWarnings("nls")
    public String connectionString() {
        return String.format("%s://%s%s%s", scheme, host,  port==null || port.trim().equals("") ?  "" : ":", port); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    
    public JampMessageURL(String url) {
        Matcher matcher = urlPattern.matcher(url);
        if (matcher.matches()) {
            scheme = matcher.group(1);        
            host = matcher.group(2);
            port = matcher.group(4);
            serviceURI = matcher.group(5);
            String[] strings = serviceURI.split("/"); //$NON-NLS-1$
            this.serviceName = strings[strings.length-1];

        } else {
            throw new IllegalStateException(MessageFormat.format(Messages.getString("JampMessageURL.9"), url)); //$NON-NLS-1$
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
