package com.ownKafka.broker.response;

import java.util.List;

public class FetchResult {
    private final List<byte[]> messages;
    private final String information;

    public FetchResult(List<byte[]> messages, String information) {
        this.messages = messages;
        this.information = information;
    }

    public List<byte[]> getMessages() {
        return messages;
    }

    public String getInformation() {
        return information;
    }
}
