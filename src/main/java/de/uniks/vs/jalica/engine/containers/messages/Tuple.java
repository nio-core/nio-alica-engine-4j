package de.uniks.vs.jalica.engine.containers.messages;

public class Tuple {
    private Object[] values;

    public Tuple(Object... values) {
        this.values = values;
    }

    public Object[] get() {
        return values;
    }
}
