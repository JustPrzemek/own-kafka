package com.ownKafka.broker;

import com.ownKafka.broker.response.ProduceResult;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Protocol {
    public static final byte PRODUCE = 0x01;
    public static final byte FETCH = 0x02;
    public static final byte METADATA = 0x03;
    public static final byte CREATE_TOPIC = 0x04;

    public static final byte PRODUCE_RESPONSE = 0x11;
    public static final byte FETCH_RESPONSE = 0x12;


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
}
