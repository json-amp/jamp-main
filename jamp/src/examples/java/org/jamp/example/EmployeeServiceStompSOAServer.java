package org.jamp.example;

import java.io.IOException;

import org.jamp.amp.StompMessageReceiver;
import org.jamp.example.model.EmployeeServiceImpl;


public class EmployeeServiceStompSOAServer {
    public static void main (String [] args) throws IOException {
        new StompMessageReceiver("stomp://localhost:6666/foo", 
                "rick", "rick", "queue/empService", EmployeeServiceImpl.class, null);
    }
}
