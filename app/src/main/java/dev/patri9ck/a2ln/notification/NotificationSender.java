package dev.patri9ck.a2ln.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.device.Device;

public class NotificationSender implements AutoCloseable {

    private static final String TAG = "A2LNNS";

    private static final int TIMEOUT_SECONDS = 5;
    private static final int CLOSE_SECONDS = 10;

    private ZContext zContext;
    private List<ZMQ.Socket> clients;

    private Timer closeTimer;

    private List<Device> devices;

    byte[] clientPublicKey;
    byte[] clientSecretKey;

    public NotificationSender(List<Device> devices, byte[] clientPublicKey, byte[] clientSecretKey) {
        this.devices = devices;

        this.clientPublicKey = clientPublicKey;
        this.clientSecretKey = clientSecretKey;
    }

    public static NotificationSender loadNotificationSender(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);

        String clientPublicKey = sharedPreferences.getString(context.getString(R.string.preferences_client_public_key), null);
        String clientSecretKey = sharedPreferences.getString(context.getString(R.string.preferences_client_secret_key), null);

        if (clientPublicKey == null || clientSecretKey == null) {
            Log.e(TAG, "Client keys not saved in preferences properly");

            return null;
        }

        return new NotificationSender(Device.fromJson(sharedPreferences.getString(context.getString(R.string.preferences_devices), null)),
                Base64.getDecoder().decode(clientPublicKey),
                Base64.getDecoder().decode(clientSecretKey));
    }

    public synchronized void setDevices(List<Device> devices) {
        close();

        this.devices = devices;
    }

    public synchronized void sendParsedNotification(ParsedNotification parsedNotification) {
        if (devices.isEmpty()) {
            Log.v(TAG, "No devices given, will not start sockets");

            return;
        }

        startSockets();
        startCloseTimer();

        ZMsg zMsg = new ZMsg();

        zMsg.add(parsedNotification.getTitle());
        zMsg.add(parsedNotification.getText());

        byte[] icon = parsedNotification.getIcon();

        if (icon != null) {
            zMsg.add(icon);
        }

        CountDownLatch countDownLatch = new CountDownLatch(clients.size());

        clients.forEach(client -> CompletableFuture.runAsync(() -> {
            Log.v(TAG, "Trying to send notification to " + client.getLastEndpoint() + " (" + countDownLatch.getCount() + ")");

            zMsg.send(client);

            countDownLatch.countDown();
        }));

        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {}

        Log.v(TAG, "Finished trying to send notification");
    }

    private synchronized void startSockets() {
        if (zContext != null && !zContext.isClosed()) {
            return;
        }

        Log.v(TAG, "Starting sockets");

        zContext = new ZContext();

        clients = new ArrayList<>();

        devices.forEach(device -> {
            ZMQ.Socket client = zContext.createSocket(SocketType.PUSH);

            client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
            client.setImmediate(false);
            client.setCurvePublicKey(clientPublicKey);
            client.setCurveSecretKey(clientSecretKey);
            client.setCurveServerKey(device.getServerPublicKey().getBytes(StandardCharsets.UTF_8));
            client.connect("tcp://" + device.getAddress());

            clients.add(client);
        });
    }

    private synchronized void stopSockets() {
        if (zContext == null || zContext.isClosed()) {
            return;
        }

        Log.v(TAG, "Stopping sockets");

        zContext.close();
    }

    private synchronized void startCloseTimer() {
        stopCloseTimer();

        closeTimer = new Timer();

        closeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopSockets();

                closeTimer = null;
            }
        }, CLOSE_SECONDS * 1000);
    }

    private synchronized void stopCloseTimer() {
        if (closeTimer == null) {
            return;
        }

        closeTimer.cancel();

        closeTimer = null;
    }

    @Override
    public synchronized void close() {
        stopSockets();
        stopCloseTimer();
    }
}
