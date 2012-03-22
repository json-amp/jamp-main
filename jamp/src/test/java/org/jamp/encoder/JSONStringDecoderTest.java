package org.jamp.encoder;

import static org.junit.Assert.assertTrue;

import org.jamp.impl.JSONStringDecoder;
import org.junit.Test;


@SuppressWarnings("nls")
public class JSONStringDecoderTest {
	
	@Test
	public void test() throws Exception{
		JSONStringDecoder decode = new JSONStringDecoder();
		
        String decodedString = decode.decode("\\\"Hello how are you?\\\"\\nGood and you?\\u20ac\\/\\\\");
		String testString = "\"Hello how are you?\"\nGood and you?Û/\\";
		assertTrue(decodedString.equals(testString));
	}

}
