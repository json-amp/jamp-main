package org.jamp.amp;

import static org.junit.Assert.*;

import org.junit.Test;


public class MessageURLTest {

    @Test
    public void test() {
                
        assertEquals("stomp://www.example.com:90/", (new MessageURL("stomp://www.example.com:90").toString()));
        assertEquals("stomp://www.example.com:90/", (new MessageURL("stomp://www.example.com:90/").toString()));
        assertEquals("http://www.example.com/empService", (new MessageURL("http://www.example.com/empService").toString()));
        assertEquals("http://www.example.com/async/empService", (new MessageURL("http://www.example.com/async/empService").toString()));

        assertEquals("empService", (new MessageURL("http://www.example.com/empService").getServiceName()));
        assertEquals("empService", (new MessageURL("http://www.example.com/async/empService").getServiceName()));

        
    }
}
