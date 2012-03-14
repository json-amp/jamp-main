package org.jamp.websocket.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.jamp.websocket.WebSocketContext;

/**
 * 
 * @author Rick Hightower
 *
 */
public class WebSocketContextImpl implements WebSocketContext {
    
    LowLevelWebSocketConnectionInternal connection;
    

    WebSocketContextImpl (LowLevelWebSocketConnectionInternal ws) {
        this.connection = ws;
    }
    public BufferedOutputStream startBinaryMessage() throws IOException {
        return new BufferedOutputStream(null) {
            public void   close() {
                // No op
            }
            public void write(byte[] b) throws IOException {
                try {
                    connection.send(b);
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
            public void write(byte[] b, int off, int len) throws IOException  {
               for (int index = off; index < len; index++) {
                   write(b[index]);
               }
            }
            public void write(int b)  throws IOException {
                this.write(new byte[]{(byte)b});
            }
        };
    }

    public PrintWriter startTextMessage() throws IOException {
        return new PrintWriter(new Writer(){

            @Override
            public void close() throws IOException {
                //No op  
            }

            @Override
            public void flush() throws IOException {
                connection.flush();
                
            }
            
            public void write(String text) throws IOException {
                try {
                    connection.send(text);
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
            
            @Override
            public void write(char[] cbuf, int off, int len)
                    throws IOException {
                
                StringBuilder builder = new StringBuilder(cbuf.length);
                for (int index = off; index < len; index++) {
                    builder.append(cbuf[index]);
                    
                }
                write(builder.toString());
            }
            
        });
    }

    public void sendText(String text) throws IOException {
        try {
            connection.send(text);
        } catch (Exception ex) {
            throw new IOException(ex);
        }        
    }

    public void sendBinary(byte[] buffer) throws IOException {
        try {
            connection.send(buffer);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }


    public void close() {
        connection.clientClose();
    }


}
