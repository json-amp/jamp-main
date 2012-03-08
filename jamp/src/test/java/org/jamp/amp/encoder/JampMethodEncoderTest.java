package org.jamp.amp.encoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jamp.amp.encoder.JampMethodEncoder;
import org.jamp.example.model.AddressBook;
import org.jamp.example.model.Employee;
import org.jamp.example.model.EmployeeService;
import org.junit.Test;



public class JampMethodEncoderTest {
    
    @Test
    public void simpleMethodEncoderTest() throws Exception {
        JampMethodEncoder encoder = new JampMethodEncoder();
        Method method = EmployeeService.class.getDeclaredMethod("addEmployee", Employee.class, int.class, float.class, Integer.class, String.class);
        assertNotNull(method);
        
        
        List<AddressBook> books = new ArrayList<AddressBook>();
        books.add(new AddressBook("a"));
        books.add(new AddressBook("b"));

        Employee emp = new Employee("rick", "510-555-1212", books);
        
        Object encodedObject = encoder.encodeMethodForSend(method, new Object[]{emp, 1, 1.0f, 2, "hello dolly"}, "to@me", "from@someoneelse");

        assertNotNull(encodedObject);
        
        System.out.println(encodedObject);
        
        assertEquals("[\"send\",\"to@me\",\"from@someoneelse\",\"addEmployee\",[{,\"name\":\"rick\",\"books\":[{,\"foo\":\"a\"},{,\"foo\":\"b\"}],\"old\":false,\"phoneNumber\":\"510-555-1212\",\"books2\":null,\"books3\":null},1,1.0,2,\"hello dolly\"]]", encodedObject);
    }

}
