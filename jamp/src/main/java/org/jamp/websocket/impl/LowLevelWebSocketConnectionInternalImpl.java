package org.jamp.websocket.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jamp.websocket.impl.Frame.Opcode;

/**
 * Represents one end (client or server) of a single WebSocket connection. Takes
 * care of the "handshake" phase, then allows for easy sending of text frames,
 * and receiving frames through an event-based model.
 * 
 * This is an inner class, used by <tt>WebSocketClient</tt> and
 * <tt>WebSocketServer</tt>, and should never need to be instantiated directly
 * by your code. However, instances are exposed in <tt>WebSocketServer</tt>
 * through the <i>onClientOpen</i>, <i>onClientClose</i>, <i>onClientMessage</i>
 * callbacks.
 * 
 * @author Nathan Rajlich
 * @author rick (did some major refactors, basically forked org.java_websocket
 */
public final class LowLevelWebSocketConnectionInternalImpl implements LowLevelWebSocketConnectionInternal {

    public static final int READY_STATE_CONNECTING = 0;
    public static final int READY_STATE_OPEN = 1;
    public static final int READY_STATE_CLOSING = 2;
    public static final int READY_STATE_CLOSED = 3;
    /**
     * The default port of WebSockets, as defined in the spec. If the nullary
     * constructor is used, DEFAULT_PORT will be the port the WebSocketServer is
     * binded to. Note that ports under 1024 usually require root permissions.
     */
    public static final int DEFAULT_PORT = 80;

    public enum Role {
        CLIENT, SERVER
    }

    private static/* final */boolean DEBUG = true; // must be final in the
                                                    // future in order to take
                                                    // advantage of VM
                                                    // optimization

    private URI clientURI = null;
    int port;

    /**
     * Determines whether to receive data as part of the handshake, or as part
     * of text/data frame transmitted over the websocket.
     */
    private boolean handshakeComplete = false;
    /**
     * Determines whether we sent already a request to Close the connection or
     * not.
     */
    private boolean closeHandshakeSent = false;
    /**
     * Determines whether the connection is open or not
     */
    private boolean connectionClosed = false;

    /**
     * The listener to notify of WebSocket events.
     */
    private LowLevelListener wsl;
    /**
     * Buffer where data is read to from the socket
     */
    private ByteBuffer socketBuffer;
    /**
     * Queue of buffers that need to be sent to the client.
     */
    private BlockingQueue<ByteBuffer> bufferQueue;
    /**
     * The amount of bytes still in queue to be sent, at every given time. It's
     * updated at every send/sent operation.
     */
    private Long bufferQueueTotalAmount = (long) 0;

    private boolean handshakeSetup = false;

    // private Role role;

    private Frame currentframe;

    private HttpHeader handshakerequest = null;

    private SocketChannel sockchannel;

    /**
     * The 'Selector' used to get event keys from the underlying socket.
     */
    private Selector selector = null;

    private final Lock clientCloseLock = new ReentrantLock();

    public static LowLevelWebSocketConnectionInternalImpl createClientWebSocket(
            LowLevelListener listener, URI uri, int port)
            throws IOException {

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(uri.getHost(), port));
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        return new LowLevelWebSocketConnectionInternalImpl(listener, socketChannel, true,
                selector, uri, port);
    }

    private LowLevelWebSocketConnectionInternalImpl(LowLevelListener listener,
            SocketChannel socketchannel, boolean b, Selector selector, URI url,
            int port) {
        init(listener, socketchannel);
        this.handshakeSetup = true;
        this.selector = selector;
        this.clientURI = url;
        this.port = port;
    }

    public static LowLevelWebSocketConnectionInternal createServerWebSocket(
            LowLevelListener listener, SocketChannel socketchannel) {
        return new LowLevelWebSocketConnectionInternalImpl(listener, socketchannel);
    }

    private LowLevelWebSocketConnectionInternalImpl(LowLevelListener listener,
            SocketChannel socketchannel) {
        init(listener, socketchannel);
        this.role = Role.SERVER;
    }

    private void init(LowLevelListener listener,
            SocketChannel socketchannel) {
        this.sockchannel = socketchannel;
        this.bufferQueue = new LinkedBlockingQueue<ByteBuffer>(10);
        this.socketBuffer = ByteBuffer.allocate(65558);
        socketBuffer.flip();
        this.wsl = listener;
        this.role = Role.CLIENT;
        this.handshakeSetup = false;

    }

    boolean handleServerRead() throws IOException, InvalidHandshakeException {
        HandshakeState handshakestate = null;

        if (handshakeSetup == false) {
            try {
                this.setParseMode(role);
                socketBuffer.reset();
                HttpHeader tmphandshake = this.translateHandshake(socketBuffer);
                if (!tmphandshake.isClient()) {
                    closeConnection(CloseFrame.PROTOCOL_ERROR,
                            "wrong http function", false);
                    return true;
                }
                HttpHeader handshake = tmphandshake;
                handshakestate = this.acceptHandshakeAsServer(handshake);
                if (handshakestate == HandshakeState.MATCHED) {
                    HttpHeader response;
                    try {
                        response = wsl.onConnectionIsServer(
                                this, handshake);
                    } catch (InvalidDataException e) {
                        closeConnection(e.getCloseCode(), e.getMessage(), false);
                        return true;
                    }
                    writeDirect(this.createHandshake(this
                            .postProcessHandshakeResponseAsServer(handshake,
                                    response), role));

                    handshakeSetup = true;
                    open(handshake);
                    handleRead();
                    return true;
                } else if (handshakestate == HandshakeState.MATCHING) {
                    if (this != null) {
                        throw new InvalidHandshakeException(
                                "multible drafts matching");
                    }
                    handshakeSetup = true;

                }
            } catch (InvalidHandshakeException e) {
                // go on with an other this
            } catch (IncompleteHandshakeException e) {
                if (socketBuffer.limit() == socketBuffer.capacity()) {
                    close(CloseFrame.TOOBIG, "handshake is to big");
                }
                // read more bytes for the handshake
                socketBuffer.position(socketBuffer.limit());
                socketBuffer.limit(socketBuffer.capacity());
                return true;
            }

            if (handshakeSetup == false) {
                close(CloseFrame.PROTOCOL_ERROR, "no this matches");
            }
            return true;
        }
        return true;

    }

    boolean handleClientRead() throws IOException, InvalidHandshakeException {
        HandshakeState handshakestate = null;

        this.setParseMode(role);
        HttpHeader tmphandshake = this.translateHandshake(socketBuffer);
        if (!tmphandshake.isServer()) {
            closeConnection(CloseFrame.PROTOCOL_ERROR, "Wwrong http function",
                    false);
            return true;
        }
        HttpHeader handshake = tmphandshake;
        handshakestate = this.acceptHandshakeAsClient(handshakerequest,
                handshake);
        if (handshakestate == HandshakeState.MATCHED) {
            open(handshake);
            handleRead();
        } else if (handshakestate == HandshakeState.MATCHING) {
            return true;
        } else {
            close(CloseFrame.PROTOCOL_ERROR, "this " + this
                    + " refuses handshake");
        }

        return false;

    }

    /**
     * Should be called when a Selector has a key that is writable for this
     * WebSocket's SocketChannel connection.
     * 
     * @throws IOException
     *             When socket related I/O errors occur.
     * @throws InterruptedException
     */
    /* package public */void handleRead() throws IOException {
        if (!socketBuffer.hasRemaining()) {
            socketBuffer.rewind();
            socketBuffer.limit(socketBuffer.capacity());
            if (sockchannel.read(socketBuffer) == -1) {
                close(CloseFrame.ABNROMAL_CLOSE);
            }

            socketBuffer.flip();
        }

        if (socketBuffer.hasRemaining()) {
            if (DEBUG)
                System.out
                        .println("process("
                                + socketBuffer.remaining()
                                + "): {"
                                + (socketBuffer.remaining() > 1000 ? "too big to display"
                                        : new String(socketBuffer.array(),
                                                socketBuffer.position(),
                                                socketBuffer.remaining()))
                                + "}");
            if (!handshakeComplete) {
                socketBuffer.mark();
                try {
                    if (role == Role.SERVER) {
                        if (this.handleServerRead()) {
                            return;
                        }
                    } else if (role == Role.CLIENT) {
                        if (this.handleClientRead()) {
                            return;
                        }
                    }
                } catch (InvalidHandshakeException e) {
                    close(e);
                }
            } else {
                // Receiving frames

                try {
                    List<Frame> frames = this.translateFrame(socketBuffer);
                    for (Frame f : frames) {
                        if (DEBUG)
                            System.out.println("matched frame: " + f);
                        Opcode curop = f.getOpcode();
                        if (curop == Opcode.CLOSING) {
                            int code = CloseFrame.NOCODE;
                            String reason = "";
                            if (f instanceof CloseFrame) {
                                CloseFrame cf = (CloseFrame) f;
                                code = cf.getCloseCode();
                                reason = cf.getMessage();
                            }
                            if (closeHandshakeSent) {
                                // complete the close handshake by disconnecting
                                closeConnection(code, reason, true);
                            } else {
                                // echo close handshake
                                close(code, reason);
                                closeConnection(code, reason, false);
                            }
                            continue;
                        } else if (curop == Opcode.PING) {
                            wsl.onPing(this, f);
                            continue;
                        } else if (curop == Opcode.PONG) {
                            wsl.onPong(this, f);
                            continue;
                        } else {
                            // process non control frames
                            if (currentframe == null) {
                                if (f.getOpcode() == Opcode.CONTINIOUS) {
                                    throw new InvalidFrameException(
                                            "unexpected continious frame");
                                } else if (f.isFin()) {
                                    // receive normal onframe message
                                    deliverMessage(f);
                                } else {
                                    // remember the frame whose payload is about
                                    // to be continued
                                    currentframe = f;
                                }
                            } else if (f.getOpcode() == Opcode.CONTINIOUS) {
                                currentframe.append(f);
                                if (f.isFin()) {
                                    deliverMessage(currentframe);
                                    currentframe = null;
                                }
                            } else {
                                throw new InvalidDataException(
                                        CloseFrame.PROTOCOL_ERROR,
                                        "non control or continious frame expected");
                            }
                        }
                    }
                } catch (InvalidDataException e1) {
                    wsl.errorHandler(this, e1);
                    close(e1);
                    return;
                }
            }
        }
    }

    public void close(int code, String message) {
        if (DEBUG) System.out.printf("CLOSE CALLED %s, %s\n\n", code, message);
        
        try {
            closeDirect(code, message);
        } catch (IOException e) {
            closeConnection(CloseFrame.ABNROMAL_CLOSE, true);
        }
    }

    public void closeDirect(int code, String message) throws IOException {
        if (!closeHandshakeSent) {
            if (handshakeComplete) {
                if (code == CloseFrame.ABNROMAL_CLOSE) {
                    closeConnection(code, true);
                    closeHandshakeSent = true;
                    return;
                }
                flush();
                if (this.hasCloseHandshake()) {
                    try {
                        sendFrameDirect(new CloseFrame(code, message));
                    } catch (InvalidDataException e) {
                        wsl.errorHandler(this, e);
                        closeConnection(CloseFrame.ABNROMAL_CLOSE,
                                "generated frame is invalid", false);
                    }
                } else {
                    closeConnection(code, false);
                }
            } else {
                closeConnection(CloseFrame.NEVERCONNECTED, false);
            }
            if (code == CloseFrame.PROTOCOL_ERROR)// this endpoint found a
                                                  // PROTOCOL_ERROR
                closeConnection(code, false);
            closeHandshakeSent = true;
            return;
        }
    }

    /**
     * closes the socket no matter if the closing handshake completed. Does not
     * send any not yet written data before closing. Calling this method more
     * than once will have no effect.
     * 
     * @param remote
     *            Indicates who "generated" <code>code</code>.<br>
     *            <code>true</code> means that this endpoint received the
     *            <code>code</code> from the other endpoint.<br>
     *            false means this endpoint decided to send the given code,<br>
     *            <code>remote</code> may also be true if this endpoint started
     *            the closing handshake since the other endpoint may not simply
     *            echo the <code>code</code> but close the connection the same
     *            time this endpoint does do but with an other <code>code</code>
     *            . <br>
     **/
    public void closeConnection(int code, String message, boolean remote) {
        if (connectionClosed) {
            return;
        }
        connectionClosed = true;
        try {
            sockchannel.close();
        } catch (IOException e) {
            wsl.errorHandler(this, e);
        }

        if (this.thread != null) {
            thread.interrupt();
        }

        this.wsl.onWebsocketClose(this, code, message, remote);
        if (this != null)
            this.reset();
        currentframe = null;
        handshakerequest = null;
    }

    public void closeConnection(int code, boolean remote) {
        closeConnection(code, "", remote);
    }

    public void close(int code) {
        close(code, "");
    }

    public void close(InvalidDataException e) {
        close(e.getCloseCode(), e.getMessage());
    }

    public void send(String text) throws IllegalArgumentException,
            NotYetConnectedException, InterruptedException {
        if (text == null)
            throw new IllegalArgumentException(
                    "Cannot send 'null' data to a WebSocket.");
        send(this.createFrames(text, role == Role.CLIENT));
    }

    public void send(byte[] bytes) throws IllegalArgumentException,
            NotYetConnectedException, InterruptedException {
        if (bytes == null)
            throw new IllegalArgumentException(
                    "Cannot send 'null' data to a WebSocket.");
        send(this.createFrames(bytes, role == Role.CLIENT));
    }

    private void send(Collection<Frame> frames) throws InterruptedException {
        if (!this.handshakeComplete)
            throw new NotYetConnectedException();
        for (Frame f : frames) {
            sendFrame(f);
        }
    }

    public void sendFrame(Frame framedata) throws InterruptedException {
        if (DEBUG)
            System.out.println("send frame: " + framedata);
        channelWrite(this.createBinaryFrame(framedata));
    }

    private void sendFrameDirect(Frame framedata) throws IOException {
        if (DEBUG)
            System.out.println("send frame: " + framedata);
        channelWriteDirect(this.createBinaryFrame(framedata));
    }

    boolean hasBufferedData() {
        return !this.bufferQueue.isEmpty();
    }

    /**
     * The amount of data in Queue, ready to be sent.
     * 
     * @return Amount of Data still in Queue and not sent yet of the socket
     */
    long bufferedDataAmount() {
        return bufferQueueTotalAmount;
    }

    /**
     * Empty the internal buffer, sending all the pending data before
     * continuing.
     */
    public void flush() throws IOException {
        ByteBuffer buffer = this.bufferQueue.peek();
        while (buffer != null) {
            sockchannel.write(buffer);
            if (buffer.remaining() > 0) {
                continue;
            } else {
                synchronized (bufferQueueTotalAmount) {
                    // subtract this amount of data from the total queued
                    // (synchronized over this object)
                    bufferQueueTotalAmount -= buffer.limit();
                }
                this.bufferQueue.poll(); // Buffer finished. Remove it.
                buffer = this.bufferQueue.peek();
            }
        }
    }

    public void startHandshake(HttpHeader handshakedata)
            throws InvalidHandshakeException, InterruptedException {
        if (handshakeComplete)
            throw new IllegalStateException("Handshake has already been sent.");

        // Store the Handshake Request we are about to send
        this.handshakerequest = this
                .postProcessHandshakeRequestAsClient(handshakedata);

        // Send
        channelWrite(this.createHandshake(this.handshakerequest, role));
    }

    private void channelWrite(ByteBuffer buf) throws InterruptedException {
        if (DEBUG)
            System.out.println("write("
                    + buf.limit()
                    + "): {"
                    + (buf.limit() > 1000 ? "too big to display" : new String(
                            buf.array())) + "}");
        buf.rewind(); // TODO rewinding should not be nessesary
        synchronized (bufferQueueTotalAmount) {
            // add up the number of bytes to the total queued (synchronized over
            // this object)
            bufferQueueTotalAmount += buf.limit();
        }
        if (!bufferQueue.offer(buf)) {
            try {
                flush();
            } catch (IOException e) {
                wsl.errorHandler(this, e);
                closeConnection(CloseFrame.ABNROMAL_CLOSE, true);
                return;
            }
            bufferQueue.put(buf);
        }

        if (selector != null)
            selector.wakeup(); // From client
        wsl.onWriteDemand(this);
    }

    private void channelWrite(List<ByteBuffer> bufs)
            throws InterruptedException {
        for (ByteBuffer b : bufs) {
            channelWrite(b);
        }
    }

    private void channelWriteDirect(ByteBuffer buf) throws IOException {
        while (buf.hasRemaining())
            sockchannel.write(buf);
    }

    private void writeDirect(List<ByteBuffer> bufs) throws IOException {
        for (ByteBuffer b : bufs) {
            channelWriteDirect(b);
        }
    }

    private void deliverMessage(Frame d) throws InvalidDataException {
        if (d.getOpcode() == Opcode.TEXT) {
            wsl.onMessageText(this,
                    Charsetfunctions.stringUtf8(d.getPayloadData()));
        } else if (d.getOpcode() == Opcode.BINARY) {
            wsl.onMessageBinary(this, d.getPayloadData());
        } else {
            if (DEBUG)
                System.out.println("Ignoring frame:" + d.toString());
            assert (false);
        }
    }

    private void open(HttpHeader d) throws IOException {
        if (DEBUG)
            System.out.println("open using this: "
                    + this.getClass().getSimpleName());
        handshakeComplete = true;
        wsl.onStart(this, d);
    }

    public InetSocketAddress getRemoteSocketAddress() {
        return (InetSocketAddress) sockchannel.socket()
                .getRemoteSocketAddress();
    }

    public InetSocketAddress getLocalSocketAddress() {
        return (InetSocketAddress) sockchannel.socket().getLocalSocketAddress();
    }

    public boolean isConnecting() {
        return (!connectionClosed && !closeHandshakeSent && !handshakeComplete);
    }

    public boolean isOpen() {
        return (!connectionClosed && !closeHandshakeSent && handshakeComplete);
    }

    public boolean isClosing() {
        return (!connectionClosed && closeHandshakeSent);
    }

    public boolean isClosed() {
        return connectionClosed;
    }

    public int getReadyState() {
        if (isConnecting()) {
            return READY_STATE_CONNECTING;
        } else if (isOpen()) {
            return READY_STATE_OPEN;
        } else if (isClosing()) {
            return READY_STATE_CLOSING;
        } else if (isClosed()) {
            return READY_STATE_CLOSED;
        }
        assert (false);
        return -1; // < This can't happen, by design!
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString(); // its nice to be able to set breakpoints here
    }

    /*** **/

    public HandshakeState acceptHandshakeAsServer(HttpHeader handshakedata)
            throws InvalidHandshakeException {
        int v = readVersion(handshakedata);
        if (v == 13)
            return HandshakeState.MATCHED;
        return HandshakeState.NOT_MATCHED;
    }

    @SuppressWarnings("serial")
    class IncompleteException extends Throwable {
        private int preferedsize;

        public IncompleteException(int preferedsize) {
            this.preferedsize = preferedsize;
        }

        public int getPreferedSize() {
            return preferedsize;
        }
    }

    public static int readVersion(HttpHeader handshakedata) {
        String vers = handshakedata.getHeader("Sec-WebSocket-Version");
        if (vers.length() > 0) {
            int v;
            try {
                v = new Integer(vers.trim());
                return v;
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private ByteBuffer incompleteframe;

    public List<ByteBuffer> createHandshake(HttpHeader handshakedata,
            Role ownrole, boolean withcontent) {
        StringBuilder bui = new StringBuilder(100);
        if (handshakedata.isClient()) {
            bui.append("GET ");
            bui.append(handshakedata.getResourceDescriptor());
            bui.append(" HTTP/1.1");
        } else if (handshakedata.isServer()) {
            bui.append("HTTP/1.1 101 " + handshakedata.getHttpStatusMessage());
        } else {
            throw new RuntimeException("unknow role");
        }
        bui.append("\r\n");
        Iterator<String> it = handshakedata.getHeaderNames();
        while (it.hasNext()) {
            String fieldname = it.next();
            String fieldvalue = handshakedata.getHeader(fieldname);
            bui.append(fieldname);
            bui.append(": ");
            bui.append(fieldvalue);
            bui.append("\r\n");
        }
        bui.append("\r\n");
        byte[] httpheader = Charsetfunctions.asciiBytes(bui.toString());

        byte[] content = withcontent ? handshakedata.getContent() : null;
        ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0
                : content.length) + httpheader.length);
        bytebuffer.put(httpheader);
        if (content != null)
            bytebuffer.put(content);
        bytebuffer.flip();
        return Collections.singletonList(bytebuffer);
    }

    public HttpHeader translateHandshake(ByteBuffer buf)
            throws InvalidHandshakeException {
        return translateHandshakeHttp(buf, role);
    }

    public int checkAlloc(int bytecount) throws LimitExedeedException,
            InvalidDataException {
        if (bytecount < 0)
            throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR,
                    "Negative count");
        return bytecount;
    }

    public void setParseMode(Role role) {
        this.role = role;
    }

    public HandshakeState acceptHandshakeAsClient(HttpHeader request,
            HttpHeader response) throws InvalidHandshakeException {
        if (!request.hasHeader("Sec-WebSocket-Key")
                || !response.hasHeader("Sec-WebSocket-Accept"))
            return HandshakeState.NOT_MATCHED;

        String seckey_answere = response.getHeader("Sec-WebSocket-Accept");
        String seckey_challenge = request.getHeader("Sec-WebSocket-Key");
        seckey_challenge = generateFinalKey(seckey_challenge);

        if (seckey_challenge.equals(seckey_answere))
            return HandshakeState.MATCHED;
        return HandshakeState.NOT_MATCHED;
    }

    public ByteBuffer createBinaryFrame(Frame framedata) {
        byte[] mes = framedata.getPayloadData();
        boolean mask = role == Role.CLIENT; // framedata.getTransfereMasked();
        int sizebytes = mes.length <= 125 ? 1 : mes.length <= 65535 ? 2 : 8;
        ByteBuffer buf = ByteBuffer.allocate(1
                + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0)
                + mes.length);
        byte optcode = fromOpcode(framedata.getOpcode());
        byte one = (byte) (framedata.isFin() ? -128 : 0);
        one |= optcode;
        buf.put(one);
        byte[] payloadlengthbytes = toByteArray(mes.length, sizebytes);
        assert (payloadlengthbytes.length == sizebytes);

        if (sizebytes == 1) {
            buf.put((byte) ((byte) payloadlengthbytes[0] | (mask ? (byte) -128
                    : 0)));
        } else if (sizebytes == 2) {
            buf.put((byte) ((byte) 126 | (mask ? (byte) -128 : 0)));
            buf.put(payloadlengthbytes);
        } else if (sizebytes == 8) {
            buf.put((byte) ((byte) 127 | (mask ? (byte) -128 : 0)));
            buf.put(payloadlengthbytes);
        } else
            throw new RuntimeException(
                    "Size representation not supported/specified");

        if (mask) {
            ByteBuffer maskkey = ByteBuffer.allocate(4);
            maskkey.putInt(new Random().nextInt());
            buf.put(maskkey.array());
            for (int i = 0; i < mes.length; i++) {
                buf.put((byte) (mes[i] ^ maskkey.get(i % 4)));
            }
        } else
            buf.put(mes);
        // translateFrame ( buf.array () , buf.array ().length );
        assert (buf.remaining() == 0) : buf.remaining();
        buf.flip();

        return buf;
    }

    public List<Frame> createFrames(byte[] binary, boolean mask) {
        Frame curframe = new Frame();
        try {
            curframe.setPayload(binary);
        } catch (InvalidDataException e) {
            throw new NotSendableException(e);
        }
        curframe.setFin(true);
        curframe.setOptcode(Opcode.BINARY);
        curframe.setTransferemasked(mask);
        return Collections.singletonList((Frame) curframe);
    }

    public List<Frame> createFrames(String text, boolean mask) {
        Frame curframe = new Frame();
        byte[] pay = Charsetfunctions.utf8Bytes(text);
        try {
            curframe.setPayload(pay);
        } catch (InvalidDataException e) {
            throw new NotSendableException(e);
        }
        curframe.setFin(true);
        curframe.setOptcode(Opcode.TEXT);
        curframe.setTransferemasked(mask);
        return Collections.singletonList((Frame) curframe);
    }

    private byte fromOpcode(Opcode opcode) {
        if (opcode == Opcode.CONTINIOUS)
            return 0;
        else if (opcode == Opcode.TEXT)
            return 1;
        else if (opcode == Opcode.BINARY)
            return 2;
        else if (opcode == Opcode.CLOSING)
            return 8;
        else if (opcode == Opcode.PING)
            return 9;
        else if (opcode == Opcode.PONG)
            return 10;
        throw new RuntimeException("Don't know how to handle "
                + opcode.toString());
    }

    private String generateFinalKey(String in) {
        String seckey = in.trim();
        String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest sh1;
        try {
            sh1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return Base64.encodeBytes(sh1.digest(acc.getBytes()));
    }

    public HttpHeader postProcessHandshakeRequestAsClient(HttpHeader request) {
        request.putHeader("Upgrade", "websocket");
        request.putHeader("Connection", "Upgrade"); // to respond to a
                                                    // Connection keep
        // alives
        request.putHeader("Sec-WebSocket-Version", "13");// overwriting the
                                                         // previous

        byte[] random = new byte[16];
        new Random().nextBytes(random);
        request.putHeader("Sec-WebSocket-Key", Base64.encodeBytes(random));

        return request;
    }

    public HttpHeader postProcessHandshakeResponseAsServer(HttpHeader request,
            HttpHeader response) throws InvalidHandshakeException {
        response.putHeader("Upgrade", "websocket");
        response.putHeader("Connection", request.getHeader("Connection")); // to
                                                                           // respond
                                                                           // to
                                                                           // a
                                                                           // Connection
                                                                           // keep
                                                                           // alives
        response.setHttpStatusMessage("Switching Protocols");
        String seckey = request.getHeader("Sec-WebSocket-Key");
        if (seckey == null)
            throw new InvalidHandshakeException("missing Sec-WebSocket-Key");
        response.putHeader("Sec-WebSocket-Accept", generateFinalKey(seckey));
        return response;
    }

    private byte[] toByteArray(long val, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for (int i = 0; i < bytecount; i++) {
            buffer[i] = (byte) (val >>> (highest - 8 * i));
        }
        return buffer;
    }

    private Opcode toOpcode(byte opcode) throws InvalidFrameException {
        switch (opcode) {
        case 0:
            return Opcode.CONTINIOUS;
        case 1:
            return Opcode.TEXT;
        case 2:
            return Opcode.BINARY;
            // 3-7 are not yet defined
        case 8:
            return Opcode.CLOSING;
        case 9:
            return Opcode.PING;
        case 10:
            return Opcode.PONG;
            // 11-15 are not yet defined
        default:
            throw new InvalidFrameException("unknow optcode " + (short) opcode);
        }
    }

    public List<Frame> translateFrame(ByteBuffer buffer)
            throws LimitExedeedException, InvalidDataException {
        List<Frame> frames = new LinkedList<Frame>();
        Frame cur;

        if (incompleteframe != null) {
            // complete an incomplete frame
            while (true) {
                try {
                    buffer.mark();
                    int available_next_byte_count = buffer.remaining();// The
                                                                       // number
                                                                       // of
                                                                       // bytes
                                                                       // received
                    int expected_next_byte_count = incompleteframe.remaining();// The
                                                                               // number
                                                                               // of
                                                                               // bytes
                                                                               // to
                                                                               // complete
                                                                               // the
                                                                               // incomplete
                                                                               // frame

                    if (expected_next_byte_count > available_next_byte_count) {
                        // did not receive enough bytes to complete the frame
                        incompleteframe.put(buffer.array(), buffer.position(),
                                available_next_byte_count);
                        buffer.position(buffer.position()
                                + available_next_byte_count);
                        return Collections.emptyList();
                    }
                    incompleteframe.put(buffer.array(), buffer.position(),
                            expected_next_byte_count);
                    buffer.position(buffer.position()
                            + expected_next_byte_count);

                    cur = translateSingleFrame((ByteBuffer) incompleteframe
                            .duplicate().position(0));
                    frames.add(cur);
                    incompleteframe = null;
                    break; // go on with the normal frame receival
                } catch (IncompleteException e) {
                    // extending as much as suggested
                    ByteBuffer extendedframe = ByteBuffer.allocate(checkAlloc(e
                            .getPreferedSize()));
                    assert (extendedframe.limit() > incompleteframe.limit());
                    incompleteframe.rewind();
                    extendedframe.put(incompleteframe);
                    incompleteframe = extendedframe;

                    return translateFrame(buffer);
                }
            }
        }

        while (buffer.hasRemaining()) {// Read as much as possible full frames
            buffer.mark();
            try {
                cur = translateSingleFrame(buffer);
                frames.add(cur);
            } catch (IncompleteException e) {
                // remember the incomplete data
                buffer.reset();
                int pref = e.getPreferedSize();
                incompleteframe = ByteBuffer.allocate(checkAlloc(pref));
                incompleteframe.put(buffer.array(), buffer.position(),
                        buffer.remaining());
                buffer.position(buffer.position() + buffer.remaining());
                break;
            }
        }
        return frames;
    }

    public Frame translateSingleFrame(ByteBuffer buffer)
            throws IncompleteException, InvalidDataException {
        int maxpacketsize = buffer.limit() - buffer.position();
        int realpacketsize = 2;
        if (maxpacketsize < realpacketsize)
            throw new IncompleteException(realpacketsize);
        byte b1 = buffer.get( /* 0 */);
        boolean FIN = b1 >> 8 != 0;
        byte rsv = (byte) ((b1 & ~(byte) 128) >> 4);
        if (rsv != 0)
            throw new InvalidFrameException("bad rsv " + rsv);
        byte b2 = buffer.get( /* 1 */);
        boolean MASK = (b2 & -128) != 0;
        int payloadlength = (byte) (b2 & ~(byte) 128);
        Opcode optcode = toOpcode((byte) (b1 & 15));

        if (!FIN) {
            if (optcode == Opcode.PING || optcode == Opcode.PONG
                    || optcode == Opcode.CLOSING) {
                throw new InvalidFrameException(
                        "control frames may no be fragmented");
            }
        }

        if (payloadlength >= 0 && payloadlength <= 125) {
        } else {
            if (optcode == Opcode.PING || optcode == Opcode.PONG
                    || optcode == Opcode.CLOSING) {
                throw new InvalidFrameException("more than 125 octets");
            }
            if (payloadlength == 126) {
                realpacketsize += 2; // additional length bytes
                if (maxpacketsize < realpacketsize)
                    throw new IncompleteException(realpacketsize);
                byte[] sizebytes = new byte[3];
                sizebytes[1] = buffer.get( /* 1 + 1 */);
                sizebytes[2] = buffer.get( /* 1 + 2 */);
                payloadlength = new BigInteger(sizebytes).intValue();
            } else {
                realpacketsize += 8; // additional length bytes
                if (maxpacketsize < realpacketsize)
                    throw new IncompleteException(realpacketsize);
                byte[] bytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                    bytes[i] = buffer.get( /* 1 + i */);
                }
                long length = new BigInteger(bytes).longValue();
                if (length > Integer.MAX_VALUE) {
                    throw new LimitExedeedException("Payloadsize is to big...");
                } else {
                    payloadlength = (int) length;
                }
            }
        }

        // int maskskeystart = foff + realpacketsize;
        realpacketsize += (MASK ? 4 : 0);
        // int payloadstart = foff + realpacketsize;
        realpacketsize += payloadlength;

        if (maxpacketsize < realpacketsize)
            throw new IncompleteException(realpacketsize);

        ByteBuffer payload = ByteBuffer.allocate(checkAlloc(payloadlength));
        if (MASK) {
            byte[] maskskey = new byte[4];
            buffer.get(maskskey);
            for (int i = 0; i < payloadlength; i++) {
                payload.put((byte) ((byte) buffer.get( /* payloadstart + i */) ^ (byte) maskskey[i % 4]));
            }
        } else {
            payload.put(buffer.array(), buffer.position(), payload.limit());
            buffer.position(buffer.position() + payload.limit());
        }

        Frame frame;
        if (optcode == Opcode.CLOSING) {
            frame = new CloseFrame();
        } else {
            frame = new Frame();
            frame.setFin(FIN);
            frame.setOptcode(optcode);
        }
        frame.setPayload(payload.array());
        return frame;
    }

    public void reset() {
        incompleteframe = null;
    }

    public boolean hasCloseHandshake() {
        return true;
    }

    /****/

    public enum HandshakeState {
        /** Handshake matched this Draft successfully */
        MATCHED,
        /** Handshake is does not match this Draft */
        NOT_MATCHED,
        /** Handshake matches this Draft but is not complete */
        MATCHING
    }

    /**
     * In some cases the handshake will be parsed different depending on whether
     */
    protected Role role = null;

    public static ByteBuffer readLine(ByteBuffer buf) {
        ByteBuffer sbuf = ByteBuffer.allocate(buf.remaining());
        byte prev = '0';
        byte cur = '0';
        while (buf.hasRemaining()) {
            prev = cur;
            cur = buf.get();
            sbuf.put(cur);
            if (prev == (byte) '\r' && cur == (byte) '\n') {
                sbuf.limit(sbuf.position() - 2);
                sbuf.position(0);
                return sbuf;

            }
        }
        // ensure that there wont be any bytes skipped
        buf.position(buf.position() - sbuf.position());
        return null;
    }

    public static String readStringLine(ByteBuffer buf) {
        ByteBuffer b = readLine(buf);
        return b == null ? null : Charsetfunctions.stringAscii(b.array(), 0,
                b.limit());
    }

    public static HttpHeader translateHandshakeHttp(ByteBuffer buf, Role role)
            throws InvalidHandshakeException {
        HttpHeader handshake;

        String line = readStringLine(buf);
        if (line == null)
            throw new InvalidHandshakeException(
                    "could not match http status line");

        String[] firstLineTokens = line.split(" ", 3);// eg. HTTP/1.1 101
                                                      // Switching the Protocols
        if (firstLineTokens.length != 3) {
            throw new InvalidHandshakeException();
        }

        if (role == Role.CLIENT) {
            handshake = HttpHeader.createServerRequest();
            handshake.setHttpStatusMessage(firstLineTokens[2]);
        } else {
            // translating/parsing the request from the CLIENT
            HttpHeader clienthandshake = HttpHeader.createClientRequest();
            clienthandshake.setResourceDescriptor(firstLineTokens[1]);
            handshake = clienthandshake;
        }

        line = readStringLine(buf);
        while (line != null && line.length() > 0) {
            String[] pair = line.split(":", 2);
            if (pair.length != 2)
                throw new InvalidHandshakeException("not an http header");
            handshake.putHeader(pair[0], pair[1].replaceFirst("^ +", ""));
            line = readStringLine(buf);
        }
        return handshake;
    }

    protected boolean basicAccept(HttpHeader handshakedata) {
        return handshakedata.getHeader("Upgrade").equalsIgnoreCase("websocket")
                && handshakedata.getHeader("Connection")
                        .toLowerCase(Locale.ENGLISH).contains("upgrade");
    }

    public List<ByteBuffer> createHandshake(HttpHeader handshakedata,
            Role ownrole) {
        return createHandshake(handshakedata, ownrole, true);
    }

    private Thread thread;

    public void clientClose() {
        if (thread != null) {
            thread.interrupt();
            clientCloseLock.lock();
            if (selector != null)
                selector.wakeup();
            clientCloseLock.unlock();
        }

    }

    public void startClient() {
        thread = new Thread(new Runnable() {

            public void run() {
                runClient();
            }
        });
        thread.start();

    }

    public void runClient() {
        try/* IO */{
            while (!this.isClosed()) {
                if (Thread.interrupted()) {
                    this.close(CloseFrame.NORMAL);
                }
                SelectionKey key = null;
                this.flush();
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> i = keys.iterator();
                while (i.hasNext()) {
                    key = i.next();
                    i.remove();
                    if (key.isReadable()) {
                        this.handleRead();
                    }
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isWritable()) {
                        this.flush();
                    }
                    if (key.isConnectable()) {
                        try {
                            finishConnect();
                        } catch (InterruptedException e) {
                            this.close(CloseFrame.NEVERCONNECTED);// report
                                                                  // error to
                                                                  // only
                            break;
                        } catch (InvalidHandshakeException e) {
                            this.close(e); // http error
                            this.flush();
                        }
                    }
                }
            }
        } catch (IOException e) {
            wsl.errorHandler(this, e);
            this.close(CloseFrame.ABNROMAL_CLOSE);
            return;
        } catch (RuntimeException e) {
            // this catch case covers internal errors only and indicates a bug
            // in this websocket implementation
            wsl.errorHandler(this, e);
            this.close(CloseFrame.BUGGYCLOSE);
            return;
        }

        try {
            selector.close();
        } catch (IOException e) {
            // onError( e );
        }
        clientCloseLock.lock();
        selector = null;
        clientCloseLock.unlock();
        try {
            this.sockchannel.close();
        } catch (IOException e) {
            wsl.errorHandler(this, e);
        }
        this.sockchannel = null;
    }

    private void finishConnect() throws IOException, InvalidHandshakeException,
            InterruptedException {
        if (sockchannel.isConnectionPending()) {
            sockchannel.finishConnect();
        }

        // Now that we're connected, re-register for only 'READ' keys.
        sockchannel.register(selector, SelectionKey.OP_READ);

        String path;
        String part1 = clientURI.getPath();
        String part2 = clientURI.getQuery();
        if (part1 == null || part1.length() == 0)
            path = "/";
        else
            path = part1;
        if (part2 != null)
            path += "?" + part2;
        String host = clientURI.getHost() + (port != 80 ? ":" + port : "");

        HttpHeader handshake = HttpHeader.createClientRequest();
        handshake.setResourceDescriptor(path);
        handshake.putHeader("Host", host);
        this.startHandshake(handshake);
    }

}
