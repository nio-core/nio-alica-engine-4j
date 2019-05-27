package de.uniks.vs.jalica.engine.common.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class ConfigParser {

    private Vector<TokenParser> tokenParsers;
    private static ConfigParser instance;

    public ConfigParser() {
        tokenParsers = new Vector();
        tokenParsers.add(new SegmentParser());
        tokenParsers.add(new PropertyParser());
    }

    public ConfigPair parse(String path) {
        BufferedReader br = null;
        ConfigPair current = null;

        try {
            br = new BufferedReader(new FileReader(path));
            String line = br.readLine();

            while (line != null) {
                current = handle(line, current);
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        current.print();
        return current;
    }

    private ConfigPair handle(String line, ConfigPair current) {

        for (TokenParser parser : tokenParsers) {
            ConfigPair tmp = parser.handle(line, current);

            if (tmp != null)
                return tmp;
        }
        return current;
    }

    public static ConfigParser getInstance() {

        if (instance == null)
            instance = new ConfigParser();
        return instance;
    }
}
