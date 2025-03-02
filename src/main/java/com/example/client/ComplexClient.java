package com.example.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
// import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.protobuf.Duration;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import afrl.ntf.common.v1.SimulationMessageIdOuterClass.SimulationMessageId;
import afrl.ntf.mms_to_broker.v1.MMSToBrokerMessageOuterClass.MMSToBrokerMessage;
import afrl.ntf.mms_to_broker.v1.PathMetricInfoReplyOuterClass.PathMetricInfoReply;
import afrl.ntf.common.v1.PathMetricInfoOuterClass.PathMetricInfo;
import afrl.ntf.mms_to_broker.v1.SubscriberInformationMessageOuterClass.SubscriberInformationMessage;

import com.example.client.model.*;
import com.example.client.store.DataStore;
import com.example.client.store.ConfigStore;


public class ComplexClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final int IMSA_PORT = 6000;
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

        AppSettings settings = ConfigStore.getInstance().getSettings();
        System.out.println(settings);

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
        String[] ta2Endpoint = ConfigStore.getInstance().getSettings().getTa2Endpoint().split(":");
        
        socket = new Socket(ta2Endpoint[0], Integer.parseInt(ta2Endpoint[1]));
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
                        
                        // TODO: Add try for different message types
                        // Parse message
                        try {
                            MMSToBrokerMessage message = MMSToBrokerMessage.parseFrom(messageBytes);
                            
                            // Handle the message
                            handleServerMessage(message);
                        } catch (InvalidProtocolBufferException e) {
                            System.out.println("Not a valid protobuf message");
                        }
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

            // TODO: Send topic to IMSL
            try {
                SubscriptionModel sub = new SubscriptionModel(subMsg.getImsTopic(), subMsg.getPredicate());
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(sub);
                System.out.println(jsonString);
                sendPostRequest("http://127.0.0.1:4567/api/v1/subscribe", jsonString); // TODO: Remove hard code
            } catch (Exception e) {
                System.out.println("Invalid proto to json conversion.");
            }


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
            // Do nothing with this message we dont need it

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
                if(DataStore.getLatestCapabilitiesMessage() == null){
                    // TODO: Send request to CDS Adapter
                }
                if (DataStore.getUpdated()){
                    // System.out.println(DataStore.getLatestCapabilitiesMessage());
                    sendBrokerServiceMessage();
                    DataStore.setUpdated(false);
                }
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
        BrokerServiceMessage brokerMsg = createBrokerServiceMessage();
        if (brokerMsg == null) {
            return;
        }
        
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

    private ClassificationLevel parseClassificationLevel(String classificationLevel) {
        switch (classificationLevel) {
            case "TS//SCI":
                return ClassificationLevel.CLASSIFICATION_LEVEL_TS_SCI;
            case "TS":
                return ClassificationLevel.CLASSIFICATION_LEVEL_TOP_SECRET;
            case "SECRET":
                return ClassificationLevel.CLASSIFICATION_LEVEL_SECRET;
            case "CS":
                return ClassificationLevel.CLASSIFICATION_LEVEL_COALITION_SECRET;
            case "CUC":
                return ClassificationLevel.CLASSIFICATION_LEVEL_COMMERCIAL_UNCLASSIFIED;
            default:
                return ClassificationLevel.CLASSIFICATION_LEVEL_UNDEFINED;
        }
    }

    private List<MessageService> parseMessageServices (List<CapabilityDetail> services) {
        List<MessageService> messageServices = new ArrayList<>();
        for(CapabilityDetail capabilityDetail : services){
            String messageType = capabilityDetail.getMessageType();
            int latency = capabilityDetail.getLatency();
            
            // TODO: Parse message types into corresponding fields

            messageServices.add(
                MessageService.newBuilder()
                    .setMessageFormat(
                        MessageFormat.newBuilder()
                            .setDataType(DataType.DATA_TYPE_SIMULATION) // TODO: Should be parsed from capabilityDetail
                            .setSimId(SimulationMessageId.UNDEFINED) // TODO: This needs to be parsed and determined from capabilities
                            .build()
                    )
                    .setAverageBrokerLatency(
                        Duration.newBuilder()
                            .setSeconds(latency/1000)
                            .setNanos((latency%1000)*1000000) // Convert to nanoseconds (1ms = 1,000,000ns) Should move any ms over 1000 to seconds
                            .build()
                    )
                    .build()
            );
        }
        return messageServices;
    }

    private BrokerServiceMessage createBrokerServiceMessage() {
        CapabilitiesMessage messages = DataStore.getLatestCapabilitiesMessage();
        if (messages == null) {
            return null;
        }
        String localDonain = messages.getPayload().getCapabilities().getLocalDomain();
        String localClass = messages.getPayload().getCapabilities().getLocalClassification();
        ClassificationLevel classificationLevel = parseClassificationLevel(localClass);
        
        /*
         * repeated DomainService reachable_domains = 2;
         *      SecurityDomain
         *      to
         *      from
         */
        // Gather Domain Services
        List <DomainService> domainServices = new ArrayList<>();
        for(Domain domain : messages.getPayload().getCapabilities().getReachableDomains()){
            String domainDesignator = domain.getDomain();
            String domainClass = domain.getClassification();
            
            // Gather TO
            List<MessageService> messageServices = parseMessageServices(domain.getTo());
            // Build TO
            DirectionalServices dsTo = DirectionalServices.newBuilder().addAllMessageServices(messageServices).build();

            // Clear the list
            messageServices.clear();
            // Gather FROM
            messageServices = parseMessageServices(domain.getFrom());
            //Build FROM
            DirectionalServices dsFrom = DirectionalServices.newBuilder().addAllMessageServices(messageServices).build();

            // Add to domain services
            domainServices.add(
                DomainService.newBuilder()
                    .setSecurityDomain(SecurityDomain.newBuilder()
                        .setClassificationLevel(parseClassificationLevel(domainClass))
                        .setSensitivityLevel(SensitivityLevel.SENSITIVITY_LEVEL_UNDEFINED)
                        .build())
                    .setTo(dsTo)
                    .setFrom(dsFrom)
                    .build()
            );

        }

        // Create a SecurityDomain
        SecurityDomain securityDomain = SecurityDomain.newBuilder()
                .setClassificationLevel(classificationLevel)
                .setSensitivityLevel(SensitivityLevel.SENSITIVITY_LEVEL_UNDEFINED)
                .build();

        // Build DomainServices
        return BrokerServiceMessage.newBuilder()
                .setBrokerId(ConfigStore.getInstance().getSettings().getName().hashCode())
                .setBrokerName(ConfigStore.getInstance().getSettings().getName())
                .setLocalDomain(securityDomain)
                .addAllReachableDomains(domainServices)
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