package org.jamp;


public interface Decoder <TO, STREAM_OR_BUFFER>{
    
    TO decode(STREAM_OR_BUFFER buffer) throws Exception; 
}
