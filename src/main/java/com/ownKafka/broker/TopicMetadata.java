package com.ownKafka.broker;

import java.util.List;

public class TopicMetadata {
    private final String topicName;
    private final List<PartitionMetadata> partitions;

    public TopicMetadata(String topicName, List<PartitionMetadata> partitions) {
        this.topicName = topicName;
        this.partitions = partitions;
    }

    public String getTopicName() {
        return topicName;
    }

    public List<PartitionMetadata> getPartitions() {
        return partitions;
    }
}
