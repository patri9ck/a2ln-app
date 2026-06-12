package dev.patri9ck.a2ln.address;

public class Address {

    private String host;
    private int port;

    public Address() {}

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
