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
package dev.patri9ck.a2ln.pairing;

import android.content.Context;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.log.KeptLog;
import dev.patri9ck.a2ln.server.Destination;

public class Pairing {

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
        KeptLog keptLog = new KeptLog(context);

        String address = destination.getAddress();

        keptLog.log(R.string.log_pairing_trying, address);

        try (ZContext zContext = new ZContext(); ZMQ.Socket client = zContext.createSocket(SocketType.REQ)) {
            client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
            client.setReceiveTimeOut(TIMEOUT_SECONDS * 1000);
            client.setImmediate(false);

            if (!client.connect("tcp://" + address)) {
                keptLog.log(R.string.log_failed_connection, address);

                return new PairingResult(keptLog);
            }

            ZMsg zMsg = new ZMsg();

            zMsg.add(ip);
            zMsg.add(rawPublicKey);

            if (!zMsg.send(client)) {
                keptLog.log(R.string.log_pairing_failed_sending, address);

                return new PairingResult(keptLog);
            }

            byte[] publicKey = client.recv();

            if (publicKey == null) {
                keptLog.log(R.string.log_pairing_failed_receiving, address);

                return new PairingResult(keptLog);
            }

            return new PairingResult(keptLog, publicKey);
        }
    }
}
