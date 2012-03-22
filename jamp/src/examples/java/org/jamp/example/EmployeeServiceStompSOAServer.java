package org.jamp.example;

import java.io.IOException;

import org.jamp.example.model.EmployeeServiceImpl;


public class EmployeeServiceStompSOAServer {
    @SuppressWarnings("nls")
    public static void main (String [] args) throws IOException {
        org.jamp.Factory.factory().createMQReciever("stomp://localhost:6666/foo", 
                "rick", "rick", "queue/empService", EmployeeServiceImpl.class, null, null, null);
    }
}
