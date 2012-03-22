package org.jamp;


public interface JampMessageEncoder extends Encoder<String, JampMessage> {

    @Override
    String encode(JampMessage message) throws Exception;

}