package com.example.client.message;

import java.util.ArrayList;
import java.util.List;

import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.BrokerServiceMessage;
import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.DomainService;
import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.DirectionalServices;
import afrl.ntf.broker_to_mms.v1.BrokerServiceMessageOuterClass.MessageService;
import afrl.ntf.broker_to_mms.v1.BrokerToMMSMessageOuterClass.BrokerToMMSMessage;
import afrl.ntf.broker_to_mms.v1.PathMetricInfoRequestOuterClass.PathMetricInfoRequest;
import afrl.ntf.common.v1.ClassificationLevelOuterClass.ClassificationLevel;
import afrl.ntf.common.v1.DataTypeOuterClass.DataType;
import afrl.ntf.common.v1.MessageFormatOuterClass.MessageFormat;
import afrl.ntf.common.v1.PathAttributeOuterClass.PathAttribute;
import afrl.ntf.common.v1.PathAttributeTypeOuterClass.PathAttributeType;
import afrl.ntf.common.v1.SecurityDomainOuterClass.SecurityDomain;
import afrl.ntf.common.v1.SensitivityLevelOuterClass.SensitivityLevel;
import afrl.ntf.common.v1.SimulationMessageIdOuterClass.SimulationMessageId;
import com.example.client.model.CapabilityDetail;
import com.example.client.model.CapabilitiesMessage;
import com.example.client.model.Domain;
import com.example.client.store.ConfigStore;
import com.example.client.store.DataStore;

public class MessageBuilder {

    /**
     * Builds a BrokerToMMSMessage containing a BrokerServiceMessage based on the latest capabilities.
     */
    public static BrokerToMMSMessage buildBrokerServiceMessage() {
        CapabilitiesMessage messages = DataStore.getLatestCapabilitiesMessage();
        if (messages == null) {
            return null;
        }
        String localDomain = messages.getPayload().getCapabilities().getLocalDomain();
        String localClassification = messages.getPayload().getCapabilities().getLocalClassification();
        ClassificationLevel classificationLevel = parseClassificationLevel(localClassification);
        List<DomainService> domainServices = new ArrayList<>();

        for (Domain domain : messages.getPayload().getCapabilities().getReachableDomains()) {
            List<MessageService> toServices = parseMessageServices(domain.getTo());
            DirectionalServices dsTo = DirectionalServices.newBuilder().addAllMessageServices(toServices).build();
            List<MessageService> fromServices = parseMessageServices(domain.getFrom());
            DirectionalServices dsFrom = DirectionalServices.newBuilder().addAllMessageServices(fromServices).build();

            DomainService domainService = DomainService.newBuilder()
                    .setSecurityDomain(SecurityDomain.newBuilder()
                            .setClassificationLevel(parseClassificationLevel(domain.getClassification()))
                            .setSensitivityLevel(SensitivityLevel.SENSITIVITY_LEVEL_UNDEFINED)
                            .build())
                    .setTo(dsTo)
                    .setFrom(dsFrom)
                    .build();
            domainServices.add(domainService);
        }

        SecurityDomain securityDomain = SecurityDomain.newBuilder()
                .setClassificationLevel(classificationLevel)
                .setSensitivityLevel(SensitivityLevel.SENSITIVITY_LEVEL_UNDEFINED)
                .build();

        BrokerServiceMessage brokerMsg = BrokerServiceMessage.newBuilder()
                .setBrokerId(ConfigStore.getInstance().getSettings().getName().hashCode())
                .setBrokerName(ConfigStore.getInstance().getSettings().getName())
                .setLocalDomain(securityDomain)
                .addAllReachableDomains(domainServices)
                .build();

        return BrokerToMMSMessage.newBuilder().setBrokerMsg(brokerMsg).build();
    }

    /**
     * Builds a sample PathMetricInfoRequest.
     */
    public static PathMetricInfoRequest buildPathMetricInfoRequest() {
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
        return PathMetricInfoRequest.newBuilder()
                .setDestinationId(101)
                .setSourceId(202)
                .addAttributes(latency)
                .addAttributes(capacity)
                .addAttributes(quality)
                .build();
    }

    /**
     * Converts a classification level string to its corresponding enum.
     */
    public static ClassificationLevel parseClassificationLevel(String level) {
        switch (level) {
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

    /**
     * Converts a list of CapabilityDetail objects into a list of MessageService objects.
     */
    public static List<MessageService> parseMessageServices(List<CapabilityDetail> services) {
        List<MessageService> messageServices = new ArrayList<>();
        for (CapabilityDetail capabilityDetail : services) {
            int latency = capabilityDetail.getLatency();
            MessageService service = MessageService.newBuilder()
                    .setMessageFormat(MessageFormat.newBuilder()
                            .setDataType(DataType.DATA_TYPE_SIMULATION)
                            .setSimId(SimulationMessageId.UNDEFINED)
                            .build())
                    .setAverageBrokerLatency(com.google.protobuf.Duration.newBuilder()
                            .setSeconds(latency / 1000)
                            .setNanos((latency % 1000) * 1_000_000)
                            .build())
                    .build();
            messageServices.add(service);
        }
        return messageServices;
    }
}
