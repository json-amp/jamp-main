package org.jamp;


public interface JampMessageDecoder extends  Decoder<JampMessage, CharSequence>{

    @Override
    JampMessage decode(CharSequence payload) throws Exception;

}