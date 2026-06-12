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

import java.util.Optional;

import dev.patri9ck.a2ln.log.KeptLog;

public class PairingResult {

    private final KeptLog keptLog;
    private final byte[] publicKey;

    public PairingResult(KeptLog keptLog) {
        this(keptLog, null);
    }

    public PairingResult(KeptLog keptLog, byte[] publicKey) {
        this.keptLog = keptLog;
        this.publicKey = publicKey;
    }

    public KeptLog getKeptLog() {
        return keptLog;
    }

    public Optional<byte[]> getPublicKey() {
        return Optional.ofNullable(publicKey);
    }
}
