package dev.patri9ck.a2ln.util;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.device.Device;

public class Pairing {

    private static final int TIMEOUT_SECONDS = 20;

    private final String deviceIp;
    private final int devicePort;
    private final String clientIp;
    private final String clientPublicKey;

    public Pairing(String deviceIp, int devicePort, String clientIp, String clientPublicKey) {
        this.deviceIp = deviceIp;
        this.devicePort = devicePort;
        this.clientIp = clientIp;
        this.clientPublicKey = clientPublicKey;
    }

    public CompletableFuture<Device> pair() {
        return CompletableFuture.supplyAsync(() -> {
            try (ZContext zContext = new ZContext(); ZMQ.Socket client = zContext.createSocket(SocketType.REQ)) {
                client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
                client.setReceiveTimeOut(TIMEOUT_SECONDS * 1000);

                client.setImmediate(false);

                if (!client.connect("tcp://" + deviceIp + ":" + devicePort)) {
                    return null;
                }

                ZMsg zMsg = new ZMsg();

                zMsg.add(clientIp);
                zMsg.add(clientPublicKey);

                if (!zMsg.send(client)) {
                    return null;
                }

                zMsg = ZMsg.recvMsg(client);

                if (zMsg == null || zMsg.size() != 2) {
                    return null;
                }

                try {
                    return new Device(deviceIp, Integer.parseInt(zMsg.pop().getString(StandardCharsets.UTF_8)), zMsg.pop().getData());
                } catch (NumberFormatException exception) {
                    return null;
                }
            }
        });
    }
}
