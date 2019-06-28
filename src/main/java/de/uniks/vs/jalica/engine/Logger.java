package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.teammanagement.TeamManager;
import de.uniks.vs.jalica.engine.teammanagement.TeamObserver;

import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
//TODO: implement
public class Logger {

    private AlicaEngine ae;
    private TeamObserver to;
    private TeamManager tm;
    private AlicaTime startTime;
    private AlicaTime endTime;
    private AlicaTime time;
    private FileWriter fileWriter;
    private StringBuffer sBuild;
    private ArrayList<String> eventStrings;
    private int itCount;

    private boolean active;
    private boolean receivedEvent;
    private boolean inIteration;


    public Logger(AlicaEngine alicaEngine) {
        CommonUtils.aboutNoImpl();
    }

    public void itertionStarts() {
        CommonUtils.aboutNoImpl();
    }

    public void iterationEnds(RunningPlan rootNode) {
        CommonUtils.aboutNoImpl();
    }

    public void eventOccured(String topFail) {
        CommonUtils.aboutNoImpl();
    }

    public void eventOccurred(String... args) {
        CommonUtils.aboutNoImpl();

        if (this.active) {
//            std::stringstream s;
//            detail::StringBuilder<Args...>::assembleString(s, args...);
//            processString(s.str());
        }

    }

    public void close() {}
}
