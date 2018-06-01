package de.uniks.vs.jalica.common.config;

import java.util.Vector;

public class SegmentParser extends TokenParser {

    @Override
    public ConfigPair handle(String line, ConfigPair current) {

        if (!(line.contains("[") && line.contains("]")))
            return null;
        line = line.replaceAll("\\t", "");
        line = line.replaceAll(" ", "");

        if (line.startsWith("[!")) {

            if(current.getParent() != null)
                return current.getParent();
            return current;
        }

        else if (line.startsWith("[")) {
            return new ConfigPair(line.substring(1, line.length()-1), new Vector(), current);
        }
        return null;
    }
}
