package org.jamp.amp.encoder;

import java.io.BufferedReader;

import org.jamp.amp.Message;




/** Encodes an JAMP input object (bufferedreader) into a Message. */
public class JampMessageReaderDecoder implements Decoder <Message, BufferedReader> {
	
    static String readPayload(BufferedReader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        
        
        try {
            String line = null;
            
            while ((line=reader.readLine())!=null) {
                builder.append(line);
            }
        }finally{
            if (reader!=null)reader.close();
        }
        
        return builder.toString();
    }


    public Message decodeObject(BufferedReader reader) throws Exception {
        JampMessageDecoder decoder = new JampMessageDecoder();

        String str = readPayload(reader);
        
        return decoder.decodeObject(str);
    }
	
}
