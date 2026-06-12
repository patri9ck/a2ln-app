/*
 * Copyright (C) 2022  Patrick Zwick and contributors
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
import android.content.SharedPreferences;
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
import dev.patri9ck.a2ln.server.Server;
import dev.patri9ck.a2ln.log.KeptLog;
import dev.patri9ck.a2ln.util.Util;
import zmq.util.Z85;

public class NotificationSender {

    private static final String TAG = "A2LNNS";

    private static final int TIMEOUT_SECONDS = 5;

    private final byte[] clientPublicKey;
    private final byte[] clientSecretKey;
    private List<Server> servers;

    public NotificationSender(byte[] clientPublicKey, byte[] clientSecretKey, List<Server> servers) {
        this.clientPublicKey = clientPublicKey;
        this.clientSecretKey = clientSecretKey;
        this.servers = filterServers(servers);
    }

    public static Optional<NotificationSender> fromSharedPreferences(Context context, SharedPreferences sharedPreferences) {
        String clientPublicKey = sharedPreferences.getString(context.getString(R.string.preferences_own_public_key), null);
        String clientSecretKey = sharedPreferences.getString(context.getString(R.string.preferences_own_secret_key), null);

        if (clientPublicKey == null || clientSecretKey == null) {
            Log.e(TAG, "Client keys not saved in preferences properly");

            return Optional.empty();
        }

        return Optional.of(new NotificationSender(Z85.decode(clientPublicKey),
                Z85.decode(clientSecretKey),
                Util.fromJson(sharedPreferences.getString(context.getString(R.string.preferences_servers), null), Server.class)));
    }

    public void setServers(List<Server> servers) {
        this.servers = filterServers(servers);
    }

    public KeptLog sendParsedNotification(ParsedNotification parsedNotification) {
        KeptLog keptLog = new KeptLog(TAG);

        if (servers.isEmpty()) {
            keptLog.log("No servers given, will not start clients");

            return keptLog;
        }

        keptLog.log("Trying to send notification to servers");

        ZMsg zMsg = new ZMsg();

        zMsg.add(parsedNotification.getAppName());
        zMsg.add(parsedNotification.getTitle());
        zMsg.add(parsedNotification.getText());

        parsedNotification.getIcon().ifPresent(zMsg::add);

        CountDownLatch countDownLatch = new CountDownLatch(servers.size());

        try (ZContext zContext = new ZContext()) {
            servers.forEach(server -> CompletableFuture.runAsync(() -> {
                try (ZMQ.Socket client = zContext.createSocket(SocketType.PUSH)) {
                    client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
                    client.setImmediate(false);
                    client.setCurvePublicKey(clientPublicKey);
                    client.setCurveSecretKey(clientSecretKey);
                    client.setCurveServerKey(server.getPublicKey());

                    String address = server.getAddress();

                    if (!client.connect("tcp://" + address)) {
                        keptLog.log("Failed to connect to " + address);
                    } else if (!zMsg.send(client, false)) {
                        keptLog.log("Failed to send notification to " + address);
                    } else {
                        keptLog.log("Successfully sent notification to " + address);
                    }

                    countDownLatch.countDown();
                }
            }));
        } finally {
            try {
                if (!countDownLatch.await(TIMEOUT_SECONDS + 1, TimeUnit.SECONDS)) {
                    keptLog.log("Timed out");
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
