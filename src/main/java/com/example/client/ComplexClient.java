package com.example.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
// import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.protobuf.Duration;

import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.BrokerServiceMessage;
import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.DomainService;
import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.DirectionalServices;
import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.MessageService;
import afrl.ntf.broker_to_mms.v1.BrokerToMMSMessageOuterClass.BrokerToMMSMessage;
import afrl.ntf.broker_to_mms.v1.PathMetricInfoRequestOuterClass.PathMetricInfoRequest;
import afrl.ntf.common.v1.ClassificationLevelOuterClass.ClassificationLevel;
import afrl.ntf.common.v1.DataTypeOuterClass.DataType;
import afrl.ntf.common.v1.Link16MessageIdOuterClass.Link16MessageId;
import afrl.ntf.common.v1.MessageFormatOuterClass.MessageFormat;
import afrl.ntf.common.v1.PathAttributeOuterClass.PathAttribute;
import afrl.ntf.common.v1.PathAttributeTypeOuterClass.PathAttributeType;
import afrl.ntf.common.v1.SecurityDomainOuterClass.SecurityDomain;
import afrl.ntf.common.v1.SensitivityLevelOuterClass.SensitivityLevel;
import afrl.ntf.mms_to_broker.v1.MMSToBrokerMessageOuterClass.MMSToBrokerMessage;
import afrl.ntf.mms_to_broker.v1.PathMetricInfoReplyOuterClass.PathMetricInfoReply;
import afrl.ntf.common.v1.PathMetricInfoOuterClass.PathMetricInfo;
import afrl.ntf.mms_to_broker.v1.SubscriberInformationMessageOuterClass.SubscriberInformationMessage;

import com.example.client.SparkRestServer;

public class ComplexClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean connected = false;
    private Thread receiverThread;

    public static void main(String[] args) {
        ComplexClient client = new ComplexClient();
        client.start();
    }

    public void start() {
        new Thread(() -> {
            SparkRestServer restServer = new SparkRestServer(4567);
            restServer.startServer();
        }).start();

        try {
            // Connect to the server
            connect();

            // Start receiver thread
            startReceiverThread();

            // Main user interaction loop
            userInteractionLoop();

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        connected = true;
        System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
    }

    private void disconnect() {
        connected = false;
        
        // Stop receiver thread
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            try {
                while (connected) {
                    // Check if there's data available
                    if (in.available() > 0) {
                        // Read message length
                        int messageLength = in.readInt();
                        
                        // Read message bytes
                        byte[] messageBytes = new byte[messageLength];
                        in.readFully(messageBytes);
                        
                        // Parse message
                        MMSToBrokerMessage message = MMSToBrokerMessage.parseFrom(messageBytes);
                        
                        // Handle the message
                        handleServerMessage(message);
                    }
                    
                    // Sleep to prevent high CPU usage
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                System.out.println("Receiver thread interrupted");
            } catch (IOException e) {
                if (connected) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
            }
        });
        
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private void handleServerMessage(MMSToBrokerMessage message) {
        System.out.println("\n==== Received Message from Server ====");
        
        if (message.hasSubscriberMsg()) {
            SubscriberInformationMessage subMsg = message.getSubscriberMsg();
            System.out.println("Subscriber Information Message:");
            System.out.println("  Subscriber ID: " + subMsg.getSubscriberId());
            System.out.println("  Subscriber Name: " + subMsg.getSubscriberName());
            System.out.println("  IMS Identifier: " + subMsg.getImsIdentifier());
            System.out.println("  IMS Topic: " + subMsg.getImsTopic());
            System.out.println("  Predicate: " + subMsg.getPredicate());
        } else if (message.hasPathReply()) {
            PathMetricInfoReply reply = message.getPathReply();
            PathMetricInfo info = reply.getInfo();
            
            System.out.println("Path Metric Info Reply:");
            System.out.println("  Destination: " + info.getDestination());
            System.out.println("  Source ID: " + info.getSourceId());
            System.out.println("  Priority: " + info.getPriority());
            System.out.println("  Path Metrics:");
            
            for (PathAttribute attr : info.getPathMetricsList()) {
                System.out.println("    Type: " + attr.getAttributeType() + ", Value: " + attr.getValue());
            }
        } else {
            System.out.println("Unknown message type");
        }
        
        System.out.println("======================================\n");
        printMenu();
    }

    private void userInteractionLoop() throws IOException {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        printMenu();
        
        while (running && connected) {
            try {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    
                    switch (input) {
                        case "1":
                            sendBrokerServiceMessage();
                            break;
                        case "2":
                            sendPathMetricInfoRequest();
                            break;
                        case "3":
                            running = false;
                            break;
                        case "4":
                            System.out.print("Enter GET endpoint URL: ");
                            String getEndpoint = scanner.nextLine().trim();
                            sendGetRequest(getEndpoint);
                            break;
                        case "5":
                            System.out.print("Enter POST endpoint URL: ");
                            String postEndpoint = scanner.nextLine().trim();
                            System.out.print("Enter JSON payload: ");
                            String jsonPayload = scanner.nextLine().trim();
                            sendPostRequest(postEndpoint, jsonPayload);
                            break;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                            break;
                    }
                    
                    if (running) {
                        printMenu();
                    }
                } else {
                    // If no input is available, wait a bit before re-checking
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                System.err.println("Error processing command: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        scanner.close();
    }

    private void printMenu() {
        System.out.println("\nClient Options:");
        System.out.println("1. Send BrokerServiceMessage");
        System.out.println("2. Send PathMetricInfoRequest");
        System.out.println("3. Exit");
        System.out.println("4. Send HTTP GET Request");
        System.out.println("5. Send HTTP POST Request");
        System.out.print("Enter choice: ");
    }

    private void sendBrokerServiceMessage() throws IOException {
        // Create a BrokerServiceMessage
        BrokerServiceMessage brokerMsg = createSampleBrokerServiceMessage();
        
        // Wrap in BrokerToMMSMessage
        BrokerToMMSMessage message = BrokerToMMSMessage.newBuilder()
                .setBrokerMsg(brokerMsg)
                .build();
        
        sendMessage(message);
        System.out.println("BrokerServiceMessage sent.");
    }

    private void sendPathMetricInfoRequest() throws IOException {
        // Create a PathMetricInfoRequest
        PathMetricInfoRequest pathRequest = createSamplePathMetricInfoRequest();
        
        // Wrap in BrokerToMMSMessage
        BrokerToMMSMessage message = BrokerToMMSMessage.newBuilder()
                .setPathRequest(pathRequest)
                .build();
        
        sendMessage(message);
        System.out.println("PathMetricInfoRequest sent.");
    }

    private void sendMessage(BrokerToMMSMessage message) throws IOException {
        // Convert message to bytes
        byte[] messageBytes = message.toByteArray();
        
        // Send message length header
        out.writeInt(messageBytes.length);
        
        // Send message payload
        out.write(messageBytes);
        out.flush();
    }

    private void sendGetRequest(String endpoint) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .GET()
                    .build();
    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET response: " + response.body());
        } catch (Exception e) {
            System.err.println("GET request error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendPostRequest(String endpoint, String jsonPayload) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST response: " + response.body());
        } catch (Exception e) {
            System.err.println("POST request error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BrokerServiceMessage createSampleBrokerServiceMessage() {
        // Create a SecurityDomain
        SecurityDomain securityDomain = SecurityDomain.newBuilder()
                .setClassificationLevel(ClassificationLevel.CLASSIFICATION_LEVEL_SECRET)
                .setSensitivityLevel(SensitivityLevel.SENSITIVITY_LEVEL_REL_TO)
                .build();
        
        // Create a MessageFormat
        MessageFormat messageFormat = MessageFormat.newBuilder()
                .setDataType(DataType.DATA_TYPE_LINK16)
                .setLink16Id(Link16MessageId.J2_DOT0)
                .build();
        
        // Create a MessageService with protobuf Duration
        Duration.Builder durationBuilder = Duration.newBuilder();
        durationBuilder.setSeconds(0);
        durationBuilder.setNanos(500000000); // 500ms
        
        MessageService messageService = MessageService.newBuilder()
                .setMessageFormat(messageFormat)
                .setAverageBrokerLatency(durationBuilder.build())
                .build();
        
        // Create DirectionalServices
        DirectionalServices directionalServices = DirectionalServices.newBuilder()
                .addMessageServices(messageService)
                .build();
        
        // Create DomainService
        DomainService domainService = DomainService.newBuilder()
                .setSecurityDomain(securityDomain)
                .setTo(directionalServices)
                .setFrom(directionalServices)
                .build();
        
        // Create BrokerServiceMessage
        return BrokerServiceMessage.newBuilder()
                .setBrokerId(42)
                .setBrokerName("Sample Broker")
                .setLocalDomain(securityDomain)
                .addReachableDomains(domainService)
                .build();
    }

    private PathMetricInfoRequest createSamplePathMetricInfoRequest() {
        // Create some PathAttributes
        PathAttribute latency = PathAttribute.newBuilder()
                .setAttributeType(PathAttributeType.PATH_LATENCY_MILLISECS)
                .setValue(150)
                .build();
        
        PathAttribute capacity = PathAttribute.newBuilder()
                .setAttributeType(PathAttributeType.PATH_CAPACITY_RECV_KBITS_PS)
                .setValue(1000)
                .build();
        
        PathAttribute quality = PathAttribute.newBuilder()
                .setAttributeType(PathAttributeType.PATH_QUALITY)
                .setValue(90)
                .build();
        
        // Create PathMetricInfoRequest
        return PathMetricInfoRequest.newBuilder()
                .setDestinationId(101)
                .setSourceId(202)
                .addAttributes(latency)
                .addAttributes(capacity)
                .addAttributes(quality)
                .build();
    }
}