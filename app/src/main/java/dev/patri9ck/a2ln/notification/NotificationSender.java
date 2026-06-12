package dev.patri9ck.a2ln.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.device.Device;
import dev.patri9ck.a2ln.util.JsonListConverter;
import zmq.util.Z85;

public class NotificationSender {

    private static final String TAG = "A2LNNS";

    private static final int TIMEOUT_SECONDS = 5;

    byte[] clientPublicKey;
    byte[] clientSecretKey;

    private List<Device> devices;

    public NotificationSender(List<Device> devices, byte[] clientPublicKey, byte[] clientSecretKey) {
        this.devices = devices;

        this.clientPublicKey = clientPublicKey;
        this.clientSecretKey = clientSecretKey;
    }

    public static NotificationSender fromSharedPreferences(Context context, SharedPreferences sharedPreferences) {
        String clientPublicKey = sharedPreferences.getString(context.getString(R.string.preferences_client_public_key), null);
        String clientSecretKey = sharedPreferences.getString(context.getString(R.string.preferences_client_secret_key), null);

        if (clientPublicKey == null || clientSecretKey == null) {
            Log.e(TAG, "Client keys not saved in preferences properly");

            return null;
        }

        return new NotificationSender(JsonListConverter.fromJson(sharedPreferences.getString(context.getString(R.string.preferences_devices), null), Device.class),
                Z85.decode(clientPublicKey),
                Z85.decode(clientSecretKey));
    }

    public synchronized void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public synchronized void sendParsedNotification(ParsedNotification parsedNotification) {
        if (devices.isEmpty()) {
            Log.v(TAG, "No devices given, will not start clients");

            return;
        }

        ZMsg zMsg = new ZMsg();

        zMsg.add(parsedNotification.getTitle());
        zMsg.add(parsedNotification.getText());

        byte[] icon = parsedNotification.getIcon();

        if (icon != null) {
            zMsg.add(icon);
        }

        try (ZContext zContext = new ZContext()) {
            CountDownLatch countDownLatch = new CountDownLatch(devices.size());

            devices.forEach(device -> CompletableFuture.runAsync(() -> {
                try (ZMQ.Socket client = zContext.createSocket(SocketType.PUSH)) {
                    client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
                    client.setImmediate(false);
                    client.setCurvePublicKey(clientPublicKey);
                    client.setCurveSecretKey(clientSecretKey);
                    client.setCurveServerKey(device.getPublicKey());
                    client.connect("tcp://" + device.getAddress());

                    Log.v(TAG, "Trying to send notification to " + client.getLastEndpoint());

                    if (zMsg.send(client, false)) {
                        Log.v(TAG, "Successfully sent notification to " + client.getLastEndpoint());
                    } else {
                        Log.v(TAG, "Failed to send notification to " + client.getLastEndpoint());
                    }
                }

                countDownLatch.countDown();
            }));

            try {
                countDownLatch.await();
            } catch (InterruptedException ignored) {}

            Log.v(TAG, "Finished trying to send notification");
        }
    }
}
