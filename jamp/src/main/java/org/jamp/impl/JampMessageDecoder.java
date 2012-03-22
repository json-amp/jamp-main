package org.jamp.impl;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.jamp.JampMessage;
import org.jamp.Decoder;

/** Encodes an JAMP input object (now just string) into a Message. */
public class JampMessageDecoder implements Decoder<JampMessage, CharSequence>{
	enum ParseMode{messageType, to, from, methodName, done};
	
	Decoder<List, CharSequence> jsonDecoder = new JSONDecoder();
	

    @SuppressWarnings("unchecked")
    public JampMessage decode(CharSequence payload) throws Exception {
        try {

            List<Object> list = jsonDecoder.decode(payload);
            return listToMessage(payload, list);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to process JAMP message, format error", ex);
        }
        
    }
    


    private JampMessage listToMessage(CharSequence payload, List<Object> list) {
        String type = (String) list.get(0);
        String to = null;
        String from = null;
        String action =  null;
        List <Object> args = null;
        String queryId = null;
        Object replyObject = null;
        Map<String, Object> errorObject = null;
        
        if (type.equals("send")) {
            to = (String) list.get(1);
            from = (String) list.get(2);
            action = (String) list.get(3);
            args = (List<Object>) list.get(4);
        } else if (type.equals("query")) {
            queryId = (String) list.get(1);
            to = (String) list.get(2);
            from = (String) list.get(3);
            action = (String) list.get(4);
            args = (List<Object>) list.get(5);
        } else if (type.equals("reply")) {
            queryId = (String) list.get(1);
            to = (String) list.get(2);
            from = (String) list.get(3);
            replyObject = list.get(4);
        } else if (type.equals("error-query")) {
            queryId = (String) list.get(1);
            to = (String) list.get(2);
            from = (String) list.get(3);
            errorObject = (Map<String, Object>) list.get(4);
        } else if (type.equals("error")) {
            to = (String) list.get(1);
            from = (String) list.get(2);
            errorObject = (Map<String, Object>) list.get(3);
        }
        
        JampMessage message = new JampMessage(type);
        message.setTo(to);
        message.setFrom(from);
        message.setAction(action);
        message.setArgs(args);
        message.setQueryId(queryId);
        message.setReplyObject(replyObject);
        message.setErrorObject(errorObject);
        message.setPayload(payload);
        return message;
    }


	
}
