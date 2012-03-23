package org.jamp;

public interface JSONEncoder extends Encoder<String, Object> {

    @Override
    String encode(Object obj) throws Exception;

}