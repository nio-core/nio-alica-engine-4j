package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 17.07.17.
 */
public class AlicaElement {

    protected long id;

    public long getId() {
        return id;
    }

    protected void setId(long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected String name;

    protected String getComment() {
        return comment;
    }

    protected void setComment(String comment) {
        this.comment = comment;
    }

    protected  String comment;



}
