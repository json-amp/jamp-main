package org.jamp;

import static org.junit.Assert.*;

import org.jamp.JampMessageURL;
import org.junit.Test;


public class MessageURLTest {

    @SuppressWarnings("nls")
    @Test
    public void test() {
                
        assertEquals("stomp://www.example.com:90/", (new JampMessageURL("stomp://www.example.com:90").toString()));
        assertEquals("stomp://www.example.com:90/", (new JampMessageURL("stomp://www.example.com:90/").toString()));
        assertEquals("http://www.example.com/empService", (new JampMessageURL("http://www.example.com/empService").toString()));
        assertEquals("http://www.example.com/async/empService", (new JampMessageURL("http://www.example.com/async/empService").toString()));

        assertEquals("empService", (new JampMessageURL("http://www.example.com/empService").getServiceName()));
        assertEquals("empService", (new JampMessageURL("http://www.example.com/async/empService").getServiceName()));

        
    }
}
