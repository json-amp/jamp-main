package org.jamp.websocket.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Rick Hightower
 *
 */
public class Frame {

    public enum Opcode {
        CONTINIOUS, TEXT, BINARY, PING, PONG, CLOSING
        // more to come
    }

    protected static final byte[] emptyarray = {};
    protected boolean finished;
    protected Opcode optcode;
    private ByteBuffer payLoadBuffer;
    protected boolean transferMask;

    public Frame() {
    }

    public Frame(Opcode op) {
        this.optcode = op;
        payLoadBuffer = ByteBuffer.wrap(emptyarray);
    }

    public Frame(Frame f) {
        finished = f.isFinished();
        optcode = f.getOpcode();
        payLoadBuffer = ByteBuffer.wrap(f.getPayloadData());
        transferMask = f.isTransferMask();
    }

    public boolean isFinished() {
        return finished;
    }

    public Opcode getOpcode() {
        return optcode;
    }

    public boolean isTransferMask() {
        return transferMask;
    }

    public byte[] getPayloadData() {
        if (!head) {
            return payLoadBuffer.array();
        } else {
            int size = 0;
            List <byte[]> buffers = new ArrayList<byte[]>();
            if (payLoadBuffer!=null) {
                byte[] buffer = payLoadBuffer.array();
                size += buffer.length;
                buffers.add(buffer);
            }
            for (Frame frame : payloadFrameSeries) {
                byte[] buffer = frame.payLoadBuffer.array();
                size += buffer.length;
                buffers.add(buffer);
            }
            
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            
            for (byte[] buf : buffers) {
                byteBuffer.put(buf);
            }
            
            return byteBuffer.array();

        }
    }

    public void setFinished(boolean fin) {
        this.finished = fin;
    }

    public void setOptcode(Opcode optcode) {
        this.optcode = optcode;
    }

    public void setPayload(byte[] payload) throws WebSocketException  {
        payLoadBuffer = ByteBuffer.wrap(payload);
    }

    public void setTransferMask(boolean transferMask) {
        this.transferMask = transferMask;
    }

    List <Frame> payloadFrameSeries;
    
    public void append(Frame nextFrameInSeries)  {
          payloadFrameSeries.add(nextFrameInSeries);
          finished = nextFrameInSeries.finished;
    }

    @Override
    public String toString() {
        return "Framedata{ optcode:"
                + getOpcode()
                + ", fin:"
                + isFinished()
                + ", payloadlength:"
                + payLoadBuffer.limit()
                + ", payload:"
                + Charsetfunctions
                        .utf8Bytes(new String(payLoadBuffer.array())) + "}";
    }

    boolean head;
    public void setSeriesHead(boolean b) {
        head = true;
        payloadFrameSeries = new ArrayList<Frame>();
    }

}
