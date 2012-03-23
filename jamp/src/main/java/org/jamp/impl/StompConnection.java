package org.jamp.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jamp.MessageQueueConnection;
import org.jamp.JampMessageURL;

/**
 * This only works with Strings for now, because that is all we need for this
 * iteration. Needs logging badly.
 */
public class StompConnection implements MessageQueueConnection {

    enum Version {
        ONE, ONE_ONE
    };

    Version version;
    int currentSubscription;
    Map<Integer, MQMessageListener> subscriptions = Collections
            .synchronizedMap(new HashMap<Integer, MQMessageListener>());
    Map<MQMessageListener, Integer> subscriptionsIds = Collections
            .synchronizedMap(new HashMap<MQMessageListener, Integer>());
    String host;
    String port;
    Socket socket;
    PrintWriter out;
    BufferedReader reader;// This is somewhat limiting since Stomp can do both
                          // bytes and strings, but this is it for now, just
                          // text

    BlockingQueue<String> closeQueue = new ArrayBlockingQueue<String>(1);
    boolean debug = true;
    boolean connected;
    static ThreadPoolExecutor threadPool = null;
    int receiptNumber;

    /**
     * This should be called before connect is ever called. Not a requirement to
     * ever call this, but if you do, do it before connect.
     */
    public static void setThreadPool(ThreadPoolExecutor athreadPool) {
        threadPool = athreadPool;

    }

    @Override
    public void connect(String connectionString, String login, String passcode,
            Object... config) throws IOException {

        if (connected) {
            throw new IOException(Messages.getString("StompConnection.0")); //$NON-NLS-1$
        }

        JampMessageURL messageURL = new JampMessageURL(connectionString);

        if (!messageURL.getScheme().equals("stomp")) { //$NON-NLS-1$
            throw new IllegalStateException(
                    Messages.getString("StompConnection.1")); //$NON-NLS-1$
        }

        this.host = messageURL.getHost();
        this.port = messageURL.getPort();

        /* Create a new socket. */
        socket = new Socket(host, Integer.parseInt(port));
        out = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));

        /* Send the connect command to authenticate. */
        sendCommand(
                "CONNECT", String.format(Messages.getString("StompConnection.2"), host), Messages.getString("StompConnection.3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                String.format("login:%s", login), String.format(Messages.getString("StompConnection.4"), passcode)); //$NON-NLS-1$ //$NON-NLS-2$

        /* Look for CONNECTED string. */
        String line = reader.readLine();

        if (!line.equals("CONNECTED")) { //$NON-NLS-1$
            throw new IOException(Messages.getString("StompConnection.5")); //$NON-NLS-1$
        }

        /*
         * Read the headers and see if we are compatible with the server's
         * version.
         */
        Properties headers = readHeaders();
        String sversion = headers.getProperty("version"); //$NON-NLS-1$

        if (sversion.equals("1.1")) { //$NON-NLS-1$
            version = Version.ONE_ONE;
        } else if (version.equals("1")) { //$NON-NLS-1$
            version = Version.ONE;
        } else {
            throw new IOException(Messages.getString("StompConnection.6") //$NON-NLS-1$
                    + sversion);
        }

        if (debug)
            System.out
                    .printf(Messages.getString("StompConnection.7"), sversion); //$NON-NLS-1$

        /* Clear out and validate the rest of the message. */
        readResult();

        connected = true;

        runMessageDispatchLoop();
    }

    private void runMessageDispatchLoop() {

        /* Create the pool if it does not exist. */
        synchronized (StompConnection.class) {
            if (threadPool == null) {
                threadPool = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(10));
            }
        }

        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                dispatchLoop();
            }
        });

    }

    /** The actual dispatch look that handles command frames from the server. */
    @SuppressWarnings("nls")
    private void dispatchLoop() {
        try {

            String line = null;
            while (connected && socket != null && socket.isConnected()
                    && (line = reader.readLine()) != null) {

                /* Ignore heart beat */
                if (Messages.getString("StompConnection.8").equals(line.trim())) { //$NON-NLS-1$
                    continue;
                }

                if (Messages.getString("StompConnection.9").equals(line)) { //$NON-NLS-1$
                    handleMessage();
                } else if (Messages
                        .getString("StompConnection.10").equals(line)) { //$NON-NLS-1$
                    handleReceipt();
                } else if (Messages
                        .getString("StompConnection.11").equals(line)) { //$NON-NLS-1$
                    handleError();
                } else {
                    if (debug)
                        System.out.println(Messages
                                .getString("StompConnection.12") + line); //$NON-NLS-1$
                }

            }

        } catch (SocketException se) {
            if (se.getMessage().contains(
                    Messages.getString("StompConnection.13"))) { //$NON-NLS-1$
                return; // this is ok... it just means that the server
                        // disconnected.
            }
            se.printStackTrace();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void handleError() throws IOException {
        this.readHeaders();

        StringBuilder readBody = readBody(0);

        System.err.printf(Messages.getString("StompConnection.14"), readBody); //$NON-NLS-1$

    }

    private void handleReceipt() throws IOException {
        Properties headers = this.readHeaders();
        String receipt = headers.getProperty(Messages
                .getString("StompConnection.15")); //$NON-NLS-1$

        if (debug)
            System.out
                    .printf(Messages.getString("StompConnection.16"), receipt); //$NON-NLS-1$

        if (receipt != null
                && receipt.startsWith(Messages.getString("StompConnection.17"))) { //$NON-NLS-1$
            String[] split = receipt.split(Messages
                    .getString("StompConnection.18")); //$NON-NLS-1$
            closeQueue.offer(split[1]);
        }

        readBody(0);

    }

    @SuppressWarnings("nls")
    private void handleMessage() throws IOException {
        Properties headers = this.readHeaders();

        String subscription = headers.getProperty(Messages
                .getString("StompConnection.19")); //$NON-NLS-1$
        String messageId = headers.getProperty(Messages
                .getString("StompConnection.20")); //$NON-NLS-1$
        String destination = headers.getProperty(Messages
                .getString("StompConnection.21")); //$NON-NLS-1$
        int length = 0;

        if (subscription == null) {
            System.err.println(Messages.getString("StompConnection.22")); //$NON-NLS-1$
        }

        if (messageId == null) {
            // throw new IOException("messageId missing from message");
        }

        if (destination == null) {
            // throw new IOException("destination missing from message");
        }

        String slength = headers.getProperty(Messages
                .getString("StompConnection.23")); //$NON-NLS-1$

        if (slength != null) {
            length = Integer.parseInt(slength);
        }

        if (debug)
            System.out
                    .printf(Messages.getString("StompConnection.24"), subscription, messageId, destination, length); //$NON-NLS-1$

        final StringBuilder body = readBody(length);

        synchronized (this) {
            if (subscription != null) {
                MQMessageListener messageListener = this.subscriptions
                        .get(Integer.parseInt(subscription));
                if (messageListener != null) {
                    try {
                        messageListener.onTextMessage(body.toString());
                    } catch (Exception ex) {
                        // you need to start logging stuff
                    }
                }
            }
        }

    }

    private StringBuilder readBody(int length) throws IOException {

        if (length == 0) {
            final StringBuilder body = new StringBuilder();

            for (char ch = (char) reader.read(); ch != 0; ch = (char) reader
                    .read()) {
                body.append(ch);
            }
            return body;
        }
        final StringBuilder body = new StringBuilder();
        char[] buffer = new char[length];
        int actual = reader.read(buffer, 0, length);
        body.append(buffer);
        if (actual != length) {
            throw new IOException(String.format(
                    Messages.getString("StompConnection.25"), length)); //$NON-NLS-1$

        }
        int ch = reader.read();
        if (ch != 0) {
            throw new IOException(String.format(
                    Messages.getString("StompConnection.26"), length)); //$NON-NLS-1$
        }
        return body;

    }

    private String readResult() throws IOException {
        StringBuilder builder = new StringBuilder();
        for (char ch = (char) reader.read(); ch != 0; ch = (char) reader.read()) {
            builder.append(ch);
        }

        String result = builder.toString();
        return result;
    }

    void sendCommand(String command, String... headers) {

        this.sendCommandWithBody(command, null, headers);

    }

    @SuppressWarnings("nls")
    void sendCommandWithBody(String command, String body, String... headers) {

        out.printf(Messages.getString("StompConnection.27"), command); //$NON-NLS-1$
        for (String header : headers) {
            out.printf(Messages.getString("StompConnection.28"), header); //$NON-NLS-1$
        }
        out.println();
        if (body != null) {
            out.print(body);
        }
        out.printf(Messages.getString("StompConnection.29")); //$NON-NLS-1$

    }

    @SuppressWarnings("nls")
    private Properties readHeaders() throws IOException {
        Properties props = new Properties();
        String line;
        while ((line = reader.readLine()) != null) {
            if (Messages.getString("StompConnection.30").equals(line)) { //$NON-NLS-1$
                break;
            }
            String[] split = line.split(Messages
                    .getString("StompConnection.31")); //$NON-NLS-1$
            if (split.length == 2)
                props.put(split[0], split[1]);

        }

        if (debug)
            System.out
                    .println(Messages.getString("StompConnection.32") + props); //$NON-NLS-1$

        return props;

    }

    @Override
    @SuppressWarnings("nls")
    public void send(String destination, Object oMessage) throws IOException {
        if (!connected)
            throw new IOException(Messages.getString("StompConnection.33")); //$NON-NLS-1$

        if (oMessage instanceof String) {
            String message = (String) oMessage;
            this.sendCommandWithBody(
                    Messages.getString("StompConnection.34"), message, //$NON-NLS-1$
                    String.format(
                            Messages.getString("StompConnection.35"), destination), //$NON-NLS-1$
                    Messages.getString("StompConnection.36"), //$NON-NLS-1$
                    String.format(Messages.getString("StompConnection.37"), //$NON-NLS-1$
                            message.length()));
        } else {
            // we don't handle byte arrays yet.
        }

    }

    @SuppressWarnings("nls")
    @Override
    public void subscribe(String destination, MQMessageListener messageListener)
            throws IOException {
        if (!connected)
            throw new IOException(Messages.getString("StompConnection.38")); //$NON-NLS-1$

        int id = 0;
        synchronized (this) {
            this.currentSubscription++;
            id = this.currentSubscription;
            this.subscriptions.put(id, messageListener);
            this.subscriptionsIds.put(messageListener, id);
        }

        this.sendCommand(
                Messages.getString("StompConnection.39"), String.format(Messages.getString("StompConnection.40"), id), String.format(Messages.getString("StompConnection.41"), destination)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    }

    @SuppressWarnings("nls")
    @Override
    public void unsubscribe(String destination,
            MQMessageListener messageListener) throws IOException {
        if (!connected)
            throw new IOException(Messages.getString("StompConnection.42")); //$NON-NLS-1$

        int id = 0;
        synchronized (this) {
            this.subscriptions.remove(messageListener);
            id = this.subscriptionsIds.get(messageListener);
            subscriptionsIds.remove(id);
        }
        this.sendCommand(
                Messages.getString("StompConnection.43"), String.format(Messages.getString("StompConnection.44"), id)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @SuppressWarnings("nls")
    @Override
    public void close() throws IOException {

        int latest = 0;
        synchronized (this) {// not really needed
            latest = this.receiptNumber++;
        }
        String receipt = Messages.getString("StompConnection.45") + latest; //$NON-NLS-1$

        this.sendCommand(
                Messages.getString("StompConnection.46"), String.format(Messages.getString("StompConnection.47"), receipt)); //$NON-NLS-1$ //$NON-NLS-2$

        try {
            String receiptFromServer = closeQueue.poll(10, TimeUnit.SECONDS);
            if ((Messages.getString("StompConnection.48") + latest).equals(receiptFromServer)) { //$NON-NLS-1$
                if (debug)
                    System.out
                            .println(Messages.getString("StompConnection.49")); //$NON-NLS-1$
                this.socket.close();
                this.socket = null;
            }
        } catch (InterruptedException e) {
            throw new IOException(Messages.getString("StompConnection.50")); //$NON-NLS-1$
        } finally {
            this.connected = false;

            try {
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException ioe) {
            }

        }
    }

}
