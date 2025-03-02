package com.example.client.message;

import java.io.DataInputStream;
import java.io.IOException;

import com.google.protobuf.InvalidProtocolBufferException;

import afrl.ntf.mms_to_broker.v1.MMSToBrokerMessageOuterClass.MMSToBrokerMessage;
import com.example.client.connection.ConnectionManager;

public class MessageReceiver implements Runnable {
    private static final int SLEEP_MILLIS = 100;
    private final ConnectionManager connectionManager;
    private final DataInputStream in;
    private final MessageHandler messageHandler;

    public MessageReceiver(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.in = connectionManager.getInputStream();
        this.messageHandler = new MessageHandler();
    }

    @Override
    public void run() {
        try {
            while (connectionManager.isConnected()) {
                if (in.available() > 0) {
                    int length = in.readInt();
                    byte[] bytes = new byte[length];
                    in.readFully(bytes);
                    try {
                        MMSToBrokerMessage message = MMSToBrokerMessage.parseFrom(bytes);
                        messageHandler.handleServerMessage(message);
                    } catch (InvalidProtocolBufferException e) {
                        System.out.println("Invalid protobuf message received.");
                    }
                }
                Thread.sleep(SLEEP_MILLIS);
            }
        } catch (InterruptedException e) {
            System.out.println("Message receiver interrupted.");
        } catch (IOException e) {
            if (connectionManager.isConnected()) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }
    }
}
