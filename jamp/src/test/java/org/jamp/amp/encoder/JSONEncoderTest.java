package org.jamp.amp.encoder;

import static org.junit.Assert.*;

import org.jamp.Encoder;
import org.jamp.impl.JSONDecoder;
import org.jamp.impl.JSONEncoder;
import org.junit.Test;


@SuppressWarnings({"rawtypes", "unchecked"})
public class JSONEncoderTest {

    @Test
    public void stringUnicodeEncoderTest() throws Exception {
        String str = "ßæçîñking bad~\u007f\u0080~"; //Range checking
        Encoder encoder = new JSONEncoder();
        assertEquals("\"\\u00DF\\u00E6\\u00E7\\u00EE\\u00F1king bad~\u007f\\u0080~\"", encoder.encodeObject(str));
        
    }
    
    @Test
    public void stringOtherEncoderTest() throws Exception {
        String str = "\\/\b\f\r\n\t";
        Encoder encoder = new JSONEncoder();

        
        JSONDecoder decoder = new JSONDecoder();
        System.out.println(decoder.decode(encoder.encodeObject(str)));

        assertEquals("\"\\\\\\/\\b\\f\\r\\n\\t\"", encoder.encodeObject(str));
        
    }
    
    @Test
    public void stringUnicodeRoundtripEncoderDecoderTest() throws Exception {
        String str = "ßæçîñking bad~\u007f\u0080~"; //Range checking 007f end of ascii and 0080 start of unicode
        Encoder encoder = new JSONEncoder();
        //not single\ of \u007f versus double \\ of \\u0080 subtle but it is a range check
        assertEquals("\"\\u00DF\\u00E6\\u00E7\\u00EE\\u00F1king bad~\u007f\\u0080~\"", encoder.encodeObject(str));
        
        JSONDecoder decoder = new JSONDecoder();
        assertEquals(str, decoder.decode(encoder.encodeObject(str)));
    }
    
    @Test
    public void stringOtherRoundtripEncoderDecoderTest() throws Exception {
        String str = "\\/\b\f\r\n\t";
        Encoder encoder = new JSONEncoder();
        JSONDecoder decoder = new JSONDecoder();

        assertEquals("\"\\\\\\/\\b\\f\\r\\n\\t\"", encoder.encodeObject(str));

        assertEquals(str, decoder.decode(encoder.encodeObject(str)));

    }

}
