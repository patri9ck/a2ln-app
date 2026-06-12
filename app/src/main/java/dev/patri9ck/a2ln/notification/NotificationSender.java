/*
 * Copyright (C) 2022 Patrick Zwick and contributors
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
import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.server.Server;
import dev.patri9ck.a2ln.util.JsonListConverter;
import zmq.util.Z85;

public class NotificationSender {

    private static final String TAG = "A2LNNS";

    private static final int TIMEOUT_SECONDS = 5;

    private final byte[] clientPublicKey;
    private final byte[] clientSecretKey;
    private List<Server> servers;

    public NotificationSender(List<Server> servers, byte[] clientPublicKey, byte[] clientSecretKey) {
        this.servers = servers;
        this.clientPublicKey = clientPublicKey;
        this.clientSecretKey = clientSecretKey;
    }

    public static NotificationSender fromSharedPreferences(Context context, SharedPreferences sharedPreferences) {
        String clientPublicKey = sharedPreferences.getString(context.getString(R.string.preferences_own_public_key), null);
        String clientSecretKey = sharedPreferences.getString(context.getString(R.string.preferences_own_secret_key), null);

        if (clientPublicKey == null || clientSecretKey == null) {
            Log.e(TAG, "Client keys not saved in preferences properly");

            return null;
        }

        return new NotificationSender(JsonListConverter.fromJson(sharedPreferences.getString(context.getString(R.string.preferences_servers), null), Server.class),
                Z85.decode(clientPublicKey),
                Z85.decode(clientSecretKey));
    }

    public synchronized void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public synchronized void sendParsedNotification(ParsedNotification parsedNotification) {
        if (servers.isEmpty()) {
            Log.v(TAG, "No servers given, will not start clients");

            return;
        }

        Log.v(TAG, "Trying to send notification to clients");

        ZMsg zMsg = new ZMsg();

        zMsg.add(parsedNotification.getTitle());
        zMsg.add(parsedNotification.getText());

        byte[] icon = parsedNotification.getIcon();

        if (icon != null) {
            zMsg.add(icon);
        }

        try (ZContext zContext = new ZContext()) {
            servers.forEach(server -> CompletableFuture.runAsync(() -> {
                try (ZMQ.Socket client = zContext.createSocket(SocketType.PUSH)) {
                    client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
                    client.setImmediate(false);
                    client.setCurvePublicKey(clientPublicKey);
                    client.setCurveSecretKey(clientSecretKey);
                    client.setCurveServerKey(server.getPublicKey());

                    if (client.connect("tcp://" + server.getAddress()) && zMsg.send(client, false)) {
                        Log.v(TAG, "Successfully sent notification to " + client.getLastEndpoint());

                        return;
                    }

                    Log.v(TAG, "Failed to send notification to " + client.getLastEndpoint());
                }
            }));
        }
    }
}
