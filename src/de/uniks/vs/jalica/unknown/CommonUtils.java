package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class CommonUtils {

    public static final int EXIT_FAILURE = 1;
    public static final int EXIT_SUCCESS = 0;

    public static <T> T find(ArrayList<T> list, int start, int end, Object obj) {
        int index = list.subList(start, end).indexOf(obj);

        return index > -1 ? list.get(index): null;

    }

    public static <T> T find(Vector<T> list, int start, int end, Object obj) {
        int index = list.subList(start, end).indexOf(obj);

        return index > -1 ? list.get(index): null;

    }

    public static <T> ArrayList<T> move(ArrayList<T> robots) {
        ArrayList<T> robots2 = robots;
        robots.clear();
        return robots2;
    }

    public static <T> void copy(Set<T> robotsInState, int start, int end, Vector<T> robots) {
        T[] array = (T[]) robotsInState.toArray();

        for (int i = start; i <= end; i++)
            robots.add(array[i]);
    }

    public static int stoi(String string) {
        return Integer.parseInt(string);
    }

    public static double stod(String string) {
        return Double.parseDouble(string);
    }

    public static long stol(String string) {
        return Long.parseLong(string);
    }
}
