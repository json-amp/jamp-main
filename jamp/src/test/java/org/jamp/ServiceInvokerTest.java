package org.jamp;


import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jamp.JampMessage;
import org.jamp.SkeletonServiceInvoker;
import org.jamp.example.model.AddressBook;
import org.jamp.example.model.Employee;
import org.jamp.example.model.EmployeeServiceImpl;
import org.jamp.impl.JampMessageEncoderImpl;
import org.junit.Test;



public class ServiceInvokerTest {
    
    JampMessageDecoder  messageDecoder = org.jamp.Factory.factory().createJampMessageDecoder();
    JampMessageEncoder  messageEncoder = org.jamp.Factory.factory().createJampMessageEncoder();

    @Test
    public void invokerTest() throws Exception {
        Object methodEncodedAsMessage = getMethodEncodedAsMessage();
        System.out.println(methodEncodedAsMessage);
        SkeletonServiceInvoker serviceInvoker = org.jamp.Factory.factory().createJampServerSkeletonFromClass(EmployeeServiceImpl.class);
        
        assertNotNull(methodEncodedAsMessage);
        JampMessage decode = messageDecoder.decode((String)methodEncodedAsMessage);
        assertNotNull(decode);
        serviceInvoker.invokeMessage(decode);
        
    }

    @SuppressWarnings("nls")
    private Object getMethodEncodedAsMessage() throws NoSuchMethodException,
            Exception {
        
        List <Object> args = new ArrayList<Object>();
        
        List<AddressBook> books = new ArrayList<AddressBook>();
        books.add(new AddressBook("a"));
        books.add(new AddressBook("b"));

        
        Set<AddressBook> books2 = new HashSet<AddressBook>();
        books2.add(new AddressBook("c"));
        books2.add(new AddressBook("d"));

        List<AddressBook> books3 = new ArrayList<AddressBook>();
        books3.add(new AddressBook("e"));
        books3.add(new AddressBook("f"));

        Employee emp = new Employee("rick", "510-555-1212", books, books2, books3.toArray(new AddressBook[books3.size()]));
        emp.setOld(true);
        
        
        args.add(emp);
        args.add(1);
        args.add(1.0f);
        args.add(2);
        args.add("hello dolly");
        
        JampMessage message = org.jamp.Factory.factory().createJampMessage();
        message.setMessageTypeFromString("send");
        
        message.setAction("addEmployee");
        message.setArgs(args);
        message.setTo("stomp://foo/foo/foo");
        message.setFrom("stomp://foo/foo/foo");
        
        messageEncoder  = new JampMessageEncoderImpl();
        
        Object encodedObject = messageEncoder.encode(message);
        return encodedObject;
    }
}
