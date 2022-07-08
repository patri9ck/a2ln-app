package dev.patri9ck.a2ln.device;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Device {

    private static final Gson GSON = new Gson();

    private static final Type DEVICES_TYPE = new TypeToken<ArrayList<Device>>() {}.getType();

    private String serverIp;
    private int serverPort;
    private String serverPublicKey;

    public Device() {}

    public Device(String serverIp, int serverPort, String serverPublicKey) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.serverPublicKey = serverPublicKey;
    }

    public static String toJson(List<Device> devices) {
        return GSON.toJson(devices);
    }

    public static List<Device> fromJson(String json) {
        if (json == null) {
            return new ArrayList<>();
        }

        return GSON.fromJson(json, DEVICES_TYPE);
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(String serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    public String getAddress() {
        return serverIp + ":" + serverPort;
    }
}
