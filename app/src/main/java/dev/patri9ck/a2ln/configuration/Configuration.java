package dev.patri9ck.a2ln.configuration;

import java.util.List;

import dev.patri9ck.a2ln.address.Address;

public class Configuration {

    private List<Address> addresses;
    private List<String> disabledApps;

    public Configuration() {
        // Gson
    }

    public Configuration(List<Address> addresses, List<String> disabledApps) {
        this.addresses = addresses;
        this.disabledApps = disabledApps;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public List<String> getDisabledApps() {
        return disabledApps;
    }
}
