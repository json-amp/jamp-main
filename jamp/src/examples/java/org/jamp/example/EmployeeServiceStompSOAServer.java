package org.jamp.example;

import java.io.IOException;

import org.jamp.amp.AmpFactory;
import org.jamp.example.model.EmployeeServiceImpl;


public class EmployeeServiceStompSOAServer {
    public static void main (String [] args) throws IOException {
        AmpFactory.factory().createMQReciever("stomp://localhost:6666/foo", 
                "rick", "rick", "queue/empService", EmployeeServiceImpl.class, null);
    }
}
