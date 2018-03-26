package de.uniks.vs.jalica.common;

public class PropertyParser extends TokenParser {

    @Override
    public ConfigPair handle(String line, ConfigPair current) {
        line = line.replaceAll("\\t", "");
        line = line.substring(0, line.indexOf("#") > -1 ? line.indexOf("#"): line.length());

        if (line.isEmpty() || !line.contains("="))
            return null;

        line = line.replaceAll(" ", "");
        String[] split = line.split("=");
        new ConfigPair(split[0],split[1], current);
        return current;
    }
}
