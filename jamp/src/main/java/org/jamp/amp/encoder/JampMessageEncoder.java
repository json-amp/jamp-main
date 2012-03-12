package org.jamp.amp.encoder;


import org.jamp.amp.AmpMessage;


/** Convert method into JAMP method. */
public class JampMessageEncoder implements Encoder<String, AmpMessage>{
	
    /* If we support Jackson we have to make this pluggable. */
	Encoder<String, Object> encoder = new JSONEncoder();



    public String encodeObject(AmpMessage message) throws Exception {
       StringBuilder builder = new StringBuilder(255);
        
        if (message.getMessageType()==AmpMessage.Type.SEND) {
        
            builder.append(String.format("[\"send\",\"%s\",\"%s\",\"%s\"",message.getTo(), message.getFrom(), message.getAction()));
        
        } else if (message.getMessageType()==AmpMessage.Type.SEND){
            builder.append(String.format("[\"query\",\"%s\",\"%s\",\"%s\",\"%s\"", message.getQueryId(), message.getTo(), message.getFrom(), message.getAction()));            
        } else if (message.getMessageType()==AmpMessage.Type.ERROR) {
            JSONEncoder encoder = new JSONEncoder();
            builder.append(String.format("[\"error\",\"%s\",\"%s\", %s]", message.getTo(), message.getFrom(), encoder.encodeObject(message.getErrorObject())));            
        }  else if (message.getMessageType()==AmpMessage.Type.ERROR_QUERY){
            builder.append(String.format("[\"error_query\",\"%s\",\"%s\",\"%s\", %s]", message.getQueryId(), message.getTo(), message.getFrom(), encoder.encodeObject(message.getErrorObject())));            
        } else if (message.getMessageType()==AmpMessage.Type.REPLY){
            builder.append(String.format("[\"reply\",\"%s\",\"%s\",\"%s\", %s]", message.getQueryId(), message.getTo(), message.getFrom(), encoder.encodeObject(message.getReplyObject())));            
        }
        
        Object[] args = message.getArgs().toArray(new Object[message.getArgs().size()]);
        
        if (args.length!=0) {
            builder.append(",[");
        }
        
        for (int index=0; index < args.length; index++) {
            Object param = args[index];
            builder.append((String)encoder.encodeObject(param));
            if (index+1!=args.length) {
                builder.append(',');
            } else {
                builder.append(']');
            }
        }
        builder.append(']');
        return builder.toString();
   }

}
