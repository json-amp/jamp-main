package org.jamp.impl;

import java.io.BufferedReader;

import org.jamp.JampMessage;
import org.jamp.Decoder;




/** Encodes an JAMP input object (bufferedreader) into a Message. */
public class JampMessageReaderDecoder implements Decoder <JampMessage, BufferedReader> {
	
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


    public JampMessage decodeObject(BufferedReader reader) throws Exception {
        JampMessageDecoder decoder = new JampMessageDecoder();

        String str = readPayload(reader);
        
        return decoder.decodeObject(str);
    }
	
}
