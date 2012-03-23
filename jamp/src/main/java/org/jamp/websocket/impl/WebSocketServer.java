package org.jamp.websocket.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <tt>WebSocketServer</tt> is an abstract class that only takes care of the
 * HTTP handshake portion of WebSockets. It's up to a subclass to add
 * functionality/purpose to the server.
 * 
 * @author Nathan Rajlich (original author)
 * @author rick
 */
public abstract class WebSocketServer {

    // INSTANCE PROPERTIES /////////////////////////////////////////////////////
    /**
     * Holds the list of active WebSocket connections. "Active" means WebSocket
     * handshake is complete and socket can be written to, or read from.
     */
    private final CopyOnWriteArraySet<WebSocketInternalImpl> connections;
    /**
     * The port number that this WebSocket server should listen on. Default is
     * WebSocket.DEFAULT_PORT.
     */
    private InetSocketAddress address;
    /**
     * The socket channel for this WebSocket server.
     */
    private ServerSocketChannel serverSocketChannel;
    /**
     * The 'Selector' used to get event keys from the underlying socket.
     */
    private Selector selector;

    private Thread thread;

    /**
     * Creates a WebSocketServer that will attempt to listen on port
     * <var>80</var>.
     */
    public WebSocketServer() throws UnknownHostException {
        this(new InetSocketAddress(InetAddress.getLocalHost(), 80));
    }

    /**
     * Creates a WebSocketServer that will attempt to bind/listen on the given
     * <var>address</var>, and comply with <tt>Draft</tt> version
     * <var>draft</var>.
     * 
     * @param address
     *            The address (host:port) this server should listen on.
     * @param draft
     *            The version of the WebSocket protocol that this server
     *            instance should comply to.
     */
    public WebSocketServer(InetSocketAddress address) {
        this.connections = new CopyOnWriteArraySet<WebSocketInternalImpl>();
        setAddress(address);
    }

    /**
     * Starts the server thread that binds to the currently set port number and
     * listeners for WebSocket connection requests.
     * 
     * @throws IllegalStateException
     */
    @SuppressWarnings("nls")
    public void start() {
        if (thread != null)
            throw new IllegalStateException("Already started");
        new Thread(new Runnable() {

            @Override
            public void run() {
                WebSocketServer.this.run();
            }
        }).start();
    }

    /**
     * Closes all connected clients sockets, then closes the underlying
     * ServerSocketChannel, effectively killing the server socket thread and
     * freeing the port the server was bound to.
     * 
     * @throws IOException
     *             When socket related I/O errors occur.
     */
    public void stop() throws IOException {
        for (WebSocketInternal ws : connections) {
            ws.close(CloseFrame.NORMAL);
        }
        thread.interrupt();
        this.serverSocketChannel.close();

    }

    /**
     * Returns a WebSocket[] of currently connected clients.
     * 
     * @return The currently connected clients in a WebSocket[].
     */
    public Set<WebSocketInternalImpl> connections() {
        return Collections.unmodifiableSet(this.connections);
    }

    /**
     * Sets the address (host:port) that this WebSocketServer should listen on.
     * 
     * @param address
     *            The address (host:port) to listen on.
     */
    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    /**
     * Gets the port number that this server listens on.
     * 
     * @return The port number.
     */
    public int getPort() {
        return getAddress().getPort();
    }

    class ClientServicer {
        SocketChannel socketChannel;
        Selector clientSelector;

        ClientServicer(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            clientSelector = Selector.open();
            WebSocketInternal connection = createConnection(socketChannel);
            socketChannel.register(clientSelector, SelectionKey.OP_READ,
                    connection);

        }

        Thread t;

        void start() {

            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    runIt();
                }
            });

        }

        void runIt() {
            while (!thread.isInterrupted()) {
                WebSocketInternalImpl conn = null;
                SelectionKey key = null;
                ;

                try {
                    clientSelector.select();
                    Set<SelectionKey> keys = clientSelector.selectedKeys();
                    Iterator<SelectionKey> i = keys.iterator();

                    while (i.hasNext()) {
                        key = i.next();

                        i.remove();

                        if (key.isReadable()) {
                            conn = (WebSocketInternalImpl) key.attachment();
                            conn.handleRead();
                        }

                        if (key.isValid() && key.isWritable()) {
                            conn = (WebSocketInternalImpl) key.attachment();
                            conn.flush();
                            key.channel().register(clientSelector,
                                    SelectionKey.OP_READ, conn);
                        }
                    }
                } catch (IOException ex) {
                    if (key != null)
                        key.cancel();
                    onError(conn, ex);// conn may be null here
                    if (conn != null) {
                        conn.close(CloseFrame.ABNROMAL_CLOSE);
                    }
                }
            }

        }

    }

    // Runnable IMPLEMENTATION /////////////////////////////////////////////////
    @SuppressWarnings("nls")
    public void run() {
        if (thread != null)
            throw new IllegalStateException("This instance of "
                    + getClass().getSimpleName()
                    + " can only be started once the same time.");
        thread = Thread.currentThread();
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(address);
            // InetAddress.getLocalHost()
            selector = Selector.open();
            serverSocketChannel.register(selector,
                    serverSocketChannel.validOps());
        } catch (IOException ex) {
            onError(null, ex);
            return;
        }

        while (!thread.isInterrupted()) {
            WebSocketInternalImpl conn = null;
            SelectionKey key = null;
            ;

            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> i = keys.iterator();

                while (i.hasNext()) {
                    key = i.next();

                    // Remove the current key
                    i.remove();

                    // if isAcceptable == true
                    // then a client required a connection
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel
                                .accept();
                        socketChannel.configureBlocking(false);
                        // ClientServicer cs = new
                        // ClientServicer(socketChannel);
                        // cs.start();
                        WebSocketInternal connection = createConnection(socketChannel);
                        socketChannel.register(selector, SelectionKey.OP_READ,
                                connection);
                    }

                    // if isReadable == true
                    // then the server is ready to read
                    if (key.isReadable()) {
                        conn = (WebSocketInternalImpl) key.attachment();
                        conn.handleRead();
                    }

                    // if isWritable == true
                    // then we need to send the rest of the data to the client
                    if (key.isValid() && key.isWritable()) {
                        conn = (WebSocketInternalImpl) key.attachment();
                        conn.flush();
                        key.channel().register(selector, SelectionKey.OP_READ,
                                conn);
                    }
                }
                Iterator<WebSocketInternalImpl> it = this.connections
                        .iterator();
                while (it.hasNext()) {
                    // We have to do this check here, and not in the thread that
                    // adds the buffered data to the WebSocket, because the
                    // Selector is not thread-safe, and can only be accessed
                    // by this thread.
                    conn = it.next();
                    if (conn.hasBufferedData()) {
                        conn.flush();
                        // key.channel().register( selector,
                        // SelectionKey.OP_READ | SelectionKey.OP_WRITE, conn );
                    }
                }
            } catch (IOException ex) {
                if (key != null)
                    key.cancel();
                onError(conn, ex);// conn may be null here
                if (conn != null) {
                    conn.close(CloseFrame.ABNROMAL_CLOSE);
                }
            }
        }
    }

    private WebSocketInternal createConnection(SocketChannel clientSocketChannel) {
        WebSocketInternal connection = WebSocketInternalImpl
                .createServerWebSocket(new LowLevelListenerAdapter() {

                    @Override
                    public final void onMessageText(WebSocketInternal conn,
                            String message) {
                        onMessage(conn, message);
                    }

                    @Override
                    public final void onMessageBinary(WebSocketInternal conn,
                            byte[] blob) {
                        onMessage(conn, blob);
                    }

                    @Override
                    public final void onStart(WebSocketInternalImpl conn,
                            HttpHeader handshake) {
                        if (connections.add(conn)) {
                            onOpen(conn, handshake);
                        }
                    }

                    @Override
                    public final void onWebsocketClose(WebSocketInternal conn,
                            int code, String reason, boolean remote) {
                        if (connections.remove(conn)) {
                            onClose(conn, code, reason, remote);
                        }
                    }

                    /**
                     * @param conn
                     *            may be null if the error does not belong to a
                     *            single connection
                     */
                    @Override
                    public final void errorHandler(WebSocketInternal conn,
                            Exception ex) {
                        onError(conn, ex);
                    }

                    @Override
                    public final void onWriteDemand(WebSocketInternal conn) {
                        selector.wakeup();
                    }

                }, clientSocketChannel);
        return connection;
    }

    public void onOpen(@SuppressWarnings("unused") WebSocketInternal conn,
            @SuppressWarnings("unused") HttpHeader handshake) {
    }

    public void onClose(@SuppressWarnings("unused") WebSocketInternal conn,
            @SuppressWarnings("unused") int code,
            @SuppressWarnings("unused") String reason,
            @SuppressWarnings("unused") boolean remote) {
    }

    public void onMessage(@SuppressWarnings("unused") WebSocketInternal conn,
            @SuppressWarnings("unused") String message) {
    }

    public void onError(@SuppressWarnings("unused") WebSocketInternal conn,
            @SuppressWarnings("unused") Exception ex) {
    }

    public void onMessage(@SuppressWarnings("unused") WebSocketInternal conn,
            @SuppressWarnings("unused") byte[] message) {
    };

}
