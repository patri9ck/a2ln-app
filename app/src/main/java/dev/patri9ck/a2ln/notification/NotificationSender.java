/*
 * Android 2 Linux Notifications - A way to display Android phone notifications on Linux
 * Copyright (C) 2023  patri9ck and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.patri9ck.a2ln.notification;

import android.content.Context;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.log.KeptLog;
import dev.patri9ck.a2ln.server.Server;
import dev.patri9ck.a2ln.util.Storage;
import zmq.util.Z85;

public class NotificationSender {

    private static final String TAG = "A2LNNS";

    private static final int TIMEOUT_SECONDS = 3;

    private final Context context;
    private final byte[] publicKey;
    private final byte[] secretKey;
    private List<Server> servers;

    public NotificationSender(Context context, byte[] publicKey, byte[] secretKey, List<Server> servers) {
        this.context = context;
        this.publicKey = publicKey;
        this.secretKey = secretKey;
        this.servers = filterServers(servers);
    }

    public static Optional<NotificationSender> fromStorage(Context context, Storage storage) {
        String rawPublicKey = storage.loadRawPublicKey().orElse(null);

        if (rawPublicKey == null) {
            Log.e(TAG, "Own public key does not exist");

            return Optional.empty();
        }

        String rawSecretKey = storage.loadRawSecretKey().orElse(null);

        if (rawSecretKey == null) {
            Log.e(TAG, "Own secret key does not exist");

            return Optional.empty();
        }

        byte[] publicKey = Z85.decode(rawPublicKey);

        if (publicKey == null) {
            Log.e(TAG, "Cannot decode own public key");

            return Optional.empty();
        }

        byte[] secretKey = Z85.decode(rawSecretKey);

        if (secretKey == null) {
            Log.e(TAG, "Cannot decode own secret key");

            return Optional.empty();
        }

        return Optional.of(new NotificationSender(context, publicKey, secretKey, storage.loadServers()));
    }

    public void setServers(List<Server> servers) {
        this.servers = filterServers(servers);
    }

    public KeptLog sendParsedNotification(ParsedNotification parsedNotification) {
        KeptLog keptLog = new KeptLog(context);

        if (servers.isEmpty()) {
            keptLog.log(R.string.log_notification_no_servers);

            return keptLog;
        }

        keptLog.log(R.string.log_notification_trying);

        ZMsg zMsg = new ZMsg();

        zMsg.add(parsedNotification.getAppName());
        zMsg.add(parsedNotification.getTitle());
        zMsg.add(parsedNotification.getText());
        zMsg.add(parsedNotification.getPackageName());

        parsedNotification.getIcon().ifPresent(zMsg::add);

        CountDownLatch countDownLatch = new CountDownLatch(servers.size());

        try (ZContext zContext = new ZContext()) {
            servers.forEach(server -> CompletableFuture.runAsync(() -> {
                try (ZMQ.Socket client = zContext.createSocket(SocketType.PUSH)) {
                    client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
                    client.setImmediate(false);
                    client.setCurvePublicKey(publicKey);
                    client.setCurveSecretKey(secretKey);
                    client.setCurveServerKey(server.getPublicKey());

                    String address = server.getAddress();

                    if (!client.connect("tcp://" + address)) {
                        keptLog.log(R.string.log_failed_connection, address);
                    } else if (!zMsg.send(client, false)) {
                        keptLog.log(R.string.log_notification_failed_sending, address);
                    } else {
                        keptLog.log(R.string.log_notification_success, address);
                    }

                    countDownLatch.countDown();
                }
            }));
        } finally {
            try {
                if (!countDownLatch.await(TIMEOUT_SECONDS + 1, TimeUnit.SECONDS)) {
                    keptLog.log(R.string.log_notification_timed_out);
                }
            } catch (InterruptedException ignored) {
                // Ignored
            }

            zMsg.destroy();
        }

        return keptLog;
    }

    private List<Server> filterServers(List<Server> servers) {
        return servers.stream().filter(Server::isEnabled).collect(Collectors.toList());
    }
}
