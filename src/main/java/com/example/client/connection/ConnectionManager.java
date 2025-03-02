package com.example.client.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.example.client.store.ConfigStore;
import afrl.ntf.broker_to_mms.v1.BrokerToMMSMessageOuterClass.BrokerToMMSMessage;

public class ConnectionManager {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private volatile boolean connected;

    /**
     * Establishes the socket connection using the TA2 endpoint from the configuration.
     */
    public void connect() throws IOException {
        String[] endpoint = ConfigStore.getInstance().getSettings().getTa2Endpoint().split(":");
        String host = endpoint[0];
        int port = Integer.parseInt(endpoint[1]);

        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        connected = true;
        System.out.println("Connected to server at " + host + ":" + port);
    }

    /**
     * Closes the socket and I/O streams.
     */
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public DataInputStream getInputStream() {
        return in;
    }

    /**
     * Sends a BrokerToMMSMessage over the connection.
     */
    public void sendMessage(BrokerToMMSMessage message) throws IOException {
        byte[] bytes = message.toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }
}
