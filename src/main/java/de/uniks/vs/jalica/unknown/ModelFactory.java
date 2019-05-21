package de.uniks.vs.jalica.unknown;

import com.sun.tools.javac.util.Pair;
import de.uniks.vs.jalica.behaviours.Behaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.parser.PlanParser;
import de.uniks.vs.jalica.teamobserver.PlanRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Attribute;
import static de.uniks.vs.jalica.unknown.CommonUtils.stod;
import static de.uniks.vs.jalica.unknown.CommonUtils.stoi;
import static de.uniks.vs.jalica.unknown.CommonUtils.stol;
import static de.uniks.vs.jalica.unknown.PlanningType.Interactive;
import static de.uniks.vs.jalica.unknown.PlanningType.Offline;
import static de.uniks.vs.jalica.unknown.PlanningType.Online;

/*
  Created by alex on 14.07.17.
 */

public class ModelFactory {

    private static final String state = "state";
    private static final String task = "task";
    private static final String inTransitions = "inTransitions";
    private static final String outTransitions = "outTransitions";
    private static final String plans = "plans";
    private static final String parametrisation = "parametrisation";
    private static final String subvar = "subvar";
    private static final String var = "var";
    private static final String vars = "vars";
    private static final String inState = "inState";
    private static final String outState = "outState";
    private static final String preCondition = "preCondition";
    private static final String quantifiers = "quantifiers";
    private static final String synchronisation = "synchronisation";
    private static final String parameters = "parameters";
    private static final String configurations = "configurations";
    private static final String sorts = "sorts";
    private static final String mappings = "mappings";
    private static final String taskPriorities = "taskPriorities";
    private static final String role = "role";
    private static final String roles = "roles";
    private static final String characteristics = "characteristics";
    private static final String capability = "capability";
    private static final String value = "value";
    private static final String capabilities = "capabilities";
    private static final String capValues = "capValues";
    private static final String postCondition = "postCondition";
    private static final String conditions = "conditions";
    private static final String waitPlan = "waitPlan";
    private static final String alternativePlan = "alternativePlan";

    private AlicaEngine ae;
    private PlanParser parser;
    private PlanRepository rep;
    private String subplan;
    private boolean ignoreMasterPlanId;

    private LinkedHashMap<Long, AlicaElement> elements = new LinkedHashMap<>();
    private List<Pair<Long, Long>> epStateReferences = new ArrayList<>();
    private List<Pair<Long, Long>> epTaskReferences = new ArrayList<>();
    private List<Pair<Long, Long>> stateInTransitionReferences = new ArrayList<>();
    private List<Pair<Long, Long>> stateOutTransitionReferences = new ArrayList<>();
    private List<Pair<Long, Long>> statePlanReferences = new ArrayList<>();
    private List<Pair<Long, Long>> paramSubPlanReferences = new ArrayList<>();
    private List<Pair<Long, Long>> paramSubVarReferences = new ArrayList<>();
    private List<Pair<Long, Long>> paramVarReferences = new ArrayList<>();
    private List<Pair<Long, Long>> transitionAimReferences = new ArrayList<>();
    private List<Pair<Long, Long>> conditionVarReferences = new ArrayList<>();
    private List<Pair<Long, Long>> quantifierScopeReferences = new ArrayList<>();
    private List<Pair<Long, Long>> transitionSynchReferences = new ArrayList<>();
    private List<Pair<Long, Long>> planTypePlanReferences = new ArrayList<>();
    private List<Pair<Long, Long>> planningProblemPlanReferences = new ArrayList<>();
    private List<Pair<Long, Long>> planningProblemPlanWaitReferences = new ArrayList<>();
    private List<Pair<Long, Long>> planningProblemPlanAlternativeReferences = new ArrayList<>();
    private List<Pair<Long, Long>> rtmRoleReferences = new ArrayList<>();
    private List<Pair<Long, Long>> charCapReferences = new ArrayList<>();
    private List<Pair<Long, Long>> charCapValReferences = new ArrayList<>();



    public ModelFactory(AlicaEngine ae, PlanParser parser, PlanRepository rep) {
        this.ae = ae;
        this.parser = parser;
        this.rep = rep;
    }

    public void computeReachabilities() {
        if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Computing Reachability sets...");

        for (Plan plan : this.rep.getPlans().values()) {
            for (EntryPoint entryPoint : plan.getEntryPoints().values()) {
                entryPoint.computeReachabilitySet();
            }
        }
        if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Computing Reachability sets...done!");

    }

    public void attachRoleReferences() {
        if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Attaching Role references..." );
        for (Pair<Long, Long> pairs : this.rtmRoleReferences) {
            Role  r = this.rep.getRoles().get(pairs.snd);//find(pairs.second).second;
            RoleTaskMapping rtm = (RoleTaskMapping) this.elements.get(pairs.fst);//find(pairs.first).second;
            r.setRoleTaskMapping(rtm);
            rtm.setRole(r);
        }
        this.rtmRoleReferences.clear();
        if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Attaching Role references... done!" );
    }

//    public void attachPlanReferences() {
////        #ifdef MF_DEBUG
//        System.out.println("MF: Attaching Plan references..");
////#endif
//        //epTaskReferences
//        for (Pair<Long, Long> pairs : this.epTaskReferences) {
//            Task t = (Task) this.elements.get(pairs.snd);
//            EntryPoint ep = (EntryPoint) this.elements.get(pairs.fst);
//            ep.setTask(t);
//        }
//        this.epTaskReferences.clear();
//
//        //transitionAimReferences
//        for (Pair<Long, Long> pairs : this.transitionAimReferences) {
//            Transition t = (Transition) this.elements.get(pairs.fst);
//            State st = (State) this.elements.get(pairs.snd);
//            if (st == null) {
//                ae.abort("MF: Cannot resolve transitionAimReferences target: ", "" + pairs.fst);
//            }
//            t.setOutState(st);
//            st.getInTransitions().add(t);
//        }
//        this.transitionAimReferences.clear();
//
//        //epStateReferences
//        for (Pair<Long, Long> pairs : this.epStateReferences) {
//            State st = (State) this.elements.get(pairs.snd);
//            EntryPoint ep = (EntryPoint) this.elements.get(pairs.fst);
//            ep.setState(st);
//            st.setEntryPoint(ep);
//        }
//        this.epStateReferences.clear();
//
//        //stateInTransitionReferences
//        for (Pair<Long, Long> pairs : this.stateInTransitionReferences) {
//            Transition t = (Transition) this.elements.get(pairs.snd);
//            State st = (State) this.elements.get(pairs.fst);
//            if (st != t.getOutState()) {
//                ae.abort("MF: Unexpected reference in a transition! ", "" + pairs.fst);
//            }
//        }
//        this.stateInTransitionReferences.clear();
//
//        //stateOutTransitionReferences
//        for (Pair<Long, Long> pairs : this.stateOutTransitionReferences) {
//            State st = (State) this.elements.get(pairs.fst);
//            Transition t = (Transition) this.elements.get(pairs.snd);
//            st.getOutTransitions().add(t);
//            t.setInState(st);
//        }
//        this.stateOutTransitionReferences.clear();
//
//        //statePlanReferences
//        for (Pair<Long, Long> pairs : this.statePlanReferences) {
//            State st = (State) this.elements.get(pairs.fst);
//            AbstractPlan p = (AbstractPlan) this.elements.get(pairs.snd);
//            st.getPlans().add(p);
//        }
//        this.statePlanReferences.clear();
//
//        //planTypePlanReferences
//        for (Pair<Long, Long> pairs : this.planTypePlanReferences) {
//            PlanType pt = (PlanType) this.elements.get(pairs.fst);
//            Plan p = (Plan) this.elements.get(pairs.snd);
//            pt.getPlans().add(p);
//        }
//        this.planTypePlanReferences.clear();
//
//        //conditionVarReferences
//        for (Pair<Long, Long> pairs : this.conditionVarReferences) {
//            Condition c = (Condition) this.elements.get(pairs.fst);
//            Variable v = (Variable) this.elements.get(pairs.snd);
//            c.getVariables().add(v);
//        }
//        this.conditionVarReferences.clear();
//
//        //paramSubPlanReferences
//        for (Pair<Long, Long> pairs : this.paramSubPlanReferences) {
//            Parametrisation p = (Parametrisation) this.elements.get(pairs.fst);
//            AbstractPlan ap = (AbstractPlan) this.elements.get(pairs.snd);
//            p.setSubPlan(ap);
//        }
//        this.paramSubPlanReferences.clear();
//
//        //paramSubVarReferences
//        for (Pair<Long, Long> pairs : this.paramSubVarReferences) {
//            Parametrisation p = (Parametrisation) this.elements.get(pairs.fst);
//            Variable ap = (Variable) this.elements.get(pairs.snd);
//            p.setSubVar(ap);
//        }
//        this.paramSubVarReferences.clear();
//
//        //paramVarReferences
//        for (Pair<Long, Long> pairs : this.paramVarReferences) {
//            Parametrisation p = (Parametrisation) this.elements.get(pairs.fst);
//            Variable v = (Variable) this.elements.get(pairs.snd);
//            p.setVar(v);
//        }
//        this.paramVarReferences.clear();
//
//        //transitionSynchReferences
//        for (Pair<Long, Long> pairs : this.transitionSynchReferences) {
//            Transition t = (Transition) this.elements.get(pairs.fst);
//            SyncTransition sync = (SyncTransition) this.elements.get(pairs.snd);
//            t.setSyncTransition(sync);
//            sync.getInSync().add(t);
//        }
//        this.transitionSynchReferences.clear();
//
//        //planningProblemPlanReferences
//        for (Pair<Long, Long> pairs : this.planningProblemPlanReferences) {
//            PlanningProblem s = (PlanningProblem) this.elements.get(pairs.fst);
//            AbstractPlan p = (AbstractPlan) this.elements.get(pairs.snd);
//            s.getPlans().add(p);
//        }
//        this.planningProblemPlanReferences.clear();
//
//        //planningProblemPlanWaitReferences
//        for (Pair<Long, Long> pairs : this.planningProblemPlanWaitReferences) {
//            PlanningProblem s = (PlanningProblem) this.elements.get(pairs.fst);
//            Plan p = (Plan) this.elements.get(pairs.snd);
//            s.setWaitPlan(p);
//        }
//        this.planningProblemPlanWaitReferences.clear();
//
//        //planningProblemPlanAlternativeReferences
//        for (Pair<Long, Long> pairs : this.planningProblemPlanAlternativeReferences) {
//            PlanningProblem s = (PlanningProblem) this.elements.get(pairs.fst);
//            Plan p = (Plan) this.elements.get(pairs.snd);
//            s.setAlternativePlan(p);
//        }
//        this.planningProblemPlanAlternativeReferences.clear();
//
//        //quantifierScopeReferences
//        for (Pair<Long, Long> pairs : this.quantifierScopeReferences) {
//            AlicaElement ae = (AlicaElement) this.elements.get(pairs.snd);
//            Quantifier q = (Quantifier) this.elements.get(pairs.fst);
//            q.setScope(this.ae, ae);
//        }
//        this.quantifierScopeReferences.clear();
//
//        removeRedundancy();
////#ifdef MF_DEBUG
//        System.out.println("MF: DONE!");
////#endif
//
//    }


    public void attachCharacteristicReferences() {
        {
            if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Attaching Characteristics references...");

            for (Pair<Long, Long> pairs : this.charCapReferences) {
                Characteristic cha = this.rep.getCharacteristics().get(pairs.fst);
                Capability cap = (Capability) this.elements.get(pairs.snd);
                cha.setCapability(cap);
            }
            this.charCapReferences.clear();

            for (Pair<Long, Long> pairs : this.charCapValReferences) {
                Characteristic cha = this.rep.getCharacteristics().get(pairs.fst);
                CapValue capVal = (CapValue) this.elements.get(pairs.snd);
                cha.setCapValue(capVal);
            }
            this.charCapValReferences.clear();

            if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Attaching Characteristics references... done!");
        }
    }

    public Plan createPlan(Document doc) {
        Node element = doc.getDocumentElement();
        long id = this.parser.parserId(element);
        Plan plan = new Plan(id, ae);
        plan.setFileName(this.parser.getCurrentFile());
        setAlicaElementAttributes(plan, element);
        // insert into elements ma
        addElement(plan);
        // insert into plan repository map
        this.rep.getPlans().put(plan.getID(), plan);
//        Node curChild = element.getFirstChild();

//        //TODO: move to parser
//        Vector<Element> nodes = extractToList(element, plans);
//        nodes.addAll(extractToList(element,inTransitions));
//        nodes.addAll(extractToList(element,outTransitions));
//        nodes.addAll(extractToList(element,parametrisation));

        Node curChild = element.getFirstChild().getNextSibling();

        while (curChild != null) {
            if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Nodename " +curChild.getNodeName());

            parser.handleTag(curChild, plan, this);
            curChild = curChild.getNextSibling();
        }
        return plan;
    }

    public Plan createPlan(JSONObject jsonObject) {
//        long id = (long) jsonObject.get("id");
        long id = this.parser.fetchId(jsonObject.get("id").toString());
        Plan plan = new Plan(id, ae);
        plan.setFileName(this.parser.getCurrentFile());
        setAlicaElementAttributes(plan, jsonObject);
        // insert into elements ma
        addElement(plan);
        // insert into plan repository map
        this.rep.getPlans().put(plan.getID(), plan);

        //TODO: move to parser
        for (Object entry: jsonObject.entrySet() ) {
            if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Entry " + ((HashMap.Entry)entry).getKey());
            parser.handleEntry(entry, plan, this);
        }
        return plan;
    }

    public SyncTransition createSyncTransition(Node element) {
        SyncTransition s = new SyncTransition();
        s.setID(this.parser.parserId(element));
        setAlicaElementAttributes(s, element);
        String talkTimeoutPtr = element.getAttributes().getNamedItem("talkTimeout").getTextContent();
        if (talkTimeoutPtr != null) {
            s.setTalkTimeOut(stol(talkTimeoutPtr));
        }
        String syncTimeoutPtr = element.getAttributes().getNamedItem("syncTimeout").getTextContent();
        if (syncTimeoutPtr != null) {
            s.setSyncTimeOut(stol(syncTimeoutPtr));
        }

        addElement(s);
        this.rep.getSyncTransitions().put(s.getID(), s);
        if (element.getFirstChild() != null) {
            ae.abort("MF: Unhandled Synchtransition Child:", element.getFirstChild().toString());
        }
        return s;
    }

    public SyncTransition createSyncTransition(JSONObject jsonObject) {
        SyncTransition s = new SyncTransition();
        long id = this.parser.fetchId(jsonObject.get("id").toString());
        s.setID(id);
        setAlicaElementAttributes(s, jsonObject);
        String talkTimeoutPtr = jsonObject.get("talkTimeout").toString();

        if (talkTimeoutPtr != null) {
            s.setTalkTimeOut(stol(talkTimeoutPtr));
        }
        String syncTimeoutPtr = jsonObject.get("syncTimeout").toString();

        if (syncTimeoutPtr != null) {
            s.setSyncTimeOut(stol(syncTimeoutPtr));
        }
        addElement(s);
        this.rep.getSyncTransitions().put(s.getID(), s);

//        if (element.getFirstChild() != null) {
//            ae.abort("MF: Unhandled Synchtransition Child:", element.getFirstChild().toString());
//        }
        return s;
    }

    public Variable createVariable(Node element) {
        String type = "";
        String conditionPtr = element.getAttributes().getNamedItem("Type").getTextContent();
        if (conditionPtr != null) {
            type = conditionPtr;
        }
        String name = "";
        String namePtr = element.getAttributes().getNamedItem("name").getTextContent();
        if (namePtr != null) {
            name = namePtr;
        }
        Variable v = new Variable(this.parser.parserId(element), name, type);
        setAlicaElementAttributes(v, element);
        addElement(v);
        this.rep.getVariables().put(v.getID(), v);
        return v;
    }

    public Variable createVariable(JSONObject jsonObject) {
        String type = "";
        String conditionPtr = jsonObject.get("Type").toString();

        if (conditionPtr != null) {
            type = conditionPtr;
        }
        String name = "";
        String namePtr = jsonObject.get("name").toString();

        if (namePtr != null) {
            name = namePtr;
        }
        long id =  this.parser.fetchId(jsonObject.get("id").toString());
        Variable v = new Variable(id, name, type);
        setAlicaElementAttributes(v, jsonObject);
        addElement(v);
        this.rep.getVariables().put(v.getID(), v);
        return v;
    }

    public RuntimeCondition createRuntimeCondition(Node element) {
        RuntimeCondition r = new RuntimeCondition();
        r.setID(this.parser.parserId(element));
        setAlicaElementAttributes(r, element);
        addElement(r);

        String conditionString = "";
        String conditionPtr = element.getAttributes().getNamedItem("conditionString").getTextContent();
        if (conditionPtr != null) {
            conditionString = conditionPtr;
            r.setConditionString(conditionString);
        }

        if (!conditionString.isEmpty()) {
            //TODO: ANTLRBUILDER
        } else {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

        String pluginNamePtr = element.getAttributes().getNamedItem("pluginName").getTextContent();

        if (pluginNamePtr != null) {
            r.setPlugInName(pluginNamePtr);
        }
        Node curChild = element.getFirstChild();

        while (curChild != null) {
            // TODO: FIXME skip #text (extract to method)
            if ("#text".equals(curChild.getNodeName()))
                curChild = curChild.getNextSibling();
            else {
                String val = curChild.getNodeName();
                long cid = this.parser.parserId(curChild);
                if (vars.equals(val)) {
                    this.conditionVarReferences.add(new Pair(r.getID(), cid));
                } else if (quantifiers.equals(val)) {
                    Quantifier q = createQuantifier(curChild);
                    r.getQuantifiers().add(q);
                } else if (parameters.equals(val)) {
                    Parameter p = createParameter(curChild);
                    r.getParameters().add(p);
                } else {
                    ae.abort("MF: Unhandled RuntimeCondition Child", curChild.toString());
                }
                curChild = curChild.getNextSibling();
            }
        }
        return r;
    }

    public RuntimeCondition createRuntimeCondition(JSONObject jsonObject) {
        RuntimeCondition runtimeCondition = new RuntimeCondition();
        long id = this.parser.fetchId(jsonObject.get("id").toString());
        runtimeCondition.setID(id);
        setAlicaElementAttributes(runtimeCondition, jsonObject);
        addElement(runtimeCondition);

        String conditionString = "";
        String conditionPtr = jsonObject.get("conditionString").toString();
        if (conditionPtr != null) {
            conditionString = conditionPtr;
            runtimeCondition.setConditionString(conditionString);
        }

        if (!conditionString.isEmpty()) {
            //TODO: ANTLRBUILDER
        } else {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }
        String pluginNamePtr = jsonObject.get("pluginName").toString();

        if (pluginNamePtr != null) {
            runtimeCondition.setPlugInName(pluginNamePtr);
        }
//        Node curChild = element.getFirstChild();
//
//        while (curChild != null) {
//            // TODO: FIXME skip #text (extract to method)
//            if ("#text".equals(curChild.getNodeName()))
//                curChild = curChild.getNextSibling();
//            else {
//                String val = curChild.getNodeName();
//                long cid = this.parser.parserId(curChild);
//                if (vars.equals(val)) {
//                    this.conditionVarReferences.add(new Pair(runtimeCondition.getID(), cid));
//                } else if (quantifiers.equals(val)) {
//                    Quantifier q = createQuantifier(curChild);
//                    runtimeCondition.getQuantifiers().add(q);
//                } else if (parameters.equals(val)) {
//                    Parameter p = createParameter(curChild);
//                    runtimeCondition.getParameters().add(p);
//                } else {
//                    ae.abort("MF: Unhandled RuntimeCondition Child", curChild.toString());
//                }
//                curChild = curChild.getNextSibling();
//            }
//        }
        return runtimeCondition;
    }

    public Transition createTransition(Node element, Plan plan) {
        Transition tran = new Transition();
        tran.setID(this.parser.parserId(element));
        setAlicaElementAttributes(tran, element);
        addElement(tran);
        this.rep.getTransitions().put(tran.getID(), tran);
//        Node curChild = element.getFirstChild();

        //        //TODO: move to parser
        Vector<Element> nodes = extractToList(element, outState);
        nodes.addAll(extractToList(element, inState));
        nodes.addAll(extractToList(element, preCondition));
        nodes.addAll(extractToList(element, synchronisation));

//        while (curChild != null)
        for (Element curChild : nodes) {
//			String val = curChild.getNodeValue();
            String val = curChild.getTagName();
            long cid = this.parser.parserId(curChild);
            if (inState.equals(val)) {
                //silently ignore
            } else if (outState.equals(val)) {
                this.transitionAimReferences.add(new Pair(tran.getID(), cid));
            } else if (preCondition.equals(val)) {
                PreCondition pre = createPreCondition(curChild);
                tran.setPreCondition(pre);
                pre.setAbstractPlan(plan);
            } else if (synchronisation.equals(val)) {
                this.transitionSynchReferences.add(new Pair(tran.getID(), cid));
            } else {
                ae.abort("MF: Unhandled Transition Child:", curChild.toString());
            }
//            curChild = curChild.getNextSibling();
        }
        return tran;
    }

    public Transition createTransition(JSONObject jsonObject, Plan plan) {
        Transition transition = new Transition();
        long id = this.parser.fetchId(jsonObject.get("id").toString());
        transition.setID(id);
        setAlicaElementAttributes(transition, jsonObject);
        addElement(transition);
        this.rep.getTransitions().put(transition.getID(), transition);

        long outStateID = (long) jsonObject.get("outState");
        this.transitionAimReferences.add(new Pair(transition.getID(), outStateID));

        Object preConditionJSON = jsonObject.get("preCondition");

        if ( preConditionJSON != null) {
            PreCondition preCondition = createPreCondition((JSONObject) preConditionJSON);
            transition.setPreCondition(preCondition);
            preCondition.setAbstractPlan(plan);
        }

        Object synchronisation = jsonObject.get("synchronisation");

        if(synchronisation != null)
            this.transitionSynchReferences.add(new Pair(transition.getID(), (long)synchronisation));
        return transition;
    }

    public PreCondition createPreCondition(Node element) {
        PreCondition pre = new PreCondition();
        pre.setID(this.parser.parserId(element));
        setAlicaElementAttributes(pre, element);
        addElement(pre);
        String conditionString = "";
        String conditionPtr = element.getAttributes().getNamedItem("conditionString").getTextContent();
        if (conditionPtr != null) {
            conditionString = conditionPtr;
            pre.setConditionString(conditionString);
        }

        if (!conditionString.isEmpty()) {
            //TODO: ANTLRBUILDER
        } else {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

        String pluginNamePtr = element.getAttributes().getNamedItem("pluginName").getTextContent();
        if (pluginNamePtr != null) {
            pre.setPlugInName(pluginNamePtr);
        }

        String enabled = "";
        String enabledPtr = element.getAttributes().getNamedItem("enabled").getTextContent();
        if (enabledPtr != null) {
            enabled = enabledPtr;
            if ("true".equals(enabled)) {
                pre.setEnabled(true);
            } else {
                pre.setEnabled(false);
            }
        } else {
            pre.setEnabled(true);
        }
        Node curChild = element.getFirstChild();
        while (curChild != null) {
            // TODO: FIXME skip #text (extract to method)
            if ("#text".equals(curChild.getNodeName()))
                curChild = curChild.getNextSibling();
            else {
                String val = curChild.getNodeName();
                long cid = this.parser.parserId(curChild);
                if (vars.equals(val)) {
                    this.conditionVarReferences.add(new Pair(pre.getID(), cid));
                } else if (quantifiers.equals(val)) {
                    Quantifier q = createQuantifier(curChild);
                    pre.getQuantifiers().add(q);
                } else if (parameters.equals(val)) {
                    Parameter p = createParameter(curChild);
                    pre.getParameters().add(p);
                } else {
                    ae.abort("MF: Unhandled PreCondition Child:", curChild.getNodeValue());
                }
                curChild = curChild.getNextSibling();
            }
        }
        return pre;
    }
    public PreCondition createPreCondition(JSONObject jsonObject) {
        PreCondition pre = new PreCondition();
        long id =  this.parser.fetchId(jsonObject.get("id").toString());
        pre.setID(id);
        setAlicaElementAttributes(pre, jsonObject);
        addElement(pre);
        String conditionString = "";
        Object conditionObj = jsonObject.get("conditionString");

        if (conditionObj != null) {
            conditionString = conditionObj.toString();
            pre.setConditionString(conditionString);
        }

        if (!conditionString.isEmpty()) {
            //TODO: ANTLRBUILDER
        } else {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

        Object pluginNameObj = jsonObject.get("pluginName");

        if (pluginNameObj != null) {
            pre.setPlugInName(pluginNameObj.toString());
        }

        String enabled = "";
        Object enabledObj = jsonObject.get("enabled");

        if (enabledObj != null) {
            enabled = enabledObj.toString();

            if ("true".equals(enabled)) {
                pre.setEnabled(true);
            } else {
                pre.setEnabled(false);
            }
        } else {
            pre.setEnabled(true);
        }
//        Node curChild = element.getFirstChild();
//        while (curChild != null) {
//            // TODO: FIXME skip #text (extract to method)
//            if ("#text".equals(curChild.getNodeName()))
//                curChild = curChild.getNextSibling();
//            else {
//                String val = curChild.getNodeName();
//                long cid = this.parser.parserId(curChild);
//                if (vars.equals(val)) {
//                    this.conditionVarReferences.add(new Pair(pre.getID(), cid));
//                } else if (quantifiers.equals(val)) {
//                    Quantifier q = createQuantifier(curChild);
//                    pre.getQuantifiers().add(q);
//                } else if (parameters.equals(val)) {
//                    Parameter p = createParameter(curChild);
//                    pre.getParameters().add(p);
//                } else {
//                    ae.abort("MF: Unhandled PreCondition Child:", curChild.getNodeValue());
//                }
//                curChild = curChild.getNextSibling();
//            }
//        }
        return pre;
    }

    private Parameter createParameter(Node element) {
        Parameter p = new Parameter();
        long id = this.parser.parserId(element);
        p.setID(id);
        addElement(p);
        setAlicaElementAttributes(p, element);
        String key = element.getAttributes().getNamedItem("key").getTextContent();
        String value = element.getAttributes().getNamedItem("value").getTextContent();
        p.setKey(key);
        p.setValue(value);
        return p;
    }

    private Quantifier createQuantifier(Node element) {
        Quantifier q = null;
        long id = this.parser.parserId(element);

        String typeString = "";
        String typePtr = element.getAttributes().getNamedItem("xsi:type").getTextContent();
        if (typePtr != null) {
            typeString = typePtr;
            if ("alica:ForallAgents".equals(typeString)) {
                q = new ForallAgents(this.ae);
                q.setID(id);
            } else {
                ae.abort("MF: Unsupported quantifier type! !", typeString);
            }
        } else {
            ae.abort("MF: Quantifier without type!", String.valueOf(id));
        }

        addElement(q);
        this.rep.getQuantifiers().put(q.getID(), q);
        setAlicaElementAttributes(q, element);

        String scopePtr = element.getAttributes().getNamedItem("scope").getTextContent();
        long cid;

        if (scopePtr != null) {
            cid = stol(scopePtr);
            this.quantifierScopeReferences.add(new Pair(q.getID(), cid));
        }
        Node curChild = element.getFirstChild();

        while (curChild != null) {
            // TODO: FIXME skip #text (extract to method)
            if ("#text".equals(curChild.getNodeName()))
                curChild = curChild.getNextSibling();
            else {
                String val = curChild.getNodeName();

                if (sorts.equals(val)) {
                    q.getDomainIdentifiers().add(curChild.getFirstChild().getNodeValue());
                } else {
                    ae.abort("MF: Unhandled Quantifier Child:", curChild.toString());
                }
                curChild = curChild.getNextSibling();
            }
        }
        return q;
    }

    public FailureState createFailureState(Node element) {
        FailureState fail = new FailureState();
        fail.setID(this.parser.parserId(element));
        setAlicaElementAttributes(fail, element);
        addElement(fail);
        this.rep.getStates().put(fail.getID(), fail);
        Node curChild = element.getFirstChild();

        while (curChild != null) {
            // TODO: FIXME skip #text (extract to method)
            if ("#text".equals(curChild.getNodeName()))
                curChild = curChild.getNextSibling();
            else {
                String val = curChild.getNodeName();
                long cid = this.parser.parserId(curChild);

                if (inTransitions.equals(val)) {
                    this.stateInTransitionReferences.add(new Pair(fail.getID(), cid));
                } else if (postCondition.equals(val)) {
                    PostCondition postCon = createPostCondition(curChild);
                    fail.setPostCondition(postCon);
                } else {
                    ae.abort("MF: Unhandled FaulireState Child: ", curChild.getNodeValue());
                }

                curChild = curChild.getNextSibling();
            }
        }
        return fail;
    }
    public FailureState createFailureState(JSONObject jsonObject) {
        FailureState state = new FailureState();
        long id =  this.parser.fetchId(jsonObject.get("id").toString());
        state.setID(id);
        setAlicaElementAttributes(state, jsonObject);
        addElement(state);
        this.rep.getStates().put(state.getID(), state);

        JSONArray inTransitions = (JSONArray) jsonObject.get("inTransitions");

        for (Object obj : inTransitions) {
            long objID = this.parser.fetchId(obj.toString());
            this.stateInTransitionReferences.add(new Pair(state.getID(), objID));
        }

        JSONObject postConditionJSONObj = (JSONObject) jsonObject.get("postCondition");
        PostCondition postCon = createPostCondition(postConditionJSONObj);
        state.setPostCondition(postCon);
//
//        while (curChild != null) {
//            // TODO: FIXME skip #text (extract to method)
//            if ("#text".equals(curChild.getNodeName()))
//                curChild = curChild.getNextSibling();
//            else {
//                String val = curChild.getNodeName();
//                long cid = this.parser.parserId(curChild);
//
//                if (inTransitions.equals(val)) {
//                    this.stateInTransitionReferences.add(new Pair(fail.getID(), cid));
//                } else if (postCondition.equals(val)) {
//                    PostCondition postCon = createPostCondition(curChild);
//                    fail.setPostCondition(postCon);
//                } else {
//                    ae.abort("MF: Unhandled FaulireState Child: ", curChild.getNodeValue());
//                }
//
//                curChild = curChild.getNextSibling();
//            }
//        }
        return state;
    }

    public SuccessState createSuccessState(Node element) {
        SuccessState suc = new SuccessState();
        suc.setID(this.parser.parserId(element));
        setAlicaElementAttributes(suc, element);
        addElement(suc);
        this.rep.getStates().put(suc.getID(), suc);
        Node curChild = element.getFirstChild();

        while (curChild != null) {
            // TODO: FIXME skip #text (extract to method)
            if ("#text".equals(curChild.getNodeName()))
                curChild = curChild.getNextSibling();
            else {
                String val = curChild.getNodeName();
                long cid = this.parser.parserId(curChild);

                if (inTransitions.equals(val)) {
                    this.stateInTransitionReferences.add(new Pair(suc.getID(), cid));
                } else if (postCondition.equals(val)) {
                    PostCondition postCon = createPostCondition(curChild);
                    suc.setPostCondition(postCon);
                } else {
                    ae.abort("MF: Unhandled SuccesState Child:", curChild.getNodeValue());
                }
                curChild = curChild.getNextSibling();
            }
        }
        return suc;
    }

    public SuccessState createSuccessState(JSONObject jsonObject) {
        SuccessState state = new SuccessState();
        long id =  this.parser.fetchId(jsonObject.get("id").toString());
        state.setID(id);
        setAlicaElementAttributes(state, jsonObject);
        addElement(state);
        this.rep.getStates().put(state.getID(), state);

        JSONArray inTransitions = (JSONArray) jsonObject.get("inTransitions");

        for (Object obj : inTransitions) {
            long objID = this.parser.fetchId(obj.toString());
            this.stateInTransitionReferences.add(new Pair(state.getID(), objID));
        }

        JSONObject postConditionJSONObj = (JSONObject) jsonObject.get("postCondition");
        PostCondition postCon = createPostCondition(postConditionJSONObj);
        state.setPostCondition(postCon);
//        Node curChild = element.getFirstChild();
//
//        while (curChild != null) {
//            // TODO: FIXME skip #text (extract to method)
//            if ("#text".equals(curChild.getNodeName()))
//                curChild = curChild.getNextSibling();
//            else {
//                String val = curChild.getNodeName();
//                long cid = this.parser.parserId(curChild);
//
//                if (inTransitions.equals(val)) {
//                    this.stateInTransitionReferences.add(new Pair(suc.getID(), cid));
//                } else if (postCondition.equals(val)) {
//                    PostCondition postCon = createPostCondition(curChild);
//                    suc.setPostCondition(postCon);
//                } else {
//                    ae.abort("MF: Unhandled SuccesState Child:", curChild.getNodeValue());
//                }
//                curChild = curChild.getNextSibling();
//            }
//        }
        return state;
    }

    public PostCondition createPostCondition(Node element) {
        PostCondition pos = new PostCondition();
        pos.setID(this.parser.parserId(element));
        setAlicaElementAttributes(pos, element);
        addElement(pos);

        String conditionString = "";
        String conditionPtr = element.getAttributes().getNamedItem("conditionString").getTextContent();
        if (conditionPtr != null) {
            conditionString = conditionPtr;
            pos.setConditionString(conditionString);
        }
        if (!conditionString.isEmpty()) {
            //TODO: ANTLRBUILDER
        } else {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

        String pluginNamePtr = element.getAttributes().getNamedItem("pluginName").getTextContent();
        if (pluginNamePtr != null) {
            pos.setPlugInName(pluginNamePtr);
        }

        if (element.getFirstChild() != null) {
            ae.abort("MF: Unhandled Result child", element.getFirstChild().toString());
        }

        return pos;
    }

    public PostCondition createPostCondition(JSONObject jsonObject) {
        PostCondition pos = new PostCondition();
        long id =  this.parser.fetchId(jsonObject.get("id").toString());
        pos.setID(id);
        setAlicaElementAttributes(pos, jsonObject);
        addElement(pos);

        String conditionString = "";
        String conditionPtr = jsonObject.get("conditionString").toString();
        if (conditionPtr != null) {
            conditionString = conditionPtr;
            pos.setConditionString(conditionString);
        }
        if (!conditionString.isEmpty()) {
            //TODO: ANTLRBUILDER
        } else {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

        String pluginNamePtr = jsonObject.get("pluginName").toString();
        if (pluginNamePtr != null) {
            pos.setPlugInName(pluginNamePtr);
        }

//        if (element.getFirstChild() != null) {
//            ae.abort("MF: Unhandled Result child", element.getFirstChild().toString());
//        }

        return pos;
    }

    public State createState(Node element) {
        State s = new State();
        s.setID(this.parser.parserId(element));
        setAlicaElementAttributes(s, element);

        addElement(s);
        this.rep.getStates().put(s.getID(), s);

        //TODO: move to parser
        Vector<Element> nodes = extractToList(element, plans);
        nodes.addAll(extractToList(element, inTransitions));
        nodes.addAll(extractToList(element, outTransitions));
        nodes.addAll(extractToList(element, parametrisation));

//        Node curChild = element.getFirstChild();
//        while (curChild != null)
        for (Element curChild : nodes) {
            String val = curChild.getTagName();
            long cid = this.parser.parserId(curChild);

            if (inTransitions.equals(val)) {
                this.stateInTransitionReferences.add(new Pair(s.getID(), cid));
            } else if (outTransitions.equals(val)) {
                this.stateOutTransitionReferences.add(new Pair(s.getID(), cid));
            } else if (plans.equals(val)) {
                this.statePlanReferences.add(new Pair(s.getID(), cid));
            } else if (parametrisation.equals(val)) {
                Parametrisation para = createParametrisation(curChild);
                s.getParametrisation().add(para);
            } else {
                ae.abort("MF: Unhandled State Child: ", val);
            }

//            curChild = curChild.getNextSibling();
        }
        return s;
    }

    public State createState(JSONObject jsonObject) {
        State state = new State();
        long id =  this.parser.fetchId(jsonObject.get("id").toString());
        state.setID(id);
        setAlicaElementAttributes(state, jsonObject);

        addElement(state);
        this.rep.getStates().put(state.getID(), state);

        JSONArray inTransitions = (JSONArray) jsonObject.get("inTransitions");

        for (Object obj : inTransitions) {
            long objID = this.parser.fetchId(obj.toString());
            this.stateInTransitionReferences.add(new Pair(state.getID(), objID));
        }
        JSONArray outTransitions = (JSONArray) jsonObject.get("outTransitions");

        for (Object obj : outTransitions) {
            long objID = this.parser.fetchId(obj.toString());
            this.stateOutTransitionReferences.add(new Pair(state.getID(), objID));
        }
        JSONArray planObjects = (JSONArray) jsonObject.get("abstractPlans");

        for (Object obj : planObjects) {
            long objID = this.parser.fetchId(obj.toString());
            this.statePlanReferences.add(new Pair(state.getID(), objID));
        }
        JSONArray variableBindings = (JSONArray) jsonObject.get("variableBindings");

        for (Object obj : variableBindings) {
            long objID = this.parser.fetchId(obj.toString());
            this.stateInTransitionReferences.add(new Pair(state.getID(), objID));
            Parametrisation para = createParametrisation(((JSONObject)obj));
            state.getParametrisation().add(para);
        }
        return state;
    }

    //  TODO: move to parser
    private Vector extractToList(Node element, String tagName) {
        NodeList nodes = ((Element) element).getElementsByTagName(tagName);
        Vector newNodes = new Vector();

        for (int i = 0; i < nodes.getLength(); i++) {
            newNodes.add(nodes.item(i));
        }

        return newNodes;
    }

    private Parametrisation createParametrisation(Node element) {
        Parametrisation para = new Parametrisation();
        para.setID(this.parser.parserId(element));
        setAlicaElementAttributes(para, element);

        addElement(para);
        Node curChild = element.getFirstChild();
        while (curChild != null) {
            String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);

            if (subplan.equals(val)) {
                this.paramSubPlanReferences.add(new Pair(para.getID(), cid));
            } else if (subvar.equals(val)) {
                this.paramSubVarReferences.add(new Pair(para.getID(), cid));
            } else if (var.equals(val)) {
                this.paramVarReferences.add(new Pair(para.getID(), cid));
            } else {
                ae.abort("MF: Unhandled Parametrisation Child:", curChild.toString());
            }

            curChild = curChild.getNextSibling();
        }
        return para;
    }

    private Parametrisation createParametrisation(JSONObject jsonObject) {
        Parametrisation para = new Parametrisation();
        long id = this.parser.fetchId(jsonObject.get("id").toString());
        para.setID(id);
        setAlicaElementAttributes(para, jsonObject);

        addElement(para);
//        Node curChild = element.getFirstChild();
//
//        while (curChild != null) {
//            String val = curChild.getNodeValue();
//            long cid = this.parser.parserId(curChild);
//
//            if (subplan.equals(val)) {
//                this.paramSubPlanReferences.add(new Pair(para.getID(), cid));
//            } else if (subvar.equals(val)) {
//                this.paramSubVarReferences.add(new Pair(para.getID(), cid));
//            } else if (var.equals(val)) {
//                this.paramVarReferences.add(new Pair(para.getID(), cid));
//            } else {
//                ae.abort("MF: Unhandled Parametrisation Child:", curChild.toString());
//            }
//
//            curChild = curChild.getNextSibling();
//        }
        return para;
    }

    public EntryPoint createEntryPoint(Node element) {
        EntryPoint ep = new EntryPoint();
        ep.setID(this.parser.parserId(element));
        setAlicaElementAttributes(ep, element);
        String attr = element.getAttributes().getNamedItem("minCardinality").getTextContent();
        if (!attr.isEmpty()) {
            ep.setMinCardinality(stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("maxCardinality").getTextContent();
        if (!attr.isEmpty()) {
            ep.setMaxCardinality(stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("successRequired").getTextContent();
        if (!attr.isEmpty()) {
//            transform(attr.begin(), attr.end(), attr.begin(), ::tolower);
            attr = attr.toLowerCase();
            ep.setSuccessRequired("true".equals(attr));
        }

        addElement(ep);
        this.rep.getEntryPoints().put(ep.getID(), ep);
//        Node curChild = element.getFirstChild();
        boolean haveState = false;
        long curChildId;

        //TODO: move to parser
        Vector<Element> nodes = extractToList(element, state);
        nodes.addAll(extractToList(element, task));

//        while (curChild != null)
        for (Element curChild : nodes) {
            String val = curChild.getTagName();
            curChildId = this.parser.parserId(curChild);

            if (state.equals(val)) {
                this.epStateReferences.add(new Pair(ep.getID(), curChildId));
                haveState = true;
            } else if (task.equals(val)) {
                this.epTaskReferences.add(new Pair(ep.getID(), curChildId));
            } else {
                ae.abort("MF: Unhandled EntryPoint Child: ", val);
            }
//            curChild = curChild.getNextSibling();
        }

        if (!haveState) {
            ae.abort("MF: No initial state identified for EntryPoint: ", String.valueOf(ep.getID()));
        }

        return ep;
    }

    public EntryPoint createEntryPoint(JSONObject jsonObject) {
        EntryPoint ep = new EntryPoint();
        long id = this.parser.fetchId(jsonObject.get("id").toString());
        ep.setID(id);
        setAlicaElementAttributes(ep, jsonObject);
        String attr = jsonObject.get("minCardinality").toString();

        if (!attr.isEmpty()) {
            ep.setMinCardinality(stoi(attr));
        }
        attr = jsonObject.get("maxCardinality").toString();

        if (!attr.isEmpty()) {
            ep.setMaxCardinality(stoi(attr));
        }
        attr = jsonObject.get("successRequired").toString();

        if (!attr.isEmpty()) {
            attr = attr.toLowerCase();
            ep.setSuccessRequired("true".equals(attr));
        }
        addElement(ep);
        this.rep.getEntryPoints().put(ep.getID(), ep);
        attr = jsonObject.get(task).toString();
        id = parser.fetchId(attr);

        if (!attr.isEmpty()) {
            this.epTaskReferences.add(new Pair(ep.getID(), id));
        }
        attr = jsonObject.get(state).toString();
        id = parser.fetchId(attr);

        if (!attr.isEmpty()) {
            this.epStateReferences.add(new Pair(ep.getID(), id));
        } else {
            ae.abort("MF: No initial state identified for EntryPoint: ", String.valueOf(ep.getID()));
        }
        return ep;
    }

    private boolean isReferenceNode(Node node) {
//        Node curChild = node.getFirstChild();
        Node curChild = node.getNextSibling();
        while (curChild != null) {
            String textNode = curChild.getTextContent();
            if (textNode != null) {
                return true;
            }
            curChild = curChild.getNextSibling();

        }
        return false;
    }

    public void addElement(AlicaElement ael) {
        //TODO: Fix
        if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: add Element " + ael.getID() + "  " + ael.getName() + "  " + ae.getAgentName());
//        if (this.elements.size()> 0 && this.elements.get(ael.getID()) != this.elements.values().toArray()[this.elements.values().size()-1])
        if (this.elements.size() > 0 && this.elements.get(ael.getID()) != null && this.elements.get(ael.getID()) != ael) {
            System.out.println("MF: ELEMENT >" + ael.getName() + "< >" + this.elements.get(ael.getID()).getName() + "<");
            System.out.println("MF: ELEMENT >" + ael.getID() + "< >" + this.elements.get(ael.getID()).getID() + "<");
            System.out.println("MF: ELEMENT >" + ael.hashCode() + "< >" + this.elements.get(ael.getID()).hashCode() + "<");
//			cout << segfaultdebug::get_stacktrace() << endl;
            ae.abort("MF: ERROR Double IDs: " + ael.getID());
        }
        elements.put(ael.getID(), ael);
    }

    private void setAlicaElementAttributes(AlicaElement ae, Node ele) {
        String name = ele.getAttributes().getNamedItem("name").getTextContent();
        String comment = ele.getAttributes().getNamedItem("comment").getTextContent();

        if (!name.isEmpty()) {
            ae.setName(name);
        } else
            ae.setName("MISSING_NAME");
        if (!comment.isEmpty()) {
            ae.setComment(comment);
        } else
            ae.setComment("");
    }

    private void setAlicaElementAttributes(AlicaElement ae, JSONObject jsonObject) {
        String name = jsonObject.get("name").toString();
        String comment = jsonObject.get("comment").toString();
        if (CommonUtils.MF_DEBUG_debug)  System.out.println("MF: attribute " + name + " ("+ comment + ")");

        if (!name.isEmpty()) {
            ae.setName(name);
        } else
            ae.setName("MISSING_NAME");
        if (!comment.isEmpty()) {
            ae.setComment(comment);
        } else
            ae.setComment("");
    }

    public AlicaEngine getAE() {
        return ae;
    }

    public PlanRepository getRep() {
        return rep;
    }

    public void attachPlanReferences() {
        if (CommonUtils.MF_DEBUG_debug)  System.out.println("MF: Attaching Plan references..");
        //epTaskReferences
        for (Pair<Long, Long> pairs : this.epTaskReferences) {
            Task task = (Task) this.elements.get(pairs.snd);
            EntryPoint entryPoint = (EntryPoint) this.elements.get(pairs.fst);
            entryPoint.setTask(task);
        }
        this.epTaskReferences.clear();

        //transitionAimReferences
        for (Pair<Long, Long> pairs : this.transitionAimReferences) {
            Transition transition = (Transition) this.elements.get(pairs.fst);
            State state = (State) this.elements.get(pairs.snd);

            if (state == null) {
                ae.abort("MF: Cannot resolve transitionAimReferences target: ", "" + pairs.fst);
            }
            transition.setOutState(state);
            state.addInTransition(transition);
        }
        this.transitionAimReferences.clear();

        //epStateReferences
        for (Pair<Long, Long> pairs : this.epStateReferences) {
//            State state = (State) this.elements.get(pairs.snd);
            Object stateID = pairs.snd;
            State state = (State)this.elements.get(stateID);
            EntryPoint entryPoint = (EntryPoint) this.elements.get(pairs.fst);
            entryPoint.setState(state);
            state.setEntryPoint(entryPoint);
        }
        this.epStateReferences.clear();

        //stateInTransitionReferences
        for (Pair<Long, Long> pairs : this.stateInTransitionReferences) {
            Transition transition = (Transition) this.elements.get(pairs.snd);
            State state = (State) this.elements.get(pairs.fst);

            if (state != transition.getOutState()) {
                ae.abort("MF: Unexpected reference in a transition! ", "" + pairs.fst);
            }
        }
        this.stateInTransitionReferences.clear();

        //stateOutTransitionReferences
        for (Pair<Long, Long> pairs : this.stateOutTransitionReferences) {
            State state = (State) this.elements.get(pairs.fst);
            Transition transition = (Transition) this.elements.get(pairs.snd);
            state.addOutTransition(transition);
            transition.setInState(state);
        }
        this.stateOutTransitionReferences.clear();

        //statePlanReferences
        for (Pair<Long, Long> pairs : this.statePlanReferences) {
            State state = (State) this.elements.get(pairs.fst);
            AbstractPlan plan = (AbstractPlan) this.elements.get(pairs.snd);
            state.getPlans().add(plan);

            if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: attachPlanReferences()  " + state.getName() +"(" + state.getID()  + ") -> "+ plan.getName() +"(" + plan.getID()+")");
        }
        this.statePlanReferences.clear();

        //planTypePlanReferences
        for (Pair<Long, Long> pairs : this.planTypePlanReferences) {
            PlanType pt = (PlanType) this.elements.get(pairs.fst);
            Plan p = (Plan) this.elements.get(pairs.snd);
            pt.getPlans().add(p);
        }
        this.planTypePlanReferences.clear();

        //conditionVarReferences
        for (Pair<Long, Long> pairs : this.conditionVarReferences) {
            Condition c = (Condition) this.elements.get(pairs.fst);
            Variable v = (Variable) this.elements.get(pairs.snd);
            c.getVariables().add(v);
        }
        this.conditionVarReferences.clear();

        //paramSubPlanReferences
        for (Pair<Long, Long> pairs : this.paramSubPlanReferences) {
            Parametrisation p = (Parametrisation) this.elements.get(pairs.fst);
            AbstractPlan ap = (AbstractPlan) this.elements.get(pairs.snd);
            p.setSubPlan(ap);
        }
        this.paramSubPlanReferences.clear();

        //paramSubVarReferences
        for (Pair<Long, Long> pairs : this.paramSubVarReferences) {
            Parametrisation p = (Parametrisation) this.elements.get(pairs.fst);
            Variable ap = (Variable) this.elements.get(pairs.snd);
            p.setSubVar(ap);
        }
        this.paramSubVarReferences.clear();

        //paramVarReferences
        for (Pair<Long, Long> pairs : this.paramVarReferences) {
            Parametrisation p = (Parametrisation) this.elements.get(pairs.fst);
            Variable v = (Variable) this.elements.get(pairs.snd);
            p.setVar(v);
        }
        this.paramVarReferences.clear();

        //transitionSynchReferences
        for (Pair<Long, Long> pairs : this.transitionSynchReferences) {
            Transition t = (Transition) this.elements.get(pairs.fst);
            SyncTransition sync = (SyncTransition) this.elements.get(pairs.snd);
            t.setSyncTransition(sync);
            sync.getInSync().add(t);
        }
        this.transitionSynchReferences.clear();

        //planningProblemPlanReferences
        for (Pair<Long, Long> pairs : this.planningProblemPlanReferences) {
            PlanningProblem planningProblem = (PlanningProblem) this.elements.get(pairs.fst);
            AbstractPlan abstractPlan = (AbstractPlan) this.elements.get(pairs.snd);
            planningProblem.getPlans().add(abstractPlan);
        }
        this.planningProblemPlanReferences.clear();

        //planningProblemPlanWaitReferences
        for (Pair<Long, Long> pairs : this.planningProblemPlanWaitReferences) {
            PlanningProblem s = (PlanningProblem) this.elements.get(pairs.fst);
            Plan p = (Plan) this.elements.get(pairs.snd);
            s.setWaitPlan(p);
        }
        this.planningProblemPlanWaitReferences.clear();

        //planningProblemPlanAlternativeReferences
        for (Pair<Long, Long> pairs : this.planningProblemPlanAlternativeReferences) {
            PlanningProblem s = (PlanningProblem) this.elements.get(pairs.fst);
            Plan p = (Plan) this.elements.get(pairs.snd);
            s.setAlternativePlan(p);
        }
        this.planningProblemPlanAlternativeReferences.clear();

        //quantifierScopeReferences
        for (Pair<Long, Long> pairs : this.quantifierScopeReferences) {
            AlicaElement ae = (AlicaElement) this.elements.get(pairs.snd);
            Quantifier q = (Quantifier) this.elements.get(pairs.fst);
            q.setScope(this.ae, ae);
        }
        this.quantifierScopeReferences.clear();

        removeRedundancy();
        if (CommonUtils.MF_DEBUG_debug)  System.out.println("MF: DONE!");
    }

    private void removeRedundancy() {
        for (Plan plan : this.rep.getPlans().values()) {
            ArrayList<Transition> transToRemove = new ArrayList<>();
            for (Transition tran : plan.getTransitions()) {
                if (tran.getInState() == null) {
                    transToRemove.add(tran);
                }
            }

            for (Transition tran : transToRemove) {
                plan.getTransitions().remove(tran);
            }
        }
    }

    public void createBehaviour(Document node) {
        Element element = node.getDocumentElement();
        long id = this.parser.parserId(element);
        Behaviour beh = new Behaviour();
        beh.setID(id);

        setAlicaElementAttributes(beh, element);
        addElement(beh);
        this.rep.getBehaviours().put(beh.getID(), beh);
        Node curChild = element.getFirstChild().getNextSibling();
        while (curChild != null) {
            String val = curChild.getNodeName();
            long cid = this.parser.parserId(curChild);

            if (configurations.equals(val)) {
                BehaviourConfiguration bc = createBehaviourConfiguration(curChild);
                this.rep.getBehaviourConfigurations().put(bc.getID(), bc);
                bc.setBehaviour(beh);
                beh.getConfigurations().add(bc);
            } else {
                ae.abort("MF: Unhandled Behaviour Child:", curChild.getNodeValue());
            }
            curChild = curChild.getNextSibling();

            if ("#text".equals(curChild.getNodeName())) {
                curChild = curChild.getNextSibling();
            }
        }
    }

    public void createBehaviour(JSONObject jsonObject) {
        long id = parser.fetchId(jsonObject.get("id").toString());
        Behaviour beh = new Behaviour(this.ae);
        beh.setID(id);
        setAlicaElementAttributes(beh, jsonObject);
        addElement(beh);
        this.rep.getBehaviours().put(beh.getID(), beh);

//        Node curChild = element.getFirstChild().getNextSibling();
        JSONArray configurations = (JSONArray) jsonObject.get("configurations");

        if (configurations == null)
            return;
//        while (curChild != null) {
        for (Object obj : configurations) {
//            String val = curChild.getNodeName();
//            long cid = this.parser.parserId(curChild);
            JSONObject confObj = (JSONObject) obj;
            long confID = parser.fetchId(confObj.get("id").toString());
//
//            if (configurations.equals(val)) {
                BehaviourConfiguration bc = createBehaviourConfiguration(confObj);
                this.rep.getBehaviourConfigurations().put(bc.getID(), bc);
                bc.setBehaviour(beh);
                beh.getConfigurations().add(bc);
//            } else {
//                ae.abort("MF: Unhandled Behaviour Child:", curChild.getNodeValue());
//            }
//            curChild = curChild.getNextSibling();
//
//            if ("#text".equals(curChild.getNodeName())) {
//                curChild = curChild.getNextSibling();
//            }
//        }
        }
    }

    private BehaviourConfiguration createBehaviourConfiguration(Node element) {
        BehaviourConfiguration b = new BehaviourConfiguration(ae);
        b.setID(this.parser.parserId(element));
        b.setFileName(this.parser.getCurrentFile());

        String attr = element.getAttributes().getNamedItem("masterPlan").getTextContent();
        String attrString = "";
        if (!attr.isEmpty()) {
            attrString = attr;
            if (attrString.equals("true")) {
                b.setMasterPlan(true);
            }
        }

        Node receiveRemoteCommand = element.getAttributes().getNamedItem("receiveRemoteCommand");
        if (receiveRemoteCommand != null && !receiveRemoteCommand.getTextContent().isEmpty()) {
            attr = receiveRemoteCommand.getTextContent();
            attrString = attr;
            if (attrString.equals("true")) {
                b.setEventDriven(true);
            }
        }
        Node visionTriggered = element.getAttributes().getNamedItem("visionTriggered");

        if (visionTriggered != null && !visionTriggered.getTextContent().isEmpty()) {
            attr = visionTriggered.getTextContent();
            attrString = attr;
            if (attrString.equals("true")) {
                b.setEventDriven(true);
            }
        }
        Node eventDriven = element.getAttributes().getNamedItem("eventDriven");

        if (eventDriven != null && !eventDriven.getTextContent().isEmpty()) {
            attr = eventDriven.getTextContent();
            attrString = attr;
            if (attrString.equals("true")) {
                b.setEventDriven(true);
            }
        }
        Node deferring = element.getAttributes().getNamedItem("deferring");

        if (deferring != null && !deferring.getTextContent().isEmpty()) {
            attr = deferring.getTextContent();
            b.setDeferring(stoi(attr));
        }
        Node frequency = element.getAttributes().getNamedItem("frequency");

        if (frequency != null && !frequency.getTextContent().isEmpty()) {
            attr = frequency.getTextContent();
            b.setFrequency(stoi(attr));
        }
        setAlicaElementAttributes(b, element);
        this.elements.put(b.getID(), b);
        Node elementFirstChild = element.getFirstChild();

        if (elementFirstChild == null)
            return b;

        Node curChild = elementFirstChild.getNextSibling();

        while (curChild != null) {
            String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);
            if (vars.endsWith(val)) {
                Variable v = createVariable(curChild);
                b.getVariables().add(v);
            } else if (parameters.equals(val)) {
                String key = curChild.getAttributes().getNamedItem("key").getTextContent();
                String value = curChild.getAttributes().getNamedItem("value").getTextContent();

                if (key != null && value != null) {
                    b.getParameters().put(key, value);
                }
            } else {
                ae.abort("MF: Unhandled BehaviourConfiguration Child: " + curChild);
            }
            curChild = curChild.getNextSibling();
        }

        return b;
    }

    private BehaviourConfiguration createBehaviourConfiguration(JSONObject jsonObject) {
        BehaviourConfiguration behaviourConfiguration = new BehaviourConfiguration(ae);
        long id = this.parser.fetchId(jsonObject.get("id").toString());
        behaviourConfiguration.setID(id);
        behaviourConfiguration.setFileName(this.parser.getCurrentFile());

//        String attr = (String)jsonObject.get("masterPlan");
//        String attrString = "";
//        if (!attr.isEmpty()) {
//            attrString = attr;
//            if (attrString.equals("true")) {
//                b.setMasterPlan(true);
//            }
//        }
//
//        Node receiveRemoteCommand = element.getAttributes().getNamedItem("receiveRemoteCommand");
//        if (receiveRemoteCommand != null && !receiveRemoteCommand.getTextContent().isEmpty()) {
//            attr = receiveRemoteCommand.getTextContent();
//            attrString = attr;
//            if (attrString.equals("true")) {
//                b.setEventDriven(true);
//            }
//        }
//        Node visionTriggered = element.getAttributes().getNamedItem("visionTriggered");
//
//        if (visionTriggered != null && !visionTriggered.getTextContent().isEmpty()) {
//            attr = visionTriggered.getTextContent();
//            attrString = attr;
//            if (attrString.equals("true")) {
//                b.setEventDriven(true);
//            }
//        }
//        Node eventDriven = element.getAttributes().getNamedItem("eventDriven");
//
//        if (eventDriven != null && !eventDriven.getTextContent().isEmpty()) {
//            attr = eventDriven.getTextContent();
//            attrString = attr;
//            if (attrString.equals("true")) {
//                b.setEventDriven(true);
//            }
//        }
//        Node deferring = element.getAttributes().getNamedItem("deferring");
//
//        if (deferring != null && !deferring.getTextContent().isEmpty()) {
//            attr = deferring.getTextContent();
//            b.setDeferring(stoi(attr));
//        }
//        Node frequency = element.getAttributes().getNamedItem("frequency");
//
//        if (frequency != null && !frequency.getTextContent().isEmpty()) {
//            attr = frequency.getTextContent();
//            b.setFrequency(stoi(attr));
//        }
        setAlicaElementAttributes(behaviourConfiguration, jsonObject);
        this.elements.put(behaviourConfiguration.getID(), behaviourConfiguration);
//        Node elementFirstChild = element.getFirstChild();
//
//        if (elementFirstChild == null)
//            return b;
//
//        Node curChild = elementFirstChild.getNextSibling();
//
//        while (curChild != null) {
//            String val = curChild.getNodeValue();
//            long cid = this.parser.parserId(curChild);
//            if (vars.endsWith(val)) {
//                Variable v = createVariable(curChild);
//                b.getVariables().add(v);
//            } else if (parameters.equals(val)) {
//                String key = curChild.getAttributes().getNamedItem("key").getTextContent();
//                String value = curChild.getAttributes().getNamedItem("value").getTextContent();
//
//                if (key != null && value != null) {
//                    b.getParameters().put(key, value);
//                }
//            } else {
//                ae.abort("MF: Unhandled BehaviourConfiguration Child: " + curChild);
//            }
//            curChild = curChild.getNextSibling();
//        }

        return behaviourConfiguration;
    }

    public RoleSet createRoleSet(Document doc, Plan masterPlan) {
        Element element = doc.getDocumentElement();

        String def = element.getAttribute("default");
        boolean isDefault = false;
        if (def != null) {
            String d = def;
            if (d.equals("true")) {
                isDefault = true;
            }
        }

        String pidPtr = element.getAttribute("usableWithPlanID");
        long pid = 0;

        if (pidPtr != null) {
            pid = stol(pidPtr);
        }

        boolean isUseable = false;
        if (ignoreMasterPlanId) {
            isUseable = true;
        } else {
            isUseable = pidPtr != null && (pid == masterPlan.getID());
        }

        if (!isDefault && !isUseable) {
            ae.abort("MF:Selected RoleSet is not default, nor useable with current masterplan");
        }

        RoleSet rs = new RoleSet();
        rs.setID(this.parser.parserId(element));
        setAlicaElementAttributes(rs, element);
        rs.setIsDefault(isDefault);
        rs.setUsableWithPlanId(pid);
        addElement(rs);

        Node curChild = getNodeChild(element);

        while (curChild != null) {
            String val = curChild.getNodeName();

            if (mappings.equals(val)) {
                RoleTaskMapping rtm = createRoleTaskMapping(curChild);
                rs.getRoleTaskMappings().add(rtm);
            } else {
                ae.abort("MF: Unhandled RoleSet Child:", curChild.getNodeValue());
            }
            curChild = getNextSilbing(curChild);
        }

        return rs;
    }

    public RoleSet createRoleSet(JSONObject jsonObject, Plan masterPlan) {

        boolean isDefault = (boolean) jsonObject.get("defaultRoleSet");

//        String pidPtr = element.getAttribute("usableWithPlanID");
//        long pid = 0;
//
//        if (pidPtr != null) {
//            pid = stol(pidPtr);
//        }

//        boolean isUseable = false;
//        if (ignoreMasterPlanId) {
//            isUseable = true;
//        } else {
//            isUseable = pidPtr != null && (pid == masterPlan.getID());
//        }
//
//        if (!isDefault && !isUseable) {
//            ae.abort("MF:Selected RoleSet is not default, nor useable with current masterplan");
//        }

        RoleSet rs = new RoleSet();

        rs.setID(parser.fetchId(jsonObject.get("id").toString()));
        setAlicaElementAttributes(rs, jsonObject);
        rs.setIsDefault(isDefault);
//        rs.setUsableWithPlanId(pid);
        rs.setUsableWithPlanId(masterPlan.getID());
        addElement(rs);

        JSONArray roles = (JSONArray) jsonObject.get("roles");

        for (Object obj : roles) {
            JSONObject role = (JSONObject) obj;
            RoleTaskMapping rtm = createRoleTaskMapping(role);
             rs.getRoleTaskMappings().add(rtm);
//            } else {
//                ae.abort("MF: Unhandled RoleSet Child:", curChild.getNodeValue());
//            }
        }
        return rs;
    }

    private Node getNodeChild(Node element) {

        Node curChild = element.getFirstChild();

        if ("#text".equals(curChild.getNodeName())) {
            curChild = curChild.getNextSibling();
        }
        return curChild;
    }

    private RoleTaskMapping createRoleTaskMapping(Node element) {
        RoleTaskMapping rtm = new RoleTaskMapping();
        rtm.setID(this.parser.parserId(element));
        setAlicaElementAttributes(rtm, element);
        addElement(rtm);

        Node curChild = getNodeChild(element);
        while (curChild != null) {
            String val = curChild.getNodeName();

            if (taskPriorities.equals(val)) {
                String keyPtr = curChild.getAttributes().getNamedItem("key").getTextContent();
                String valuePtr = curChild.getAttributes().getNamedItem("value").getTextContent();
                if (keyPtr != null && valuePtr != null) {
                    rtm.getTaskPriorities().put(stol(keyPtr), stod(valuePtr));
                }
            } else if (role.equals(val)) {
                long cid = this.parser.parserId(curChild);
                this.rtmRoleReferences.add(new Pair<>(rtm.getID(), cid));
            } else {
                ae.abort("MF: Unhandled RoleTaskMapping Child ", curChild.getNodeValue());
            }
            curChild = getNextSilbing(curChild);
        }

        return rtm;
    }

    private Long generateUniqueId() {
        long val = -1;

        do {
            final UUID uid = UUID.randomUUID();
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(uid.getLeastSignificantBits());
            buffer.putLong(uid.getMostSignificantBits());
            final BigInteger bi = new BigInteger(buffer.array());
            val = bi.longValue();
        }
        // We also make sure that the ID is in positive space, if its not we simply repeat the process
        while (val < 0);
        return val;
    }

    private RoleTaskMapping createRoleTaskMapping(JSONObject jsonObject) {
        RoleTaskMapping rtm = new RoleTaskMapping();
//        long id = parser.fetchId(jsonObject.get("id").toString());
        long id = generateUniqueId();
        rtm.setID(id);
        setAlicaElementAttributes(rtm, jsonObject);
        addElement(rtm);
//        Role role = new Role();
//        role.setName((String) jsonObject.get("name"));
//        role.setID(id);
        Role role = createRole(jsonObject);
        this.rtmRoleReferences.add(new Pair<>(rtm.getID(), role.getID()));
//        Node curChild = getNodeChild(element);
        JSONObject priorities = (JSONObject) jsonObject.get("taskPriorities");
        Set set = priorities.keySet();

        for (Object taskEntry : set) {
            long taskRef = this.parser.extractID((String) taskEntry);
            Double priority = (Double) priorities.get(taskEntry);

            rtm.getTaskPriorities().put(taskRef, priority);
//            this.rtmRoleReferences.add(new Pair<Long, Long>(rtm.getID(), taskRef));
        }

//        while (curChild != null) {
//            String val = curChild.getNodeName();
//
//            if (taskPriorities.equals(val)) {
//                String keyPtr = curChild.getAttributes().getNamedItem("key").getTextContent();
//                String valuePtr = curChild.getAttributes().getNamedItem("value").getTextContent();
//                if (keyPtr != null && valuePtr != null) {
//                    rtm.getTaskPriorities().put(stol(keyPtr), stod(valuePtr));
//                }
//            } else if (role.equals(val)) {
//                long cid = this.parser.parserId(curChild);
//                this.rtmRoleReferences.add(new Pair<>(rtm.getID(), cid));
//            } else {
//                ae.abort("MF: Unhandled RoleTaskMapping Child ", curChild.getNodeValue());
//            }
//            curChild = getNextSilbing(curChild);
//        }
        return rtm;
    }

    private Node getNextSilbing(Node curChild) {
        curChild = curChild.getNextSibling();

        if ("#text".equals(curChild.getNodeName())) {
            curChild = curChild.getNextSibling();
        }
        return curChild;
    }

    public void createTasks(Document doc) {
        Element element = doc.getDocumentElement();
        TaskRepository tr = new TaskRepository();
        tr.setID(this.parser.parserId(element));
        tr.setFileName(this.parser.getCurrentFile());
        addElement(tr);
        setAlicaElementAttributes(tr, element);
        this.rep.getTaskRepositorys().put(tr.getID(), tr);
        long id = 0;
        String defaultTaskPtr = element.getAttribute("defaultTask");
        if (defaultTaskPtr != null) {
            id = stol(defaultTaskPtr);
            tr.setDefaultTask(id);
        }

        Node curChild = element.getFirstChild();

        if (curChild.getNodeValue().startsWith("\n"))
            curChild = curChild.getNextSibling();

        while (curChild != null) {
            long cid = this.parser.parserId(curChild);

            Task task = new Task(cid == id);
            task.setID(cid);
            setAlicaElementAttributes(task, curChild);
            String descriptionkPtr = curChild.getAttributes().getNamedItem("description").getTextContent();

            if (descriptionkPtr != null) {
                task.setDescription(descriptionkPtr);
            }
            addElement(task);
            this.rep.getTasks().put(task.getID(), task);
            task.setTaskRepository(tr);
            tr.getTasks().add(task);

            curChild = curChild.getNextSibling();
            if (curChild.getNodeValue().startsWith("\n"))
                curChild = curChild.getNextSibling();
        }
    }

    public void createTasks(JSONObject jsonObject) {
        TaskRepository tr = new TaskRepository();
        long id = parser.fetchId(jsonObject.get("id").toString());
        tr.setID(id);
        tr.setFileName(this.parser.getCurrentFile());
        addElement(tr);
        setAlicaElementAttributes(tr, jsonObject);
        this.rep.getTaskRepositorys().put(tr.getID(), tr);
        Object defaultTaskObj = jsonObject.get("defaultTask");

        if (defaultTaskObj != null) {
            id = stol(defaultTaskObj.toString());
            tr.setDefaultTask(id);
        }

//        Node curChild = element.getFirstChild();
        JSONArray tasks = (JSONArray) jsonObject.get("tasks");
//
//        if (curChild.getNodeValue().startsWith("\n"))
//            curChild = curChild.getNextSibling();
//
//        while (curChild != null) {
        for (Object obj  : tasks) {
//            long cid = this.parser.parserId(curChild);
            JSONObject taskObj = (JSONObject) obj;
            long taskID = parser.fetchId(taskObj.get("id").toString());
//            Task task = new Task(cid == id);
            Task task = new Task(taskID == id);
            task.setID(taskID);
            setAlicaElementAttributes(task, taskObj);
//            String descriptionkPtr = curChild.getAttributes().getNamedItem("description").getTextContent();
//
//            if (descriptionkPtr != null) {
//                task.setDescription(descriptionkPtr);
//            }
            addElement(task);
            this.rep.getTasks().put(task.getID(), task);
            task.setTaskRepository(tr);
            tr.getTasks().add(task);
//
//            curChild = curChild.getNextSibling();
//            if (curChild.getNodeValue().startsWith("\n"))
//                curChild = curChild.getNextSibling();
//        }
        }
    }

    public void createRoleDefinitionSet(Document node) {
        Element element = node.getDocumentElement();
        RoleDefinitionSet r = new RoleDefinitionSet();
        r.setID(this.parser.parserId(element));
        r.setFileName(this.parser.getCurrentFile());
        setAlicaElementAttributes(r, element);
        addElement(r);
        this.rep.getRoleDefinitionSets().put(r.getID(), r);

        Node curChild = getNodeChild(element);

        while (curChild != null) {
            String val = curChild.getNodeName();

            if (roles.equals(val)) {
                Role role = createRole(curChild);
                r.getRoles().add(role);
                role.setRoleDefinitionSet(r);
            } else {
                ae.abort("MF: Unhandled RoleDefinitionSet Child:", curChild.getNodeValue());
            }
            curChild = getNextSilbing(curChild);
        }
    }

    private Role createRole(Node element) {
        Role r = new Role();
        r.setID(this.parser . parserId(element));
        setAlicaElementAttributes(r, element);
        addElement(r);
        this.rep.getRoles().put(r.getID(), r);
        Node curChild = getNodeChild(element);

        while (curChild != null) {
			String val = curChild.getNodeName();

            if (characteristics.equals(val)) {
                Characteristic  cha = createCharacteristic(curChild);
                r.getCharacteristics().put(cha.getName(), cha);
            } else {
                ae.abort("MF: Unhandled Role Child:", curChild.getNodeValue());
            }
            curChild = getNextSilbing(curChild);
        }
        return r;
    }

    private Role createRole(JSONObject jsonObject) {
        Role role = new Role();
        long id = parser.fetchId(jsonObject.get("id").toString());
        role.setID(id);
        setAlicaElementAttributes(role, jsonObject);
        addElement(role);
        this.rep.getRoles().put(role.getID(), role);
//        Node curChild = getNodeChild(element);

        JSONArray characteristics = (JSONArray) jsonObject.get("characteristics");
//        while (curChild != null) {

        for (Object obj : characteristics) {
//            String val = curChild.getNodeName();
            JSONObject charObj = (JSONObject) obj;
//
//            if (ModelFactory.characteristics.equals(val)) {
//                Characteristic cha = createCharacteristic(curChild);
//                r.getCharacteristics().put(cha.getName(), cha);
//            } else {
                ae.abort("MF: Unhandled Role Characteristic: ", role.getName());
//            }
//            curChild = getNextSilbing(curChild);
        }
//        }
        return role;
    }

    private Characteristic createCharacteristic(Node element) {
        Characteristic cha = new Characteristic();
        cha.setID(this.parser.parserId(element));
        setAlicaElementAttributes(cha, element);
		String attr = element.getAttributes().getNamedItem("weight").getTextContent();
        if (attr != null)
        {
            cha.setWeight(stod(attr));
        }

        addElement(cha);
        this.rep.getCharacteristics().put(cha.getID(), cha);
        Node curChild = getNodeChild(element);

        while (curChild != null)
        {
			String val = curChild.getNodeName();

            if (capability.equals(val))
            {
                long capid = this.parser.parserId(curChild);
                this.charCapReferences.add(new Pair<Long, Long>(cha.getID(), capid));
            }
            else if (value.equals(val))
            {
                long capValid = this.parser.parserId(curChild);
                this.charCapValReferences.add(new Pair<Long, Long>(cha.getID(), capValid));
            }
            else
            {
                ae.abort("MF: Unhandled Characteristic Child:", curChild.getNodeValue());
            }
            curChild = getNextSilbing(curChild);
        }
        return cha;
    }

    public void createCapabilityDefinitionSet(Document node) {
        Element element = node.getDocumentElement();
        CapabilityDefinitionSet capSet = new CapabilityDefinitionSet();
        capSet.setID(this.parser.parserId(element));
        setAlicaElementAttributes(capSet, element);
        addElement(capSet);

        Node curChild = getNodeChild(element);
        while (curChild != null)
        {
			String val = curChild.getNodeName();

            if (capabilities.equals(val))
            {
                Capability cap = createCapability(curChild);
                capSet.getCapabilities().add(cap);
            }
            else
            {
                ae.abort("MF: Unhandled Behaviour Child:", curChild.getNodeValue());
            }
            curChild = getNextSilbing(curChild);
        }
    }

    private Capability createCapability(Node element) {
        Capability cap = new Capability();
        cap.setID(this.parser.parserId(element));
        setAlicaElementAttributes(cap, element);
        addElement(cap);
        this.rep.getCapabilities().put(cap.getID(), cap);

        Node curChild = getNodeChild(element);

        while (curChild != null)
        {
			String val = curChild.getNodeName();
            if (capValues.equals(val))
            {
                CapValue cval = new CapValue();
                cval.setID(this.parser.parserId(curChild));
                setAlicaElementAttributes(cval, curChild);
                addElement(cval);
                cap.getCapValues().add(cval);
            }
            else
            {
                ae.abort("MF: Unhandled Capability Child:", curChild.getNodeValue());
            }
            curChild = getNextSilbing(curChild);
        }
        return cap;
    }


    public PlanningProblem createPlanningProblem(Document node) {
//        tinyxml2::XMLElement* element = node->FirstChildElement();
        Element element = node.getDocumentElement();
        PlanningProblem p = new PlanningProblem(ae);
        p.setID(this.parser.parserId(element));
        p.setFileName(this.parser.getCurrentFile());
        setAlicaElementAttributes(p, element);
        addElement(p);

        String conditionPtr = element.getAttribute("updateRate");

        if (conditionPtr != null) {
            p.setUpdateRate(Integer.valueOf(conditionPtr));
        }
        else {
            p.setUpdateRate(-1);
        }
        String attr;
        String attrPtr = element.getAttribute("distributeProblem");

        if(attrPtr != null) {
            attr = attrPtr;
            if (attr.equals("true")) {
                p.setDistributeProblem(true);
            }
            else {
                p.setDistributeProblem(false);
            }
        }
        else {
            p.setDistributeProblem(false);
        }
        attrPtr = element.getAttribute("planningType");

        if (attrPtr != null) {
            attr = attrPtr;
            if (attr.equals("Interactive")) {
                p.setPlanningType(Interactive);
            }
            else if (attr.equals("Online")) {
                p.setPlanningType(Online);
            }
            else {
                p.setPlanningType(Offline);
            }
        }
        else {
            p.setPlanningType(Online);
        }

        attrPtr = element.getAttribute("requirements");

        if (attrPtr != null) {
            p.setRequirements(attrPtr);
        }
        else {
            p.setRequirements("");
        }

        this.rep.getPlanningProblems().put(p.getID(), p);

//        tinyxml2::XMLElement curChild = element.FirstChildElement();
        Node curChild = element.getFirstChild();

        while (curChild != null) {
            String val = curChild.getNodeName();

            if ("#text".equals(curChild.getNodeName()))
                curChild = curChild.getNextSibling();
            else {
                long cid = this.parser.parserId(curChild);

                if (plans.equals(val)) {
                    this.planningProblemPlanReferences.add(new Pair(p.getID(), cid));
                } else if (conditions.equals(val)) {
                    String type = curChild.getAttributes().getNamedItem("xsi:type").getNodeValue();
                    String typeStr;

                    if (type != null) {
                        typeStr = type;

                        if (typeStr.equals("alica:PostCondition")) {
                            PostCondition pa = createPostCondition(curChild);
                            p.setPostCondition(pa);
                        } else if (typeStr.equals("alica:PreCondition")) {
                            PreCondition pa = createPreCondition(curChild);
                            p.setPreCondition(pa);
                        } else if (typeStr.equals("alica:RuntimeCondition")) {
                            RuntimeCondition pa = createRuntimeCondition(curChild);
                            p.setRuntimeCondition(pa);
                        }
                    } else {
                        ae.abort("MF: Unknown Condition type:", curChild.getNodeValue());
                    }
                } else if (waitPlan.equals(val)) {
                    this.planningProblemPlanWaitReferences.add(new Pair(p.getID(), cid));
                } else if (alternativePlan.equals(val)) {
                    this.planningProblemPlanAlternativeReferences.add(new Pair(p.getID(), cid));
                }

                curChild = curChild.getNextSibling();
            }
        }
        return p;
    }

    public LinkedHashMap<Long, AlicaElement> getElements() {
        return elements;
    }

    public void createPlanType(Document node) {
        Element element = node.getDocumentElement();
        PlanType pt = new PlanType(ae);
        pt.setID(this.parser.parserId(element));
        pt.setFileName(this.parser.getCurrentFile());
        setAlicaElementAttributes(pt, element);
        addElement(pt);
        this.rep.getPlanTypes().put(pt.getID(), pt);
        Node curChild = element.getFirstChild();

        while (curChild != null) {
            String val = curChild.getNodeName();
            // TODO: FIXME skip #text (extract to method)
            if ("#text".equals(curChild.getNodeName()))
                curChild = curChild.getNextSibling();
            else {

                if (plans.equals(val)) {
                    String activated = "";
                    String activatedPtr = curChild.getAttributes().getNamedItem("activated").getNodeValue();

                    if (activatedPtr != null) {
                        activated = activatedPtr;
//                    transform(activated.begin(), activated.end(), activated.begin(), ::tolower);
                        activated = activated.toLowerCase();

                        if (activated.equals("true")) {
                            long cid = this.parser.parserId(curChild.getFirstChild().getNextSibling());
                            this.planTypePlanReferences.add(new Pair(pt.getID(), cid));
                        }
                    } else {
                        if (CommonUtils.MF_DEBUG_debug) System.out.println("MF: Skipping deactivated plan");
                    }
                } else if (vars.equals(val)) {
                    Variable var = createVariable(curChild);
                    pt.getVariables().add(var);
                } else if (parametrisation.equals(val)) {
                    Parametrisation para = createParametrisation(curChild);
                    pt.getParametrisation().add(para);
                } else {
                    ae.abort("MF: Unhandled PlanType Child:", val);
                }
                curChild = curChild.getNextSibling();
            }
        }
    }
}

