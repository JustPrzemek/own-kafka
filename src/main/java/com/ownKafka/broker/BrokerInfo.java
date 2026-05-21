package com.ownKafka.broker;

public class BrokerInfo {
    private final int id;
    private final String host;
    private final int port;

    public BrokerInfo(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "BrokerInfo{id=" + id + ", host=" + host + ", port=" + port + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrokerInfo brokerInfo = (BrokerInfo) o;
        return id == brokerInfo.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
