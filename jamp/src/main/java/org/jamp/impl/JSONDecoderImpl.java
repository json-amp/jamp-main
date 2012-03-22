package org.jamp.impl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jamp.JSONDecoder;

/**
 * Converts an input JSON String into Java objects works with Reader or String
 * as input. Produces an Object which can be any of the basic JSON types mapped
 * to Java.
 * */
public class JSONDecoderImpl<T> implements JSONDecoder<T> {

    CharSequence str;
    char[] charArray;
    int __index;
    int line;
    int lastLineStart;
    char ch;
    final boolean debug = false; // just used to debug if their are
                                         // problems

    JSONStringDecoder jsStringDecoder = new JSONStringDecoder();

    private final boolean safe() {

        return __index < charArray.length;
    }

    private final boolean hasMore() throws Exception {
        return __index + 1 <= charArray.length;
    }

    private final char currentChar() throws Exception {

        if (safe()) {
            return ch = charArray[__index];
        }
            return ch;
        
    }

    private final char nextChar() throws Exception {
        if (hasMore()) {
            __index++;
            return this.currentChar();
        } 
            return ch;
        
    }

    void reset() {
        str = null;
        charArray = null;
        __index = 0;
        line = 0;
        lastLineStart = 0;
        ch = (char) 0;

    }

    @SuppressWarnings({ "nls", "unchecked" })
    @Override
    public T decode(CharSequence cs) throws Exception {
        str = cs;
        charArray = cs.toString().toCharArray();
        Object root = decodeValue();
        return (T) root;
    }

    @SuppressWarnings("nls")
    public void debug() {
        if (debug) {
            PrintStream out = System.out;
            out.println(Messages.getString("JSONDecoderImpl.0") + line); //$NON-NLS-1$

            int lineCount = __index - lastLineStart;

            out.println(Messages.getString("JSONDecoderImpl.1") //$NON-NLS-1$
                    + str.subSequence(lastLineStart, __index));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lineCount; i++) {
                if (lineCount - 1 == i) {
                    builder.append('^');
                } else {
                    builder.append('.');
                }
            }
            System.out.println(builder.toString());
        }
    }

    public void skipWhiteSpace() throws Exception {

        // if (fine) System.out.println("skipWhiteSpace");

        int tooMuchWhiteSpace = 20;
        int count = 0;

        while (hasMore()) {
            count++;
            if (count > tooMuchWhiteSpace)
                break;
            char c = this.currentChar();
            // if (fine) System.out.printf("skipWhiteSpace c='%s' \n", c);
            if (c == '\n') {
                line++;
                lastLineStart = __index;
                this.nextChar();
                continue;
            } else if (c == '\r') {
                line++;
                if (hasMore()) {
                    c = this.nextChar();
                    if (c != '\n') {
                        lastLineStart = __index;
                        break;
                    }
                }
                lastLineStart = __index;
                this.nextChar();
                continue;
            } else if (Character.isWhitespace(c)) {
                this.nextChar();
                continue;
            } else {
                break;
            }
        }

    }

    public Object decodeJsonObject() throws Exception {
        if (debug)
            System.out.println("decodeJsonObject enter"); //$NON-NLS-1$

        if (this.currentChar() == '{' && this.hasMore())
            this.nextChar();

        Map<String, Object> map = new HashMap<String, Object>();
        do {

            skipWhiteSpace();

            char c = this.currentChar();

            if (c == '"') {
                String key = decodeKeyName();
                skipWhiteSpace();
                c = this.currentChar();
                if (c != ':') {
                    debug();
                    throw new IllegalStateException(
                            Messages.getString("JSONDecoderImpl.2") + c //$NON-NLS-1$
                                    + Messages.getString("JSONDecoderImpl.3") + line //$NON-NLS-1$
                                    + Messages.getString("JSONDecoderImpl.4") + __index); //$NON-NLS-1$
                }
                c = this.nextChar(); // skip past ':'
                skipWhiteSpace();
                Object value = decodeValue();

                if (debug)
                    System.out.printf(Messages.getString("JSONDecoderImpl.5"), key, value); //$NON-NLS-1$
                skipWhiteSpace();

                map.put(key, value);

                c = this.currentChar();
                if (!(c == '}' || c == ',')) {
                    debug();
                    throw new IllegalStateException(
                            Messages.getString("JSONDecoderImpl.6") + c //$NON-NLS-1$
                                    + Messages.getString("JSONDecoderImpl.7") + line //$NON-NLS-1$
                                    + Messages.getString("JSONDecoderImpl.8") + __index); //$NON-NLS-1$
                }
            }
            if (c == '}') {
                this.nextChar();
                break;
            } else if (c == ',') {
                this.nextChar();
                continue;
            } else {
                debug();
                throw new IllegalStateException(
                        Messages.getString("JSONDecoderImpl.9") + c //$NON-NLS-1$
                                + Messages.getString("JSONDecoderImpl.10") + line + Messages.getString("JSONDecoderImpl.11") //$NON-NLS-1$ //$NON-NLS-2$
                                + __index);
            }
        } while (this.hasMore());
        return map;
    }

    private Object decodeValue() throws Exception {
        Object value = null;

        while (hasMore()) {
            char c = this.currentChar();
            if (c == '"') {
                value = decodeString();
                break;
            } else if (c == 't' || c == 'f') {
                value = decodeBoolean();
                break;
            } else if (c == 'n') {
                value = decodeNull();
                break;
            } else if (c == '[') {
                value = decodeJsonArray();
                break;
            } else if (c == '{') {
                value = decodeJsonObject();
                break;
            } else if (c == '-' || Character.isDigit(c)) {
                value = decodeNumber();
                break;
            }
            this.nextChar();
        }
        skipWhiteSpace();
        return value;
    }

    private Object decodeNumber() throws Exception {
        StringBuilder builder = new StringBuilder();

        boolean doubleFloat = false;
        do {
            char c = this.currentChar();
            if (Character.isWhitespace(c) || c == ',' || c == '}' || c == ']') {
                break;
            }
            if (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E'
                    || c == '+' || c == '-') {
                if (c == '.' || c == 'e' || c == 'E') {
                    doubleFloat = true;
                }
                builder.append(c);
                this.nextChar();
                continue;
            } 
                debug();
                throw new IllegalStateException(Messages.getString("JSONDecoderImpl.12") //$NON-NLS-1$
                        + line + Messages.getString("JSONDecoderImpl.13") + __index //$NON-NLS-1$
                        + Messages.getString("JSONDecoderImpl.14") + c); //$NON-NLS-1$ 
            
        } while (this.hasMore());

        String svalue = builder.toString();
        Object value = null;
        try {
            if (doubleFloat) {
                value = Double.parseDouble(svalue);
            } else {
                value = Integer.parseInt(svalue);
            }
        } catch (Exception ex) {
            debug();
            throw new IllegalStateException(
                    Messages.getString("JSONDecoderImpl.15") + line //$NON-NLS-1$
                            + Messages.getString("JSONDecoderImpl.16") + __index + Messages.getString("JSONDecoderImpl.17") //$NON-NLS-1$ //$NON-NLS-2$
                            + svalue);

        }

        return value;

    }

    private int index() {
        return __index;
    }

    private Object decodeBoolean() throws Exception {
        StringBuilder builder = new StringBuilder();
        do {
            char c = this.currentChar();
            if (Character.isWhitespace(c) || c == ',' || c == '}') {
                break;
            }
            builder.append(c);
            this.nextChar();
        } while (hasMore());
        return Boolean.parseBoolean(builder.toString());
    }

    private Object decodeNull() throws Exception {
        StringBuilder builder = new StringBuilder();
        do {
            char c = this.currentChar();
            if (Character.isWhitespace(c) || c == ',' || c == '}') {
                break;
            }
            builder.append(c);
            this.nextChar();
        } while (hasMore());
        return null;
    }

    private Object decodeString() throws Exception {
        CharSequence value = null;

        int startIndex = index() + 1; // Increment past the starting quote
        do {
            char c = this.nextChar();
            if (c == '"') {
                break;
            }
        } while (hasMore());

        value = str.subSequence(startIndex, index());

        value = encodeString(value);
        this.nextChar(); // skip other quote

        return value;
    }

    private String encodeString(CharSequence string) throws Exception {
        return jsStringDecoder.decode(string(string));
    }

    private String string(CharSequence string) {
        if (string instanceof String) {
            return (String) string;
        }
            return string.toString();
        
    }

    private String decodeKeyName() throws Exception {

        StringBuilder builder = new StringBuilder();
        do {
            char c = this.nextChar();
            if (c == '"') {
                break;
            }
            builder.append(c);
        } while (hasMore());

        Object value = builder.toString();
        this.nextChar(); // skip other quote

        return (String) value;
    }

    public Object decodeJsonArray() throws Exception {
        if (this.currentChar() == '[' && hasMore())
            this.nextChar();
        skipWhiteSpace();
        List<Object> list = new ArrayList<Object>();

        int arrayIndex = 0;

        do {
            skipWhiteSpace();
            char c = this.currentChar();
            list.add(decodeValue());
            arrayIndex++;
            skipWhiteSpace();
            c = this.currentChar();
            if (!(c == ',' || c == ']')) {
                debug();
                throw new IllegalStateException(
                        Messages.getString("JSONDecoderImpl.18") + c //$NON-NLS-1$
                                + Messages.getString("JSONDecoderImpl.19") + line + Messages.getString("JSONDecoderImpl.20") //$NON-NLS-1$ //$NON-NLS-2$
                                + index());
            }
            if (c == ']') {
                this.nextChar();
                break;
            }
        } while (this.hasMore());
        return list;
    }

}
