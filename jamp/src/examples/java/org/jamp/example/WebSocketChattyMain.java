package org.jamp.example;

import java.io.IOException;
import java.io.PrintWriter;

import org.jamp.websocket.SimpleWebSocketListener;
import org.jamp.websocket.WebSocketConnection;
import org.jamp.websocket.WebSocketConnectionFactory;
import org.jamp.websocket.WebSocketContext;

public class WebSocketChattyMain {

    public static void main (String args[]) throws Exception {
        WebSocketConnection connection = WebSocketConnectionFactory.create();
        
        connection.connect("ws://localhost:8887/", new SimpleWebSocketListener() {
            
            public void onTextMessage(WebSocketContext context, String text)
                    throws IOException {
                
                System.out.println("onTextMessage " + text);
                
            }
            
            public void onStart(WebSocketContext context) throws IOException {
                System.out.println("onStart ");
                PrintWriter printWriter = context.startTextMessage();
                printWriter.printf("Hello how are you?\n");
                context.sendText("Good... thanks for asking!!!!!!!!!!!!!!!!!!!");
            }
            
            public void onDisconnect(WebSocketContext context) throws IOException {
                System.out.println("onDisconnect ");
           }
            
            public void onClose(WebSocketContext context) throws IOException {
                System.out.println("onClose ");
                               
            }
            
            public void onBinaryMessage(WebSocketContext context, byte[] buffer)
                    throws IOException {
                System.out.println("onBinaryMessage ");
                               
            }
        });
        
    }
}
