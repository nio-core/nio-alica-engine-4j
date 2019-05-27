package de.uniks.vs.jalica.engine.model;

/**
 * Created by alex on 17.07.17.
 */
public class AlicaElement {

    protected long id;

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

    protected String name;

    protected String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    protected  String comment;



}
