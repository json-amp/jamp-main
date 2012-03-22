package org.jamp.amp;


import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jamp.JampMessage;
import org.jamp.Decoder;
import org.jamp.SkeletonServiceInvoker;
import org.jamp.example.model.AddressBook;
import org.jamp.example.model.Employee;
import org.jamp.example.model.EmployeeServiceImpl;
import org.jamp.impl.JampFactoryImpl;
import org.jamp.impl.JampMessageDecoder;
import org.jamp.impl.JampMessageEncoder;
import org.junit.Test;



public class ServiceInvokerTest {
    
    Decoder <JampMessage, CharSequence> messageDecoder = new JampMessageDecoder();


    @Test
    public void invokerTest() throws Exception {
        Object methodEncodedAsMessage = getMethodEncodedAsMessage();
        System.out.println(methodEncodedAsMessage);
        SkeletonServiceInvoker serviceInvoker = JampFactoryImpl.factory().createJampServerSkeleton(EmployeeServiceImpl.class);
        
        assertNotNull(methodEncodedAsMessage);
        JampMessage decode = messageDecoder.decode((String)methodEncodedAsMessage);
        assertNotNull(decode);
        serviceInvoker.invokeMessage(decode);
        
    }

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
        
        JampMessage message = new JampMessage("send");
        
        message.setAction("addEmployee");
        message.setArgs(args);
        message.setTo("stomp://foo/foo/foo");
        message.setFrom("stomp://foo/foo/foo");
        
        JampMessageEncoder encoder = new JampMessageEncoder();
        
        Object encodedObject = encoder.encodeObject(message);
        return encodedObject;
    }
}
