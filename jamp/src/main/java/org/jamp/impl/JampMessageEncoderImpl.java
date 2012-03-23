package org.jamp.impl;

import org.jamp.JampMessage;
import org.jamp.Encoder;
import org.jamp.JampMessageEncoder;

/** Convert method into JAMP method. */
public class JampMessageEncoderImpl implements JampMessageEncoder {

    Encoder<String, Object> encoder = new JSONEncoderImpl();

    @SuppressWarnings("nls")
    @Override
    public String encode(JampMessage message) throws Exception {
        StringBuilder builder = new StringBuilder(255);

        if (message.getMessageType() == JampMessage.Type.SEND) {

            builder.append(String.format("[\"send\",\"%s\",\"%s\",\"%s\"",
                    message.getTo(), message.getFrom(), message.getAction()));

        } else if (message.getMessageType() == JampMessage.Type.SEND) {
            builder.append(String.format(
                    "[\"query\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    message.getQueryId(), message.getTo(), message.getFrom(),
                    message.getAction()));
        } else if (message.getMessageType() == JampMessage.Type.ERROR) {
            builder.append(String.format("[\"error\",\"%s\",\"%s\", %s]",
                    message.getTo(), message.getFrom(),
                    encoder.encode(message.getErrorObject())));
        } else if (message.getMessageType() == JampMessage.Type.ERROR_QUERY) {
            builder.append(String.format(
                    "[\"error_query\",\"%s\",\"%s\",\"%s\", %s]",
                    message.getQueryId(), message.getTo(), message.getFrom(),
                    encoder.encode(message.getErrorObject())));
        } else if (message.getMessageType() == JampMessage.Type.REPLY) {
            builder.append(String.format(
                    "[\"reply\",\"%s\",\"%s\",\"%s\", %s]",
                    message.getQueryId(), message.getTo(), message.getFrom(),
                    encoder.encode(message.getReplyObject())));
        }

        Object[] args = message.getArgs().toArray(
                new Object[message.getArgs().size()]);

        if (args.length != 0) {
            builder.append(",[");
        }

        for (int index = 0; index < args.length; index++) {
            Object param = args[index];
            builder.append(encoder.encode(param));
            if (index + 1 != args.length) {
                builder.append(',');
            } else {
                builder.append(']');
            }
        }
        builder.append(']');
        return builder.toString();
    }

}
