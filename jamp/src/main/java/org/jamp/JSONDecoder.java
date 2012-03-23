package org.jamp;

public interface JSONDecoder<T> extends Decoder<T, CharSequence> {

    @Override
    T decode(CharSequence cs) throws Exception;

}