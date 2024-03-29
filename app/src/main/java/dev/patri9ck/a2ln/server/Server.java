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
package dev.patri9ck.a2ln.server;

import java.util.Optional;

public class Server extends Destination {

    private byte[] publicKey;
    private String alias;
    private boolean enabled;

    public Server() {
        // Gson
    }

    public Server(String ip, int port, byte[] publicKey, String alias, boolean enabled) {
        super(ip, port);

        this.publicKey = publicKey;
        this.alias = alias;
        this.enabled = enabled;
    }

    public Server(String ip, int port, byte[] publicKey) {
        this(ip, port, publicKey, null, true);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public Optional<String> getAlias() {
        return Optional.ofNullable(alias);
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
