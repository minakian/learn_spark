package com.example.client.message;

import afrl.ntf.mms_to_broker.v1.MMSToBrokerMessageOuterClass.MMSToBrokerMessage;
import afrl.ntf.mms_to_broker.v1.SubscriberInformationMessageOuterClass.SubscriberInformationMessage;
import afrl.ntf.mms_to_broker.v1.PathMetricInfoReplyOuterClass.PathMetricInfoReply;
import afrl.ntf.common.v1.PathMetricInfoOuterClass.PathMetricInfo;
import afrl.ntf.common.v1.PathAttributeOuterClass.PathAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.client.http.HttpClientService;
import com.example.client.model.SubscriptionModel;

public class MessageHandler {

    /**
     * Processes the incoming MMSToBrokerMessage and dispatches based on its type.
     */
    public void handleServerMessage(MMSToBrokerMessage message) {
        System.out.println("\n==== Received Message from Server ====");

        if (message.hasSubscriberMsg()) {
            SubscriberInformationMessage subMsg = message.getSubscriberMsg();
            System.out.println("Subscriber Information Message:");
            System.out.println("  Subscriber ID: " + subMsg.getSubscriberId());
            System.out.println("  Subscriber Name: " + subMsg.getSubscriberName());
            System.out.println("  IMS Identifier: " + subMsg.getImsIdentifier());
            System.out.println("  IMS Topic: " + subMsg.getImsTopic());
            System.out.println("  Predicate: " + subMsg.getPredicate());
            handleSubscriptionMessage(subMsg);
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

    private void handleSubscriptionMessage(SubscriberInformationMessage subMsg) {
        try {
            SubscriptionModel sub = new SubscriptionModel(subMsg.getImsTopic(), subMsg.getPredicate());
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(sub);
            System.out.println("Subscription JSON: " + json);
            HttpClientService.sendPostRequest("http://127.0.0.1:4567/api/v1/subscribe", json);
        } catch (Exception e) {
            System.out.println("Error converting subscription message to JSON: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("Client Options:");
        System.out.println("1. Send BrokerServiceMessage");
        System.out.println("2. Send PathMetricInfoRequest");
        System.out.println("3. Exit");
        System.out.println("4. Send HTTP GET Request");
        System.out.println("5. Send HTTP POST Request");
        System.out.print("Enter choice: ");
    }
}
