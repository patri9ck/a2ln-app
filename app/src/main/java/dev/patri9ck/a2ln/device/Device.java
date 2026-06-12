package dev.patri9ck.a2ln.device;

public class Device {

    private String ip;
    private int port;
    private byte[] publicKey;

    public Device() {
        // Gson
    }

    public Device(String ip, int port, byte[] publicKey) {
        this.ip = ip;
        this.port = port;
        this.publicKey = publicKey;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getAddress() {
        return ip + ":" + port;
    }
}
