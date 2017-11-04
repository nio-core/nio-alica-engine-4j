package de.uniks.vs.jalica.supplementary;

import java.util.Vector;

/**
 * Created by alex on 14.07.17.
 */
public class Configuration {
    public Vector<String> getSections(String s) {
        Vector<String> vector = new Vector<>();
        vector.add("NIO.ZERO");
        return vector;
    }
}
