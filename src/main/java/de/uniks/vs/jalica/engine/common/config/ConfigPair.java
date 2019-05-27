package de.uniks.vs.jalica.engine.common.config;

import java.util.Vector;

public class ConfigPair {

    private String name;
    private Vector value;
    private ConfigPair parent;

    public ConfigPair(String name, Vector value, ConfigPair parent) {
        this.name = name;
        this.value = value;
        this.parent = parent;

        if (this.parent != null) this.parent.value.add(this);
    }

    public ConfigPair(String name, String value, ConfigPair parent) {
        this.name = name;
        this.value = new Vector();
        this.value.add(value);
        this.parent = parent;

        if (this.parent != null) this.parent.value.add(this);
    }

//    public void print() {
//
//        if(value.get(0) instanceof String)
//            System.out.println(name +" = " + value);
//        else
//            System.out.println("["+name+ "]");
//
//        for (Object entry: value) {
//
//            if(entry instanceof ConfigPair)
//                ((ConfigPair)entry).print();
//        }
//    }

    public Object get(String string) {
        String[] parts = string.split("\\.");
        Object token = null;

        for (String part: parts) {
             token = findToken(part, token);

            if (token != null && ((ConfigPair)token).getValue().get(0) instanceof String) {
                return ((ConfigPair)token).getValue().get(0);
            }
        }
        return token;
    }

    private Object findToken(String part, Object token) {
        Vector values = value;

        if(token != null)
            values = ((ConfigPair)token).getValue();

        for (Object obj: values) {

//            if(obj instanceof String && ((String)obj).equals(part)) {
//                return obj;
//            }
//            else
            if(obj instanceof ConfigPair && ((ConfigPair)obj).name.equals(part)) {
                return obj;
            }
        }
        return null;
    }

    public Vector getKeys() {
        Vector keys = new Vector();

        for (Object obj: getValue()) {
            keys.add(((ConfigPair)obj).getName());
        }
        return keys;
    }


    public String getName() {
        return name;
    }

    public Vector getValue() {
        return value;
    }

    public ConfigPair getParent() {
        return parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Vector value) {
        this.value = value;
    }

    public void setParent(ConfigPair parent) {
        this.parent = parent;
    }
}
