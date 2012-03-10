package org.jamp.amp;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jamp.amp.AmpFactory;
import org.jamp.amp.SkeletonServiceInvoker;
import org.jamp.amp.encoder.Decoder;
import org.jamp.amp.encoder.JampMessageDecoder;
import org.jamp.amp.encoder.JampMethodEncoder;
import org.jamp.example.model.AddressBook;
import org.jamp.example.model.Employee;
import org.jamp.example.model.EmployeeService;
import org.jamp.example.model.EmployeeServiceImpl;
import org.junit.Test;



public class ServiceInvokerTest {
    
    Decoder <AmpMessage, String> messageDecoder = new JampMessageDecoder();


    @Test
    public void invokerTest() throws Exception {
        Object methodEncodedAsMessage = getMethodEncodedAsMessage();
        System.out.println(methodEncodedAsMessage);
        SkeletonServiceInvoker serviceInvoker = AmpFactory.factory().createJampServerSkeleton(EmployeeServiceImpl.class);
        serviceInvoker.invokeMessage(messageDecoder.decodeObject((String)methodEncodedAsMessage));
        
    }

    private Object getMethodEncodedAsMessage() throws NoSuchMethodException,
            Exception {
        JampMethodEncoder encoder = new JampMethodEncoder();
        Method method = EmployeeService.class.getDeclaredMethod("addEmployee", Employee.class, int.class, float.class, Integer.class, String.class);
        assertNotNull(method);
        
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
        
        Object encodedObject = encoder.encodeMethodForSend(method, new Object[]{emp, 1, 1.0f, 2, "hello dolly"}, "ws://foobar/employeeService", "browser://foobar/employeeService");
        return encodedObject;
    }
}
