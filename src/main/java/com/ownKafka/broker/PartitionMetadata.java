package com.ownKafka.broker;

import java.util.List;

public class PartitionMetadata {
    private final int partitionId;
    private final int leaderId;
    private final List<Integer> replicas;

    public PartitionMetadata (int partitionId, int leaderId, List<Integer> replicas) {
        this.partitionId = partitionId;
        this.leaderId = leaderId;
        this.replicas = replicas;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public List<Integer> getReplicas() {
        return replicas;
    }
}
