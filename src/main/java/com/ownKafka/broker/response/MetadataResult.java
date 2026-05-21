package com.ownKafka.broker.response;

import com.ownKafka.broker.BrokerInfo;
import com.ownKafka.broker.TopicMetadata;

import java.util.List;

public class MetadataResult {
    private final List<BrokerInfo> brokers;
    private final List<TopicMetadata> topics;
    private final String error;

    public MetadataResult(List<BrokerInfo> brokers, List<TopicMetadata> topics, String error) {
        this.brokers = brokers;
        this.topics = topics;
        this.error = error;
    }

    public List<BrokerInfo> getBrokers() {
        return brokers;
    }

    public List<TopicMetadata> getTopics() {
        return topics;
    }

    public String getError() {
        return error;
    }
}
