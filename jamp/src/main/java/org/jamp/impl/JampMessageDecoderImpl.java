package org.jamp.impl;

import java.util.List;
import java.util.Map;

import org.jamp.Factory;
import org.jamp.JSONDecoder;
import org.jamp.JampMessage;
import org.jamp.JampMessageDecoder;

/** Encodes an JAMP input object (now just string) into a Message. */
public class JampMessageDecoderImpl implements JampMessageDecoder {
    enum ParseMode {
        messageType, to, from, methodName, done
    };

    JSONDecoder<List<Object>> jsonDecoder = Factory.factory()
            .createJSONListDecoder();

    @Override
    @SuppressWarnings("unchecked")
    public JampMessage decode(CharSequence payload) throws Exception {
        try {

            List<Object> list = jsonDecoder.decode(payload);
            return listToMessage(payload, list);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    Messages.getString("JampMessageDecoderImpl.0"), ex); //$NON-NLS-1$
        }

    }

    @SuppressWarnings({ "nls", "unchecked" })
    private JampMessage listToMessage(CharSequence payload, List<Object> list) {
        String type = (String) list.get(0);
        String to = null;
        String from = null;
        String action = null;
        List<Object> args = null;
        String queryId = null;
        Object replyObject = null;
        Map<String, Object> errorObject = null;

        if (type.equals("send")) { //$NON-NLS-1$
            to = (String) list.get(1);
            from = (String) list.get(2);
            action = (String) list.get(3);
            args = (List<Object>) list.get(4);
        } else if (type.equals("query")) { //$NON-NLS-1$
            queryId = (String) list.get(1);
            to = (String) list.get(2);
            from = (String) list.get(3);
            action = (String) list.get(4);
            args = (List<Object>) list.get(5);
        } else if (type.equals("reply")) { //$NON-NLS-1$
            queryId = (String) list.get(1);
            to = (String) list.get(2);
            from = (String) list.get(3);
            replyObject = list.get(4);
        } else if (type.equals("error-query")) { //$NON-NLS-1$
            queryId = (String) list.get(1);
            to = (String) list.get(2);
            from = (String) list.get(3);
            errorObject = (Map<String, Object>) list.get(4);
        } else if (type.equals("error")) { //$NON-NLS-1$
            to = (String) list.get(1);
            from = (String) list.get(2);
            errorObject = (Map<String, Object>) list.get(3);
        }
        
        if (to==null) {
            if (action!=null && action.contains(".")) {
                String[] split = action.split("\\.");
                String objectName = split[0];
                action = split[1];
                to = String.format("soa://service/%s", objectName);
            }

        }

        JampMessage message = org.jamp.Factory.factory().createJampMessage();
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
