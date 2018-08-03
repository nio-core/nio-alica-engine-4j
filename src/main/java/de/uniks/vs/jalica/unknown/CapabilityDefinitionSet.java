package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;

/**
 * Created by alex on 04.11.17.
 */
public class CapabilityDefinitionSet extends AlicaElement {
    ArrayList<Capability> capabilities = new ArrayList<>();

    public ArrayList<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ArrayList<Capability> capabilities) {
        this.capabilities = capabilities;
    }
}
