package com.hichigo.websocket;

import com.hichigo.websocket.messages.Hello;
import com.hichigo.websocket.messages.Ping;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * BackendWebSocket:
 *  Creates a socket for communicating
 *  between Electron frontend to Java backend
 *
 * @author cool guy
 * @version 1.0
 */
public class BackendWebSocket extends WebSocketServer {
    public static final Logger logger = Logger.getLogger(BackendWebSocket.class.getName());
    private static final int connectionLostTimeout = 100;
    private static final int heartbeatInterval = 1000;

    /** Initialize new message parser.*/
    private MessageParser messageParser = new MessageParser();

    /** Initialize new heartbeat thread */
    private final HeartbeatThread heartbeatRunnable = new HeartbeatThread(heartbeatInterval, this);
    private Thread heartbeatThread = new Thread(heartbeatRunnable);

    /** Properties */
    public Boolean isTerminated = false;

    /**
     * Initializes the class.
     * @param port WebSocket port
     */
    public BackendWebSocket(int port) {
        // Start the web socket
        super(new InetSocketAddress(port));
        logger.log(Level.INFO, "Web socket class has been initialized.");
    }

    /**
     * Returns the termination state
     * of the web socket server.
     */
    public boolean isTerminated() {
        return this.isTerminated;
    }

    /**
     * Close the web socket server
     * and dispose the class.
     */
    public void terminate() throws IOException, InterruptedException {
        isTerminated = true; this.stop();
        heartbeatThread.join();

        heartbeatThread = null;
        isTerminated = null;
        messageParser = null;
    }

    /**
     * Triggers when a new connection establishes.
     * @param webSocket Connected WebSocket
     * @param clientHandshake Handshake packet
     */
    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.log(Level.INFO, String.format("New connection established: %s", clientHandshake.getResourceDescriptor()));

        // Generate the hello message
        // TODO: Implement a ".setSettings(settings)" function
        Hello helloMessage = new Hello.Builder()
                .setOnlineMachineIds(Arrays.asList((String[])MessageParser.onlineMachines.keySet().toArray()))
                .build();

        // Send the message
        webSocket.send(helloMessage.toString());
    }

    /**
     * Triggers when a connection closes.
     * @param webSocket Disconnected WebSocket
     * @param code Disconnection code
     * @param reason Disconnection reason
     * @param remote I dunno what this is
     */
    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        logger.log(Level.INFO, String.format("Connection has been closed; code: %d reason: %s remote: %b", code, reason, remote));
    }

    /**
     * Triggers when new message comes.
     * @param webSocket WebSocket that message coming from
     * @param message New message
     */
    @Override
    public void onMessage(WebSocket webSocket, String message) {
        logger.log(Level.INFO, String.format("New message: %s", message));
        messageParser.parseMessage(this, message);
    }

    /**
     * Triggers when WebSocket got thrown a exception.
     * @param webSocket WebSocket that got exception
     * @param e Exception message
     */
    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.trace("Exception thrown.", e);
    }

    /**
     * Triggers when the socket is ready.
     */
    @Override
    public void onStart() {
        logger.log(Level.INFO, "Backend web socket is up!");

        // Set connection timeout
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(connectionLostTimeout);

        // Start the ping-pong thread
        heartbeatThread.start();
    }

    /**
     * HeartbeatThread
     *  Sends an heartbeat every x times to the broadcast
     */
    private static class HeartbeatThread implements Runnable {
        private final int interval;
        private final BackendWebSocket webSocket;

        /** Initialize the class */
        public HeartbeatThread(int interval, BackendWebSocket webSocket) {
            this.interval = interval;
            this.webSocket = webSocket;
        }

        /** Thread function to run until socket is closed. */
        @Override
        public void run() {
            while (!webSocket.isTerminated) {
                // Sleep the interval
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    // Silent
                    return;
                }

                // Check if web socket got connection
                if (webSocket.getConnections().size() <= 0) continue;

                // Get online machine ids
                String[] machineIds = new String[MessageParser.onlineMachines.keySet().size()];
                machineIds = MessageParser.onlineMachines.keySet().toArray(machineIds);

                // Generate ping message
                Ping ping = new Ping.Builder()
                        .setMachineCountries(MessageParser.machineCountries)
                        .setSettings(MessageParser.settings)
                        .setOnlineMachineIds(Arrays.asList(machineIds))
                        .setMachineContexts(MessageParser.machineContexts)
                        .build();

                // Broadcast the update
                webSocket.broadcast(ping.toString());
            }
        }
    }
}
