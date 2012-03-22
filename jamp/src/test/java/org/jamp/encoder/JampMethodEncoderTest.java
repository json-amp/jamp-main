package org.jamp.encoder;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;

import org.jamp.JampMessage;
import org.jamp.example.model.AddressBook;
import org.jamp.example.model.Employee;
import org.jamp.impl.JampMessageEncoderImpl;
import org.junit.Test;



public class JampMethodEncoderTest {
    
    @SuppressWarnings("nls")
    @Test
    public void simpleMethodEncoderTest() throws Exception {
        JampMessageEncoderImpl encoder = new JampMessageEncoderImpl();
        
        
        List<AddressBook> books = new ArrayList<AddressBook>();
        books.add(new AddressBook("a"));
        books.add(new AddressBook("b"));

        Employee emp = new Employee("rick", "510-555-1212", books);
        
        List <Object> args = new ArrayList<Object>();

        
        JampMessage message = new JampMessage("send");
        message.setAction("addEmployee");
        message.setArgs(args);
        
        args.add(emp);
        args.add(1);
        args.add(1.0f);
        args.add(2);
        args.add("hello dolly");
        
        message.setTo("stomp://foo/EmployeeService");
        message.setFrom("stomp://foo/EmployeeServiceClient/Browser123");
        
        Object encodedObject = encoder.encode(message);
        
        assertEquals("[\"send\",\"stomp://foo/EmployeeService\",\"stomp://foo/EmployeeServiceClient/Browser123\",\"addEmployee\",[{,\"name\":\"rick\",\"books\":[{,\"foo\":\"a\"},{,\"foo\":\"b\"}],\"old\":false,\"phoneNumber\":\"510-555-1212\",\"books2\":null,\"books3\":null},1,1.0,2,\"hello dolly\"]]", encodedObject);
    }

}
