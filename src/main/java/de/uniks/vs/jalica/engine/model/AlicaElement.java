package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.idmanagement.ID;

/**
 * Created by alex on 17.07.17.
 */
public class AlicaElement {

    protected long id;
    private String name;
    private String comment;

    public AlicaElement() { }

    public AlicaElement(long id) {
        this.id = id;
    }

    public AlicaElement(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public AlicaElement(long id, String name, String comment) {
        this.id = id;
        this.name = name;
        this.comment = comment;
    }

    public long getID() {
        return id;
    }
    public void setID(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    protected String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent + "Name: " + getName() + " ID: " + getID() + " Comment: " + getComment() + "\n";
        return ss;
    }

}
