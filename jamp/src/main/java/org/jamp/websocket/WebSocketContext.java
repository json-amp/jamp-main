package org.jamp.websocket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public interface WebSocketContext {
    BufferedOutputStream startBinaryMessage() throws IOException;
    PrintWriter startTextMessage() throws IOException;
    void sendText(String text) throws IOException;
    void sendBinary(byte [] buffer) throws IOException;
    void close();
}
