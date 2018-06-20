package de.uniks.vs.jalica.unknown;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by alex on 21.07.17.
 */
public class CommonUtils {

    public static final boolean RULE_debug = true;
    public static final boolean SM_FAILURE_debug = false;
    public static final boolean SM_MESSAGES_debug = false;
    public static final boolean PP_DEBUG_debug = false;
    public static final boolean UFDEBUG_debug = false;
    public static final boolean PB_DEBUG_debug = false;
    public static final boolean CM_REASON_DEBUG_debug = true;
    public static final boolean CM_DEBUG_debug = true;
    public static final boolean SUCDEBUG_debug = false;
    public static final boolean CS_DEBUG_debug = false;

    public static final int EXIT_FAILURE = 1;
    public static final int EXIT_SUCCESS = 0;

    public static <T> T find(ArrayList<T> list, int start, int end, Object obj) {
        int index = list.subList(start, end).indexOf(obj);

        return index > -1 ? list.get(index): null;

    }

    public static <T> T find(Vector<T> list, int start, int end, Object obj) {
        if(list.size() == 0 || start > end) {
            CommonUtils.aboutError("CU: find error     start:" + start + " end:" + end + "  obj:" +obj);
            return null;
        }
        int index = list.subList(start, end).indexOf(obj);
        return index > -1 ? list.get(index): null;
    }

    public static <T> ArrayList<T> move(ArrayList<T> agents) {
        ArrayList<T> newAgents = agents;
        agents.clear();
        return newAgents;
    }

    public static <T> void copy(Set<T> agentsInState, int start, int end, Vector<T> agents) {
        T[] array = (T[]) agentsInState.toArray();

        for (int i = start; i <= end; i++)
            agents.add(array[i]);
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

    public static void aboutNoImpl() {
            try {
                throw new Exception();
            } catch (Exception e) {
                System.out.flush();
                StackTraceElement[] stackTrace = e.getStackTrace();
                System.err.println(stackTrace[1] + " ->  Implementation is missing!");
                System.err.flush();
            }
    }

    public static void aboutError(String msg) {
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.flush();
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.err.println(msg + "   (" + stackTrace[2]+")");
            System.err.flush();
        }
    }

    public static void aboutImplIncomplete() {
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.flush();
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.err.println(stackTrace[1] + " ->  Implementation incomplete!");
            System.err.flush();
        }
    }

    public static void stable_sort(Vector vector, int start, int end) {

        if (end < start) {
            System.out.println("CU: sort start > end" );
            return;
        }
        Collections.sort(vector.subList(start,end));
    }

    public static double round(double d) {
        return (Math.round(d));
    }

    public static double roundCorrect(double num, int precision) {
        double EPSILON = 1.0;
        double c = 0.5 * EPSILON * num;
        double p = 1; while (precision--> 0) p *= 10;
        if (num < 0)
            p *= -1;
        return Math.round((num + c) * p) / p;
    }
}
