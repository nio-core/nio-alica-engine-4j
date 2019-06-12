package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.model.AlicaElement;

import java.util.ArrayList;

/**
 * Created by alex on 04.11.17.
 */
@Deprecated
public class CapabilityDefinitionSet extends AlicaElement {
    ArrayList<Capability> capabilities = new ArrayList<>();

    public ArrayList<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ArrayList<Capability> capabilities) {
        this.capabilities = capabilities;
    }
}
