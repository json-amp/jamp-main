package org.jamp.encoder;

import static org.junit.Assert.*;

import org.jamp.Encoder;
import org.jamp.impl.JSONDecoderImpl;
import org.jamp.impl.JSONEncoderImpl;
import org.junit.Test;

@SuppressWarnings({"rawtypes", "unchecked", "nls"})
public class JSONEncoderTest {

    @Test
    public void stringUnicodeEncoderTest() throws Exception {
        String str = "ßæçîñking bad~\u007f\u0080~"; //Range checking
        Encoder encoder = new JSONEncoderImpl();
        assertEquals("\"\\u00DF\\u00E6\\u00E7\\u00EE\\u00F1king bad~\u007f\\u0080~\"", encoder.encode(str));
        
    }
    
    @Test
    public void stringOtherEncoderTest() throws Exception {
        String str = "\\/\b\f\r\n\t";
        Encoder encoder = new JSONEncoderImpl();

        
        JSONDecoderImpl decoder = new JSONDecoderImpl();
        System.out.println(decoder.decode(encoder.encode(str)));

        assertEquals("\"\\\\\\/\\b\\f\\r\\n\\t\"", encoder.encode(str));
        
    }
    
    @Test
    public void stringUnicodeRoundtripEncoderDecoderTest() throws Exception {
        String str = "ßæçîñking bad~\u007f\u0080~"; //Range checking 007f end of ascii and 0080 start of unicode
        Encoder encoder = new JSONEncoderImpl();
        //not single\ of \u007f versus double \\ of \\u0080 subtle but it is a range check
        assertEquals("\"\\u00DF\\u00E6\\u00E7\\u00EE\\u00F1king bad~\u007f\\u0080~\"", encoder.encode(str));
        
        JSONDecoderImpl decoder = new JSONDecoderImpl();
        assertEquals(str, decoder.decode(encoder.encode(str)));
    }
    
    @Test
    public void stringOtherRoundtripEncoderDecoderTest() throws Exception {
        String str = "\\/\b\f\r\n\t";
        Encoder encoder = new JSONEncoderImpl();
        JSONDecoderImpl decoder = new JSONDecoderImpl();

        assertEquals("\"\\\\\\/\\b\\f\\r\\n\\t\"", encoder.encode(str));

        assertEquals(str, decoder.decode(encoder.encode(str)));

    }

}
