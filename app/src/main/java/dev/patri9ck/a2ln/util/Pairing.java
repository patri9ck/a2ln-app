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
package dev.patri9ck.a2ln.util;

import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.server.Server;

public class Pairing {

    private static final String TAG = "A2LNP";

    private static final int TIMEOUT_SECONDS = 20;

    private final String deviceIp;
    private final int pairingPort;
    private final String clientIp;
    private final String clientPublicKey;

    public Pairing(String deviceIp, int pairingPort, String clientIp, String clientPublicKey) {
        this.deviceIp = deviceIp;
        this.pairingPort = pairingPort;
        this.clientIp = clientIp;
        this.clientPublicKey = clientPublicKey;
    }

    public CompletableFuture<Server> pair() {
        return CompletableFuture.supplyAsync(() -> {
            String address = deviceIp + ":" + pairingPort;

            Log.v(TAG, "Trying to pair with " + address);

            try (ZContext zContext = new ZContext(); ZMQ.Socket client = zContext.createSocket(SocketType.REQ)) {
                client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
                client.setReceiveTimeOut(TIMEOUT_SECONDS * 1000);
                client.setImmediate(false);

                if (!client.connect("tcp://" + address)) {
                    Log.v(TAG, "Failed to connect to " + address);

                    return null;
                }

                ZMsg zMsg = new ZMsg();

                zMsg.add(clientIp);
                zMsg.add(clientPublicKey);

                if (!zMsg.send(client)) {
                    Log.v(TAG, "Failed to send own IP and public key to " + address);

                    return null;
                }

                zMsg = ZMsg.recvMsg(client);

                if (zMsg == null || zMsg.size() != 2) {
                    Log.v(TAG, "Failed to receive port and public key from " + address);

                    return null;
                }

                int devicePort = PortParser.parsePort(zMsg.pop().getString(StandardCharsets.UTF_8));

                if (devicePort == PortParser.INVALID_PORT) {
                    Log.v(TAG, "Received port is not valid");

                    return null;
                }

                return new Server(deviceIp, devicePort, zMsg.pop().getData());
            }
        });
    }
}
