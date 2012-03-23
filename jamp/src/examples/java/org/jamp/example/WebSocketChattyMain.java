package org.jamp.example;

import java.io.IOException;
import java.io.PrintWriter;

import org.jamp.websocket.SimpleWebSocketListener;
import org.jamp.websocket.WebSocketConnection;
import org.jamp.websocket.WebSocketConnectionFactory;
import org.jamp.websocket.WebSocketContext;

public class WebSocketChattyMain {

    @SuppressWarnings("nls")
    public static void main(String args[]) throws Exception {
        WebSocketConnection connection = WebSocketConnectionFactory.create();

        connection.connectWithProtocol("ws://localhost:8887/",
                new SimpleWebSocketListener() {

                    @Override
                    public void onTextMessage(WebSocketContext context,
                            String text) throws IOException {

                        System.out.println("onTextMessage " + text);

                    }

                    @Override
                    public void onStart(WebSocketContext context)
                            throws IOException {
                        System.out.println("onStart ");
                        PrintWriter printWriter = context.startTextMessage();
                        printWriter.printf("Hello how are you?\n");
                        context.sendText("Good... thanks for asking!!!!!!!!!!!!!!!!!!!");
                    }

                    @Override
                    public void onDisconnect(WebSocketContext context)
                            throws IOException {
                        System.out.println("onDisconnect ");
                    }

                    @Override
                    public void onClose(WebSocketContext context)
                            throws IOException {
                        System.out.println("onClose ");

                    }

                    @Override
                    public void onBinaryMessage(WebSocketContext context,
                            byte[] buffer) throws IOException {
                        System.out.println("onBinaryMessage ");

                    }
                }, "chat1.2", "chat1.1");

    }
}
