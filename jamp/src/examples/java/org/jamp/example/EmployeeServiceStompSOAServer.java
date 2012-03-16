package org.jamp.example;

import java.io.IOException;

import org.jamp.example.model.EmployeeServiceImpl;
import org.jamp.impl.JampFactoryImpl;


public class EmployeeServiceStompSOAServer {
    public static void main (String [] args) throws IOException {
        JampFactoryImpl.factory().createMQReciever("stomp://localhost:6666/foo", 
                "rick", "rick", "queue/empService", EmployeeServiceImpl.class, null);
    }
}
