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
package dev.patri9ck.a2ln.pairing;

import android.content.Context;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.log.KeptLog;
import dev.patri9ck.a2ln.server.Destination;
import dev.patri9ck.a2ln.server.Server;
import dev.patri9ck.a2ln.util.Util;

public class Pairing {

    private static final String TAG = "A2LNP";

    private static final int TIMEOUT_SECONDS = 20;

    private final Context context;
    private final Destination destination;
    private final String ip;
    private final String rawPublicKey;

    public Pairing(Context context, Destination destination, String ip, String rawPublicKey) {
        this.context = context;
        this.destination = destination;
        this.ip = ip;
        this.rawPublicKey = rawPublicKey;
    }

    public PairingResult pair() {
        KeptLog keptLog = new KeptLog(context, TAG);

        String address = destination.getAddress();

        keptLog.log(Log.INFO, R.string.log_pairing_trying, address);

        try (ZContext zContext = new ZContext(); ZMQ.Socket client = zContext.createSocket(SocketType.REQ)) {
            client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
            client.setReceiveTimeOut(TIMEOUT_SECONDS * 1000);
            client.setImmediate(false);

            if (!client.connect("tcp://" + address)) {
                keptLog.log(Log.ERROR, R.string.log_failed_connection, address);

                return new PairingResult(keptLog);
            }

            ZMsg zMsg = new ZMsg();

            zMsg.add(ip);
            zMsg.add(rawPublicKey);

            if (!zMsg.send(client)) {
                keptLog.log(Log.ERROR, R.string.log_pairing_failed_sending, address);

                return new PairingResult(keptLog);
            }

            zMsg = ZMsg.recvMsg(client);

            if (zMsg == null || zMsg.size() != 2) {
                keptLog.log(Log.ERROR, R.string.log_pairing_failed_receiving, address);

                return new PairingResult(keptLog);
            }

            Optional<Integer> port = Util.parsePort(zMsg.pop().getString(StandardCharsets.UTF_8));

            if (!port.isPresent()) {
                keptLog.log(Log.ERROR, R.string.log_pairing_invalid_port);

                return new PairingResult(keptLog);
            }

            return new PairingResult(keptLog, new Server(destination.getIp(), port.get(), zMsg.pop().getData()));
        }
    }
}
