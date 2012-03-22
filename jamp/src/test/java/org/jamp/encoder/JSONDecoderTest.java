package org.jamp.encoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.jamp.Decoder;
import org.jamp.impl.JSONDecoderImpl;
import org.junit.Test;

@SuppressWarnings({"rawtypes", "unchecked", "nls"})
public class JSONDecoderTest {

    @Test
    public void map() throws Exception {
        
        Decoder decoder = new JSONDecoderImpl();
        Map<String, Object> map = (Map<String, Object>) decoder
                .decode("{ \"name\":\"rick\"}");
        assertEquals("rick", map.get("name"));
    }

    @Test
    public void simpleDecoderTest() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        Map<String, Object> map = (Map<String, Object>) decoder
                .decode("     { \"name\" \t\n : 5 }");
        System.out.println(map);
    }

    @Test
    public void simpleDecoderBasicTypes() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        Map<String, Object> map = (Map<String, Object>) decoder
                .decode("{" + "\"name\":\"rick\"," + "\"old\":false,"
                        + "\"age\":99," + "\"weight\":122.2,"
                        + "\"phoneNumber\":\t\n\"5205551212\","
                        + "\"mylist\":[1,2,3,4]} ");
        System.out.println(map);

        Double weight = (Double) map.get("weight");
        assertTrue("weight is 122.2", weight.doubleValue() == 122.2);

        Integer age = (Integer) map.get("age");
        assertTrue("age is 99", age.intValue() == 99);

        Boolean old = (Boolean) map.get("old");
        assertTrue("old is false", old.booleanValue() == false);

        String name = (String) map.get("name");

        assertEquals("name is Rick Hightower", name, "rick");

        List mylist = (List) map.get("mylist");

        assertEquals("list index 0 is 1", 1, mylist.get(0));

    }

    @Test
    public void simpleNestedObject() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        Map<String, Object> map = (Map<String, Object>) decoder
                .decode("{ "
                        + "\"obj\": {\"age\":5, \"obj2\":{\"foo\":7}},"
                        + "\"stop\":true" + "}");
        System.out.println(map);
    }

    @Test
    public void simpleJSONArray() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        List<Object> list = (List<Object>) decoder
                .decode("[0,1, 2, 3, 4, 5, 0]");
        System.out.println(list);
    }

    @Test
    public void jsonArrayWithObject() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        List<Object> list = (List<Object>) decoder
                .decode("[0,1, 2, 3, 4, 5, 0, {\"foo\":7}]");
        System.out.println(list);
    }

    @Test
    public void simpleString() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        String s = (String) decoder.decode("\"Love\"");
        assertEquals("Love", s);
    }

    @Test
    public void simpleNumber() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        Integer i = (Integer) decoder.decode("5");
        assertEquals((Integer) 5, i);
    }

    @Test
    public void simpleWhiteSpace() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        Integer i = (Integer) decoder.decode("\t\n\r\r\n\r\r5");
        assertEquals((Integer) 5, i);
    }

    @Test
    public void jsonObjectWithArrays() throws Exception {
        Decoder decoder = new JSONDecoderImpl();
        decoder.decode("{\"l1\"\t:\t[0,1,2,3], \"l2\"\n:\n[0,1,2,3], \"l3\":[0,1,2,3], \"l4\"\r\n:\r\n[0,1,2,3]}");
    }

}
