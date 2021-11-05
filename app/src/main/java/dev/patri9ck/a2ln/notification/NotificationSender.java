package dev.patri9ck.a2ln.notification;

import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.patri9ck.a2ln.address.Address;

public class NotificationSender implements AutoCloseable {

    private static final String TAG = "A2LNNS";

    private static final String ADDRESS = "tcp://%s:%d";

    private static final int TIMEOUT_SECONDS = 5;
    private static final int CLOSE_SECONDS = 10;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private ZContext zContext;

    private List<ZMQ.Socket> sockets;

    private Timer closeTimer;

    private List<Address> addresses;

    public NotificationSender(List<Address> addresses) {
        this.addresses = addresses;
    }

    public synchronized void setAddresses(List<Address> addresses) {
        close();

        this.addresses = addresses;
    }

    public synchronized void sendParsedNotification(ParsedNotification parsedNotification) {
        if (addresses.isEmpty()) {
            Log.v(TAG, "No addresses given, will not start sockets");

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

        CountDownLatch countDownLatch = new CountDownLatch(sockets.size());

        sockets.forEach(socket -> executorService.execute(() -> {
            Log.v(TAG, "Trying to send notification to " + socket.getLastEndpoint() + " (" + countDownLatch.getCount() + ")");

            zMsg.send(socket);

            countDownLatch.countDown();
        }));

        try {
            countDownLatch.await();
        } catch (InterruptedException exception) {}

        Log.v(TAG, "Finished trying to send notification");
    }

    private synchronized void startSockets() {
        if (zContext != null && !zContext.isClosed()) {
            return;
        }

        Log.v(TAG, "Starting sockets");

        zContext = new ZContext();

        sockets = new ArrayList<>();

        addresses.forEach(address -> {
            ZMQ.Socket socket = zContext.createSocket(SocketType.PUSH);

            socket.setSendTimeOut(TIMEOUT_SECONDS * 1000);
            socket.setImmediate(false);
            socket.connect(String.format(ADDRESS, address.getHost(), address.getPort()));

            sockets.add(socket);
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
    }

    @Override
    public synchronized void close() {
        stopSockets();
        stopCloseTimer();
    }
}
