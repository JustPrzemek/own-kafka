package com.ownKafka.broker;

import com.ownKafka.broker.response.FetchResult;
import com.ownKafka.broker.response.MetadataResult;
import com.ownKafka.broker.response.ProduceResult;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
    public static final byte PRODUCE = 0x01;
    public static final byte FETCH = 0x02;
    public static final byte METADATA = 0x03;
    public static final byte CREATE_TOPIC = 0x04;

    public static final byte PRODUCE_RESPONSE = 0x11;
    public static final byte FETCH_RESPONSE = 0x12;
    public static final byte METADATA_REQUEST = 0x03;
    public static final byte METADATA_RESPONSE = 0x13;


    public static ByteBuffer encodeProduceRequest(String topic, int partition, byte[] message) {
        byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);

        int capacity = 1 + 2 + topicBytes.length + 4 + 4 + message.length;

        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        buffer.put(PRODUCE);
        buffer.putShort((short) topicBytes.length);
        buffer.put(topicBytes);
        buffer.putInt(partition);
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();

        return buffer;
    }

    public static ByteBuffer encodeFetchRequest(String topic, int partition, long offset, int maxBytes) {
        byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);

        int capacity = 1 + 2 + topicBytes.length + 4 + 8 + 4;

        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        buffer.put(FETCH);
        buffer.putShort((short) topicBytes.length);
        buffer.put(topicBytes);
        buffer.putInt(partition);
        buffer.putLong(offset);
        buffer.putInt(maxBytes);
        buffer.flip();

        return buffer;
    }

    public static ByteBuffer encodeMetadataRequest() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(METADATA);
        buffer.flip();
        return buffer;
    }

    public static ByteBuffer encodeCreateTopicRequest(String topic, int numPartitions, short replicationFactor) {
        byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);

        int capacity = 1 + 2 + topicBytes.length + 4 + 2;

        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(CREATE_TOPIC);
        buffer.putShort((short) topicBytes.length);
        buffer.put(topicBytes);
        buffer.putInt(numPartitions);
        buffer.putShort(replicationFactor);
        buffer.flip();
        return buffer;
    }

    public static ProduceResult decodeProduceResponse(ByteBuffer buffer) {
        byte byteResponse = buffer.get();

        if (byteResponse != PRODUCE_RESPONSE) {
            return new ProduceResult(-1, "Wrong response type!!");
        }

        long offset = buffer.getLong();

        short errorLength = buffer.getShort();
        String errorInformation = null;

        if (errorLength > 0) {
            byte[] errorBytes = new byte[errorLength];
            buffer.get(errorBytes);
            errorInformation = new String(errorBytes, StandardCharsets.UTF_8);
        }

        return new ProduceResult(offset, errorInformation);
    }

    public static FetchResult decodeFetchResponse(ByteBuffer buffer) {
        byte byteResponse = buffer.get();

        if (byteResponse != FETCH_RESPONSE) {
            return new FetchResult(new ArrayList<>(), "Wrong response type!!");
        }

        short errorLength = buffer.getShort();
        String errorInformation = null;

        if (errorLength > 0) {
            byte[] errorBytes = new byte[errorLength];
            buffer.get(errorBytes);
            errorInformation = new String(errorBytes, StandardCharsets.UTF_8);
        }

        List<byte[]> messages = new ArrayList<>();
        int size = buffer.getInt();

        for (int i = 0; i < size; i++) {
            long offset = buffer.getLong();
            int messageSize = buffer.getInt();

            byte[] message = new byte[messageSize];
            buffer.get(message);

            messages.add(message);
        }

        return new FetchResult(messages, errorInformation);
    }

    public static MetadataResult decodeMetadataResponse(ByteBuffer buffer) {
        byte responseType = buffer.get();
        if (responseType != METADATA_RESPONSE) {
            return new MetadataResult(new ArrayList<>(), new ArrayList<>(), "Wrong response type xP");
        }

        short errorLength = buffer.getShort();
        String errorInformation = null;

        if (errorLength > 0) {
            byte[] errorBytes = new byte[errorLength];
            buffer.get(errorBytes);
            errorInformation = new String(errorBytes, StandardCharsets.UTF_8);
        }

        int brokerCount = buffer.getInt();
        List<BrokerInfo> brokers = new ArrayList<>();

        for (int i = 0; i < brokerCount; i++) {
            int id = buffer.getInt();

            short hostLength = buffer.getShort();
            byte[] hostByte = new byte[hostLength];
            buffer.get(hostByte);
            String host = new String(hostByte, StandardCharsets.UTF_8);

            int port = buffer.getInt();

            brokers.add(new BrokerInfo(id, host, port));
        }

        int topicCount = buffer.getInt();
        List<TopicMetadata> topicMetadata= new ArrayList<>();

        for (int i = 0; i < topicCount; i++) {
            short topicNameLength = buffer.getShort();
            byte[] topicNameByte = new byte[topicNameLength];
            buffer.get(topicNameByte);
            String topicName = new String(topicNameByte, StandardCharsets.UTF_8);

            List<PartitionMetadata> partitionData = new ArrayList<>();
            int partitionLength = buffer.getInt();

            for (int j = 0; j < partitionLength; j++) {
                int id = buffer.getInt();
                int leaderId = buffer.getInt();
                int replicasLength = buffer.getInt();
                List<Integer> replicas = new ArrayList<>();

                for (int k = 0; k < replicasLength; k++) {

                    replicas.add(buffer.getInt());
                }

                partitionData.add(new PartitionMetadata(id, leaderId, replicas));
            }

            topicMetadata.add(new TopicMetadata(topicName, partitionData));
        }


        return new MetadataResult(brokers, topicMetadata, errorInformation);
    }
}
