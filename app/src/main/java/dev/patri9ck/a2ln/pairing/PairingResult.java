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

import java.util.Optional;

import dev.patri9ck.a2ln.log.KeptLog;
import dev.patri9ck.a2ln.server.Server;

public class PairingResult {

    private final KeptLog keptLog;
    private Server server;

    public PairingResult(KeptLog keptLog) {
        this.keptLog = keptLog;
    }

    public PairingResult(KeptLog keptLog, Server server) {
        this.keptLog = keptLog;
        this.server = server;
    }

    public KeptLog getKeptLog() {
        return keptLog;
    }

    public Optional<Server> getServer() {
        return Optional.ofNullable(server);
    }
}
