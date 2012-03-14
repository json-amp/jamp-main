package org.jamp.websocket;

import java.io.IOException;

public interface SimpleWebSocketListener extends BaseWebSocketListener{
    
  void onStart(WebSocketContext context)
    throws IOException;

  void onTextMessage(WebSocketContext context, String text)
    throws IOException;

  void onBinaryMessage(WebSocketContext context, byte [] buffer)
    throws IOException;

  void onClose(WebSocketContext context)
    throws IOException;

  void onDisconnect(WebSocketContext context)
    throws IOException;


}
