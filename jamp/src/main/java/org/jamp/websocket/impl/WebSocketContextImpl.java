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

    WebSocketInternal connection;

    WebSocketContextImpl(WebSocketInternal ws) {
        this.connection = ws;
    }

    @Override
    public BufferedOutputStream startBinaryMessage() throws IOException {
        return new BufferedOutputStream(null) {
            @Override
            public void close() {
                // No op
            }

            @Override
            public void write(byte[] b) throws IOException {
                try {
                    connection.send(b);
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }

            @Override
            public synchronized void write(byte[] b, int off, int len)
                    throws IOException {
                for (int index = off; index < len; index++) {
                    write(b[index]);
                }
            }

            @Override
            public synchronized void write(int b) throws IOException {
                this.write(new byte[] { (byte) b });
            }
        };
    }

    @Override
    public PrintWriter startTextMessage() throws IOException {
        return new PrintWriter(new Writer() {

            @Override
            public void close() throws IOException {
                // No op
            }

            @Override
            public void flush() throws IOException {
                connection.flush();

            }

            @Override
            public void write(String text) throws IOException {
                try {
                    connection.send(text);
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {

                StringBuilder builder = new StringBuilder(cbuf.length);
                for (int index = off; index < len; index++) {
                    builder.append(cbuf[index]);

                }
                write(builder.toString());
            }

        });
    }

    @Override
    public void sendText(String text) throws IOException {
        try {
            connection.send(text);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void sendBinary(byte[] buffer) throws IOException {
        try {
            connection.send(buffer);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() {
        connection.clientClose();
    }

}
