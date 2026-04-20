package com.ownKafka.broker.response;

public class ProduceResult {
    private final long offset;
    private final String errorInformation;

    public ProduceResult(long offset, String errorInformation) {
        this.offset = offset;
        this.errorInformation = errorInformation;
    }

    public long getOffset() {
        return offset;
    }

    public String getErrorInformation() {
        return errorInformation;
    }
}
