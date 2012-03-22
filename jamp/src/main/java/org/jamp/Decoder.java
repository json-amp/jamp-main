package org.jamp;

import java.io.Reader;

/** Decode object from on-wire stream format to Java object. */
public interface Decoder <TO, STREAM_OR_BUFFER>{
    
    TO decode(STREAM_OR_BUFFER buffer) throws Exception; 
}
