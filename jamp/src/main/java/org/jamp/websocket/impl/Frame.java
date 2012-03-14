package org.jamp.websocket.impl;

import java.nio.ByteBuffer;


public class Frame {

    public enum Opcode {
        CONTINIOUS, TEXT, BINARY, PING, PONG, CLOSING
        // more to come
    }

    protected static byte[] emptyarray = {};
    protected boolean fin;
    protected Opcode optcode;
    private ByteBuffer unmaskedpayload;
    protected boolean transferemasked;

    public Frame() {
    }

    public Frame(Opcode op) {
        this.optcode = op;
        unmaskedpayload = ByteBuffer.wrap(emptyarray);
    }

    public Frame(Frame f) {
        fin = f.isFin();
        optcode = f.getOpcode();
        unmaskedpayload = ByteBuffer.wrap(f.getPayloadData());
        transferemasked = f.getTransfereMasked();
    }

    public boolean isFin() {
        return fin;
    }

    public Opcode getOpcode() {
        return optcode;
    }

    public boolean getTransfereMasked() {
        return transferemasked;
    }

    public byte[] getPayloadData() {
        return unmaskedpayload.array();
    }

    public void setFin(boolean fin) {
        this.fin = fin;
    }

    public void setOptcode(Opcode optcode) {
        this.optcode = optcode;
    }

    public void setPayload(byte[] payload) throws InvalidDataException {
        unmaskedpayload = ByteBuffer.wrap(payload);
    }

    public void setTransferemasked(boolean transferemasked) {
        this.transferemasked = transferemasked;
    }

    public void append(Frame nextframe) throws InvalidFrameException {
        if (unmaskedpayload == null) {
            unmaskedpayload = ByteBuffer.wrap(nextframe.getPayloadData());
        } else {
            // TODO might be inefficient. Cosider a global buffer pool
            ByteBuffer tmp = ByteBuffer
                    .allocate(nextframe.getPayloadData().length
                            + unmaskedpayload.capacity());
            tmp.put(unmaskedpayload.array());
            tmp.put(nextframe.getPayloadData());
            unmaskedpayload = tmp;
        }
        fin = nextframe.isFin();
    }

    @Override
    public String toString() {
        return "Framedata{ optcode:"
                + getOpcode()
                + ", fin:"
                + isFin()
                + ", payloadlength:"
                + unmaskedpayload.limit()
                + ", payload:"
                + Charsetfunctions
                        .utf8Bytes(new String(unmaskedpayload.array())) + "}";
    }

}
