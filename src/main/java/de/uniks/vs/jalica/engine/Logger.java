package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.teammanagement.TeamManager;
import de.uniks.vs.jalica.engine.teammanagement.TeamObserver;

import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
//TODO: complete implementation

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
//        SystemConfig sc = alicaEngine.getSystemConfig();
//        boolean active = Boolean.valueOf((String) sc.get("Alica").get("Alica.EventLogging.Enabled"));
//
//        if (active) {
//            String robotName = ae.getRobotName();
//            String logPath = String.valueOf((String) sc.get("Alica").get("Alica.EventLogging.LogFolder"));
//
//            if (!essentials::FileSystem::isDirectory(logPath)) {
//                if (!essentials::FileSystem::createDirectory(logPath, 0777)) {
//                    AlicaEngine.abort("Cannot create log folder: ", logPath);
//                }
//            }
//            std::stringstream sb;
//            struct tm timestruct;
//            long time = System.currentTimeMillis();
//
//            sb + logPath + "/" + std::put_time(localtime_r(&time, &timestruct), "%Y-%Om-%Od_%OH-%OM-%OS") + "_alica-run--" + robotName + ".txt";
//            this.fileWriter.open(sb.str().c_str());
//            this.to = ae.getTeamObserver();
//            this.tm = ae.getTeamManager();
//        }
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

//    void Logger::processString(const std::string& event)
//    {
//        if (this->inIteration) {
//        this->eventStrings.push_back(event);
//    } else {
//        // add flag for fast path out-of-loop events.
//        this->eventStrings.push_back(event + "(FP)");
//    }
//        this->receivedEvent = true;
//        ALICA_DEBUG_MSG("Logger: " << this->eventStrings.back());
//    }
//
//    /**
//     * Notify the logger of a new iteration, called by the PlanBase
//     */
//    void Logger::itertionStarts()
//    {
//        this->inIteration = true;
//        this->startTime = ae->getAlicaClock()->now();
//    }
//
//    /**
//     * Notify that the current iteration is finished, triggering the Logger to write an entry if an event occurred in the
//     * current iteration. Called by the PlanBase.
//     * @param p The root RunningPlan of the plan base.
//     */
//    void Logger::iterationEnds(const RunningPlan* rp)
//    {
//        if (!_active) {
//            return;
//        }
//        this->inIteration = false;
//        this->endTime = ae->getAlicaClock()->now();
//        this->itCount++;
//        this->time += (this->endTime - this->startTime);
//
//        if (!this->receivedEvent) {
//        return;
//    }
//        this->receivedEvent = false;
//
//        _sBuild << "START:\t";
//        _sBuild << this->startTime.inMilliseconds() << endl;
//        _sBuild << "AVG-RT:\t";
//        _sBuild << (this->time.inMilliseconds() / this->itCount) << endl;
//        _sBuild << "CUR-RT:\t";
//        _sBuild << (this->endTime - this->startTime).inMilliseconds() << endl;
//        _sBuild << "REASON:";
//        for (const std::string& reason : this->eventStrings) {
//        _sBuild << "\t";
//        _sBuild << reason;
//    }
//        _sBuild << endl;
//        ActiveAgentIdView agents = tm->getActiveAgentIds();
//
//        _sBuild << "TeamSize:\t";
//        _sBuild << tm->getTeamSize();
//
//        _sBuild << " TeamMember:";
//        for (essentials::IdentifierConstPtr id : agents) {
//        _sBuild << "\t";
//        _sBuild << id;
//    }
//        _sBuild << endl;
//        if (rp) {
//            _sBuild << "LocalTree:";
//            createTreeLog(_sBuild, *rp);
//            _sBuild << endl;
//            evaluationAssignmentsToString(_sBuild, *rp);
//        }
//
//    const auto& teamPlanTrees = this->to->getTeamPlanTrees();
//        if (!teamPlanTrees.empty()) {
//            _sBuild << "OtherTrees:" << endl;
//            for (const auto& kvp : teamPlanTrees) {
//                _sBuild << "OPT:\t";
//                _sBuild << kvp.first;
//                _sBuild << "\t";
//
//                auto ids = createHumanReadablePlanTree(kvp.second->getStateIds());
//
//                for (const std::string& name : (*ids)) {
//                    _sBuild << name << "\t";
//                }
//                _sBuild << endl;
//            }
//        } else {
//            _sBuild << "NO OtherPlanTrees" << endl;
//        }
//        _sBuild << "END" << endl;
//        _fileWriter << _sBuild.str();
//        _fileWriter.flush();
//        _sBuild.str(""); // this clears the string stream
//        this->time = AlicaTime::zero();
//        this->itCount = 0;
//        this->eventStrings.clear();
//    }
//
//    /**
//     * Closes the logger.
//     */
//    void Logger::close()
//    {
//        if (_active) {
//            _active = false;
//            _fileWriter.close();
//        }
//    }
//
///**
// * Internal method to create the log string from a serialised plan.
// * @param l A list<long>
// * @return shared_ptr<list<string> >
// */
//    std::shared_ptr<std::list<std::string>> Logger::createHumanReadablePlanTree(const IdGrp& l) const
//    {
//        std::shared_ptr<std::list<std::string>> result = std::make_shared<std::list<std::string>>(std::list<std::string>());
//
//    const PlanRepository::Accessor<State>& states = ae->getPlanRepository()->getStates();
//
//    const EntryPoint* e;
//        for (int64_t id : l) {
//            if (id > 0) {
//            const State* s = states.find(id);
//                if (s) {
//                    e = entryPointOfState(s);
//                    result->push_back(e->getTask()->getName());
//                    result->push_back(s->getName());
//                }
//            } else {
//                result->push_back(to_string(id));
//            }
//        }
//
//        return result;
//    }
//
//const EntryPoint* Logger::entryPointOfState(const State* s) const
//    {
//        for (const EntryPoint* ep : s->getInPlan()->getEntryPoints()) {
//        if (ep->isStateReachable(s)) {
//            return ep;
//        }
//    }
//        return nullptr;
//    }
//
//    void Logger::evaluationAssignmentsToString(std::stringstream& ss, const RunningPlan& rp)
//    {
//        if (rp.isBehaviour()) {
//            return;
//        }
//
//        ss << rp.getAssignment();
//        for (const RunningPlan* child : rp.getChildren()) {
//        evaluationAssignmentsToString(ss, *child);
//    }
//    }
//
//    std::stringstream& Logger::createTreeLog(std::stringstream& ss, const RunningPlan& r)
//    {
//        PlanStateTriple ptz = r.getActiveTriple();
//        if (ptz.state != nullptr) {
//            if (ptz.entryPoint != nullptr) {
//                ss << ptz.entryPoint->getTask()->getName() << "\t";
//            } else {
//                ss << "-3\t"; // indicates no task
//            }
//
//            ss << ptz.state->getName();
//        } else {
//            if (r.getBasicBehaviour() != nullptr) {
//                ss << "BasicBehaviour\t";
//                ss << r.getBasicBehaviour()->getName() << "\t";
//            } else // will idle
//            {
//                ss << "IDLE\t";
//                ss << "NOSTATE\t";
//            }
//        }
//
//        if (!r.getChildren().empty()) {
//            ss << "-1\t"; // start children marker
//
//            for (const RunningPlan* rp : r.getChildren()) {
//                createTreeLog(ss, *rp);
//            }
//
//            ss << "-2\t"; // end children marker
//        }
//        return ss;
//    }
//
//    void Logger::logToConsole(const std::string& logString)
//    {
//        std::cout << "Agent " << this->ae->getTeamManager()->getLocalAgentID() << ":\t" << logString << std::endl;
//    }
}
