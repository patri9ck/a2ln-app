package dev.patri9ck.a2ln.address;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Device {

    private static final Gson GSON = new Gson();

    private static final Type DEVICES_TYPE = new TypeToken<ArrayList<Device>>() {}.getType();

    private String serverIp;
    private int port;
    private String serverPublicKey;

    public Device() {}

    public Device(String serverIp, int port, String serverPublicKey) {
        this.serverIp = serverIp;
        this.port = port;
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

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return serverIp + ":" + port;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }
}
