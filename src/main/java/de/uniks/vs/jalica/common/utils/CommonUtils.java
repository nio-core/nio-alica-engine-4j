package de.uniks.vs.jalica.common.utils;

import de.uniks.vs.jalica.common.TimerEvent;
import de.uniks.vs.jalica.engine.AlicaTime;

import java.util.*;

/**
 * Created by alex on 21.07.17.
 */
public class CommonUtils {

    public static final boolean PP_DEBUG_debug = false;
    public static final boolean CM_DEBUG_debug = false;
    public static final boolean MF_DEBUG_debug = false;
    public static final boolean RA_DEBUG_debug = true;
    public static final boolean FS_DEBUG_debug = false;
    public static final boolean PS_DEBUG_debug = false;
    public static final boolean TA_DEBUG_debug = false;
    public static final boolean AM_DEBUG_debug = false;
    public static final boolean PA_DEBUG_debug = false;
    public static final boolean AE_DEBUG_debug = false;
    public static final boolean SC_DEBUG_debug = false;
    public static final boolean BP_DEBUG_debug = false;

    // Communication
    public static final boolean COMM_debug = false;
    public static final boolean COMM_D_DEBUG_debug = false;

    public static final boolean RULE_debug = false;
    public static final boolean SM_FAILURE_debug = false;
    public static final boolean SM_MESSAGES_debug = false;
    public static final boolean UFDEBUG_debug = false;
    public static final boolean PB_DEBUG_debug = false;
    public static final boolean CM_REASON_DEBUG_debug = false;
    public static final boolean SUCDEBUG_debug = false;
    public static final boolean CS_DEBUG_debug = false;
    public static final boolean C_DEBUG_debug = false;
    public static final boolean TO_DEBUG_debug = false;
    public static final boolean RP_DEBUG_debug = false;
    public static final boolean AC_DEBUG_debug = false;
    public static final boolean TE_DEBUG_debug = false;
    public static final boolean B_DEBUG_debug = true;
    public static final boolean CV_DEBUG_debug = false;
    public static final boolean AED_DEBUG_debug = false;
    public static final boolean XTH_DEBUG_debug = false;
    public static final boolean DC_debug = false;

    // DEBUG Extension
    public static final boolean VERBOSE_ARRAY_LIST_DEBUG = false;
    public static final boolean MISSING_IMPLEMENTATION_debug = true;
    public static final boolean IMPLEMENTATION_INCOMPLETE_debug = true;
    public static final boolean CALL_debug = true;
    public static final boolean ERROR_debug = true;

    public static final int EXIT_FAILURE = 1;
    public static final int EXIT_SUCCESS = 0;

    public static final boolean CU_DEBUG_debug = false;


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

    public static int findIndex(Vector list, int start, int end, Object obj) {

        if(list.size() == 0 || start > end) {
            CommonUtils.aboutError("CU: findIndex error     start:" + start + " end:" + end + "  obj:" +obj);
            return -1;
        }
        return list.subList(start, end).indexOf(obj);
    }

    public static <T> ArrayList<T> move(ArrayList<T> agents) {
        ArrayList<T> newAgents = new ArrayList<>(agents);
        agents.clear();
        return newAgents;
    }

    public static <T> HashSet<T> move(HashSet<T> agents) {
        HashSet<T> newAgents = new HashSet<>(agents);
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

            if (!MISSING_IMPLEMENTATION_debug)
                return;

            try {
                throw new Exception();
            } catch (Exception e) {
                System.out.flush();
                StackTraceElement[] stackTrace = e.getStackTrace();
                System.out.println(stackTrace[1] + " ->  Implementation is missing!");
                System.out.flush();
            }
    }

    public static void aboutError(String msg) {
        if (!ERROR_debug)
            return;

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

        if(!IMPLEMENTATION_INCOMPLETE_debug)
            return;

        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.flush();
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.out.println(stackTrace[1] + " ->  Implementation incomplete!");
            System.out.flush();
        }
    }

    public static void aboutCallNotification(String msg) {

        if (!CALL_debug)
            return;

        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.flush();
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.out.print(stackTrace[1] + " ->  called  ("+msg+")");
            if (stackTrace.length > 2)
                System.out.println("  (from " + stackTrace[2] + ")");
            else
                System.out.println();
            System.out.flush();
        }
    }


    public static void aboutCallNotification() {

        if (!CALL_debug)
            return;

        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.flush();
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.out.print(stackTrace[1] + " ->  called");
            if (stackTrace.length > 2)
                System.out.println("  (from " + stackTrace[2] + ")");
            else
                System.out.println();
            System.out.flush();
        }
    }

    public static void aboutCalledFrom() {
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.flush();
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.out.println(stackTrace[1] + "  called from   ->   " + stackTrace[2] );
            System.out.flush();
        }
    }

    public static void aboutCalledFrom(String msg) {
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.flush();
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.out.println(msg);
            System.out.println(stackTrace[1] + "  called from   ->   " + stackTrace[2] );
            System.out.flush();
        }
    }

    public static void stable_sort(Vector vector, int start, int end) {

        if (end < start) {
            if (CommonUtils.CU_DEBUG_debug) System.out.println("CU: sort start > end" );
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

    public static AlicaTime debugNowTime(){
        return new AlicaTime().inNanoseconds(TimerEvent.getCurrentTimeInNanoSec());
    }

    public static void debugPrintDiff( AlicaTime start){
        System.err.println((new AlicaTime().inNanoseconds(TimerEvent.getCurrentTimeInNanoSec()).time - start.time));
    }

    public static boolean isNumeric(String value) {
        try {
             Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }
}
