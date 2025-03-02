package com.example.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.BrokerServiceMessage;
import afrl.ntf.broker_to_mms.v1.BrokerToMMSMessageOuterClass.BrokerToMMSMessage;
import afrl.ntf.broker_to_mms.v1.PathMetricInfoRequestOuterClass.PathMetricInfoRequest;
import afrl.ntf.common.v1.PathAttributeOuterClass.PathAttribute;
import afrl.ntf.common.v1.PathAttributeTypeOuterClass.PathAttributeType;
import afrl.ntf.common.v1.PathMetricInfoOuterClass.PathMetricInfo;
import afrl.ntf.mms_to_broker.v1.MMSToBrokerMessageOuterClass.MMSToBrokerMessage;
import afrl.ntf.mms_to_broker.v1.PathMetricInfoReplyOuterClass.PathMetricInfoReply;
import afrl.ntf.mms_to_broker.v1.SubscriberInformationMessageOuterClass.SubscriberInformationMessage;

public class ComplexServer {
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private boolean running = false;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private Thread userInputThread;

    public static void main(String[] args) {
        ComplexServer server = new ComplexServer();
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);

            // Start user input thread
            startUserInputThread();

            // Accept client connections
            while (running) {
                try {
                    System.out.println("Waiting for client connection...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    
                    // Create and start a new client handler
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clients.add(handler);
                    new Thread(handler).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        
        // Stop user input thread
        if (userInputThread != null) {
            userInputThread.interrupt();
        }
        
        // Close all client connections
        for (ClientHandler client : clients) {
            client.close();
        }
        clients.clear();
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server stopped");
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    private void startUserInputThread() {
        userInputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            
            printMenu();
            
            while (running) {
                try {
                    if (scanner.hasNextLine()) {
                        String input = scanner.nextLine().trim();
                        
                        switch (input) {
                            case "1":
                                broadcastSubscriberInformationMessage();
                                break;
                            case "2":
                                broadcastPathMetricInfoReply();
                                break;
                            case "3":
                                running = false;
                                break;
                            default:
                                System.out.println("Invalid choice. Please try again.");
                                break;
                        }
                        
                        if (running) {
                            printMenu();
                        }
                    }
                    
                    // Prevent high CPU usage
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("User input thread interrupted");
                    break;
                } catch (Exception e) {
                    System.err.println("Error processing command: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            scanner.close();
        });
        
        userInputThread.setDaemon(true);
        userInputThread.start();
    }

    private void printMenu() {
        System.out.println("\nServer Options:");
        System.out.println("1. Send SubscriberInformationMessage to all clients");
        System.out.println("2. Send PathMetricInfoReply to all clients");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");
    }

    private void broadcastSubscriberInformationMessage() {
        if (clients.isEmpty()) {
            System.out.println("No connected clients.");
            return;
        }
        
        // Create SubscriberInformationMessage
        SubscriberInformationMessage subscriberMsg = createSampleSubscriberInformationMessage();
        
        // Wrap in MMSToBrokerMessage
        MMSToBrokerMessage message = MMSToBrokerMessage.newBuilder()
                .setSubscriberMsg(subscriberMsg)
                .build();
        
        // Send to all clients
        broadcastMessage(message);
        System.out.println("SubscriberInformationMessage sent to all clients.");
    }

    private void broadcastPathMetricInfoReply() {
        if (clients.isEmpty()) {
            System.out.println("No connected clients.");
            return;
        }
        
        // Create PathMetricInfoReply
        PathMetricInfoReply pathReply = createSamplePathMetricInfoReply();
        
        // Wrap in MMSToBrokerMessage
        MMSToBrokerMessage message = MMSToBrokerMessage.newBuilder()
                .setPathReply(pathReply)
                .build();
        
        // Send to all clients
        broadcastMessage(message);
        System.out.println("PathMetricInfoReply sent to all clients.");
    }

    private void broadcastMessage(MMSToBrokerMessage message) {
        for (ClientHandler client : clients) {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                System.err.println("Error sending message to client: " + e.getMessage());
            }
        }
    }

    private SubscriberInformationMessage createSampleSubscriberInformationMessage() {
        return SubscriberInformationMessage.newBuilder()
                .setSubscriberId(1001)
                .setSubscriberName("Example Subscriber")
                .setImsIdentifier("IMS-2023-001")
                .setImsTopic("ntf.data.link16")
                .setPredicate("DATA")
                .build();
    }

    private PathMetricInfoReply createSamplePathMetricInfoReply() {
        // Create PathAttributes
        PathAttribute latency = PathAttribute.newBuilder()
                .setAttributeType(PathAttributeType.PATH_LATENCY_MILLISECS)
                .setValue(120)
                .build();
        
        PathAttribute jitter = PathAttribute.newBuilder()
                .setAttributeType(PathAttributeType.PATH_JITTER_MILLISECS)
                .setValue(15)
                .build();
        
        PathAttribute delivery = PathAttribute.newBuilder()
                .setAttributeType(PathAttributeType.PATH_DELIVERY)
                .setValue(98)
                .build();
        
        // Create PathMetricInfo
        PathMetricInfo pathInfo = PathMetricInfo.newBuilder()
                .setDestination(101)
                .setSourceId(202)
                .setPriority(5)
                .addPathMetrics(latency)
                .addPathMetrics(jitter)
                .addPathMetrics(delivery)
                .build();
        
        // Create PathMetricInfoReply
        return PathMetricInfoReply.newBuilder()
                .setInfo(pathInfo)
                .build();
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private DataInputStream in;
        private DataOutputStream out;
        private boolean running = true;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.err.println("Error creating client handler: " + e.getMessage());
                running = false;
            }
        }

        @Override
        public void run() {
            try {
                while (running) {
                    // Read message length
                    int messageLength = in.readInt();
                    System.out.println("Received message header. Expected payload length: " + messageLength + " bytes");
                    
                    // Read message payload
                    byte[] messageBytes = new byte[messageLength];
                    in.readFully(messageBytes);
                    
                    // Parse the message
                    BrokerToMMSMessage message = BrokerToMMSMessage.parseFrom(messageBytes);
                    
                    // Process the message
                    processMessage(message);
                    
                    // Send success response
                    // sendSuccessResponse();
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            } finally {
                close();
                clients.remove(this);
                System.out.println("Client handler terminated.");
            }
        }

        private void processMessage(BrokerToMMSMessage message) {
            System.out.println("\n==== Received Message ====");
            
            if (message.hasBrokerMsg()) {
                // Handle BrokerServiceMessage
                BrokerServiceMessage brokerMsg = message.getBrokerMsg();
                System.out.println("BrokerServiceMessage:");
                System.out.println("  Broker ID: " + brokerMsg.getBrokerId());
                System.out.println("  Broker Name: " + brokerMsg.getBrokerName());
                System.out.println("  Local Domain: " + formatSecurityDomain(brokerMsg.getLocalDomain()));
                System.out.println("  Reachable Domains Count: " + brokerMsg.getReachableDomainsCount());
                System.out.println(brokerMsg);
            } else if (message.hasPathRequest()) {
                // Handle PathMetricInfoRequest
                PathMetricInfoRequest pathRequest = message.getPathRequest();
                System.out.println("PathMetricInfoRequest:");
                System.out.println("  Destination ID: " + pathRequest.getDestinationId());
                System.out.println("  Source ID: " + pathRequest.getSourceId());
                System.out.println("  Attributes:");
                
                for (PathAttribute attr : pathRequest.getAttributesList()) {
                    System.out.println("    Type: " + attr.getAttributeType() + ", Value: " + attr.getValue());
                }
            } else {
                System.out.println("Unknown message type");
            }
            
            System.out.println("=========================\n");
        }

        private String formatSecurityDomain(afrl.ntf.common.v1.SecurityDomainOuterClass.SecurityDomain domain) {
            return domain.getClassificationLevel() + "/" + domain.getSensitivityLevel();
        }

        private void sendSuccessResponse() throws IOException {
            // Send a simple success response (just a 1-byte payload indicating success)
            out.writeInt(1);
            out.write(new byte[] { 1 }); // 1 = success
            out.flush();
        }

        public void sendMessage(MMSToBrokerMessage message) throws IOException {
            if (!running) return;
            
            try {
                // Convert message to bytes
                byte[] messageBytes = message.toByteArray();
                
                // Send message length header
                out.writeInt(messageBytes.length);
                
                // Send message payload
                out.write(messageBytes);
                out.flush();
            } catch (IOException e) {
                running = false;
                throw e;
            }
        }

        public void close() {
            running = false;
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client handler: " + e.getMessage());
            }
        }
    }
}