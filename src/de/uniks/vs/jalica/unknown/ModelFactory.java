package de.uniks.vs.jalica.unknown;

import com.sun.tools.javac.util.Pair;
import de.uniks.vs.jalica.behaviours.Behaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.parser.PlanParser;
import de.uniks.vs.jalica.teamobserver.PlanRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import static de.uniks.vs.jalica.unknown.CommonUtils.stod;
import static de.uniks.vs.jalica.unknown.CommonUtils.stoi;
import static de.uniks.vs.jalica.unknown.CommonUtils.stol;

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

    private AlicaEngine ae;
    private PlanParser parser;
    private PlanRepository rep;
    private String subplan;
    private String postCondition;
    private boolean ignoreMasterPlanId;

    private LinkedHashMap<Long, AlicaElement> elements = new LinkedHashMap<>();
    private List<Pair<Long, Long>> epStateReferences  = new ArrayList<>();
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



    public ModelFactory(AlicaEngine ae, PlanParser parser, PlanRepository rep) {

        this.ae = ae;
        this.parser = parser;
        this.rep = rep;
    }

    public void computeReachabilities() {
//        #ifdef MF_DEBUG
        System.out.println( "MF: Computing Reachability sets..." );
//#endif

        for ( Plan plan : this.rep.getPlans().values())
        {
            for (EntryPoint entryPoint : plan.getEntryPoints().values())
            {
                entryPoint.computeReachabilitySet();
            }
        }
//#ifdef MF_DEBUG
        System.out.println( "MF: Computing Reachability sets...done!");
//#endif

    }

    public void attachRoleReferences() {
        CommonUtils.aboutNoImpl();
    }

    public void attachCharacteristicReferences() {
        CommonUtils.aboutNoImpl();
    }

    public Plan createPlan(Document doc) {
        Node element = doc.getDocumentElement();
        long id = this.parser.parserId(element);
        Plan plan = new Plan(id);
        plan.setFileName(this.parser.getCurrentFile());
        setAlicaElementAttributes(plan, element);
        // insert into elements ma
        addElement(plan);
        // insert into plan repository map
        this.rep.getPlans().put(plan.getId(), plan);
//        Node curChild = element.getFirstChild();

//        //TODO: move to parser
//        Vector<Element> nodes = extractToList(element, plans);
//        nodes.addAll(extractToList(element,inTransitions));
//        nodes.addAll(extractToList(element,outTransitions));
//        nodes.addAll(extractToList(element,parametrisation));

        Node curChild = element.getFirstChild().getNextSibling();

        while (curChild != null)
        {
            System.out.println(curChild.getNodeName());

            parser.handleTag(curChild, plan, this);
            curChild = curChild.getNextSibling();
        }
        return plan;
    }

    public SyncTransition createSyncTransition(Node element) {
        SyncTransition s = new SyncTransition();
        s.setId(this.parser.parserId(element));
        setAlicaElementAttributes(s, element);
		String talkTimeoutPtr = element.getAttributes().getNamedItem("talkTimeout").getTextContent();
        if (talkTimeoutPtr != null)
        {
            s.setTalkTimeOut(stol(talkTimeoutPtr));
        }
        String syncTimeoutPtr = element.getAttributes().getNamedItem("syncTimeout").getTextContent();
        if (syncTimeoutPtr!= null)
        {
            s.setSyncTimeOut(stol(syncTimeoutPtr));
        }

        addElement(s);
        this.rep.getSyncTransitions().put(s.getId(), s);
        if (element.getFirstChild() != null)
        {
            ae.abort("MF: Unhandled Synchtransition Child:", element.getFirstChild().toString());
        }
        return s;
    }

    public Variable createVariable(Node element) {
        String type = "";
        String conditionPtr = element.getAttributes().getNamedItem("Type").getTextContent();
        if (conditionPtr != null)
        {
            type = conditionPtr;
        }
        String name = "";
        String namePtr = element.getAttributes().getNamedItem("name").getTextContent();
        if (namePtr!= null)
        {
            name = namePtr;
        }
        Variable v = new Variable(this.parser.parserId(element), name, type);
        setAlicaElementAttributes(v, element);
        addElement(v);
        this.rep.getVariables().put(v.getId(), v);
        return v;
    }

    public RuntimeCondition createRuntimeCondition(Node element) {
        RuntimeCondition r = new RuntimeCondition();
        r.setId(this.parser.parserId(element));
        setAlicaElementAttributes(r, element);
        addElement(r);

        String conditionString = "";
        String conditionPtr = element.getAttributes().getNamedItem("conditionString").getTextContent();
        if (conditionPtr != null)
        {
            conditionString = conditionPtr;
            r.setConditionString(conditionString);
        }

        if (!conditionString.isEmpty())
        {
            //TODO: ANTLRBUILDER
        }
        else
        {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

		String pluginNamePtr = element.getAttributes().getNamedItem("pluginName").getTextContent();
        if (pluginNamePtr != null)
        {
            r.setPlugInName(pluginNamePtr);
        }
        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
            String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);
            if (vars.equals(val))
            {
                this.conditionVarReferences.add(new Pair(r.getId(), cid));
            }
            else if (quantifiers.equals(val) )
            {
                Quantifier q = createQuantifier(curChild);
                r.getQuantifiers().add(q);
            }
            else if (parameters.equals(val))
            {
                Parameter p = createParameter(curChild);
                r.getParameters().add(p);
            }
            else
            {
                ae.abort("MF: Unhandled RuntimeCondition Child", curChild.toString());
            }
            curChild = curChild.getNextSibling();
        }
        return r;
    }

    public Transition createTransition(Node element, Plan plan) {
        Transition tran = new Transition();
        tran.setId(this.parser.parserId(element));
        setAlicaElementAttributes(tran, element);
        addElement(tran);
        this.rep.getTransitions().put(tran.getId(), tran);
//        Node curChild = element.getFirstChild();

        //        //TODO: move to parser
        Vector<Element> nodes = extractToList(element, outState);
        nodes.addAll(extractToList(element, inState));
        nodes.addAll(extractToList(element, preCondition));
        nodes.addAll(extractToList(element, synchronisation));

//        while (curChild != null)
        for (Element curChild : nodes)
        {
//			String val = curChild.getNodeValue();
			String val = curChild.getTagName();
            long cid = this.parser.parserId(curChild);
            if (inState.equals(val))
            {
                //silently ignore
            }
            else if (outState.equals(val))
            {
                this.transitionAimReferences.add(new Pair(tran.getId(), cid));
            }
            else if (preCondition.equals(val))
            {
                PreCondition pre = createPreCondition(curChild);
                tran.setPreCondition(pre);
                pre.setAbstractPlan(plan);
            }
            else if (synchronisation.equals(val))
            {
                this.transitionSynchReferences.add(new Pair(tran.getId(), cid));
            }
            else
            {
                ae.abort("MF: Unhandled Transition Child:", curChild.toString());
            }
//            curChild = curChild.getNextSibling();
        }
        return tran;
    }

    public PreCondition createPreCondition(Node element) {
        PreCondition pre = new PreCondition();
        pre.setId(this.parser.parserId(element));
        setAlicaElementAttributes(pre, element);
        addElement(pre);
        String conditionString = "";
		String conditionPtr = element.getAttributes().getNamedItem("conditionString").getTextContent();
        if (conditionPtr != null)
        {
            conditionString = conditionPtr;
            pre.setConditionString(conditionString);
        }

        if (!conditionString.isEmpty())
        {
            //TODO: ANTLRBUILDER
        }
        else
        {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

        String pluginNamePtr = element.getAttributes().getNamedItem("pluginName").getTextContent();
        if (pluginNamePtr != null)
        {
            pre.setPlugInName(pluginNamePtr);
        }

        String enabled = "";
        String enabledPtr = element.getAttributes().getNamedItem("enabled").getTextContent();
        if (enabledPtr != null)
        {
            enabled = enabledPtr;
            if ("true".equals(enabled))
            {
                pre.setEnabled(true);
            }
            else
            {
                pre.setEnabled(false);
            }
        }
        else
        {
            pre.setEnabled(true);
        }
        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
            String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);
            if (vars.equals(val))
            {
                this.conditionVarReferences.add(new Pair(pre.getId(), cid));
            }
            else if (quantifiers.equals(val))
            {
                Quantifier q = createQuantifier(curChild);
                pre.getQuantifiers().add(q);
            }
            else if (parameters.equals(val))
            {
                Parameter p = createParameter(curChild);
                pre.getParameters().add(p);
            }
            else
            {
                ae.abort("MF: Unhandled PreCondition Child:", curChild.getNodeValue());
            }
            curChild = curChild.getNextSibling();
        }
        return pre;
    }

    private Parameter createParameter(Node element) {
        Parameter p = new Parameter();
        long id = this.parser.parserId(element);
        p.setId(id);
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
        if (typePtr != null)
        {
            typeString = typePtr;
            if ("alica:ForallAgents".equals(typeString))
            {
                q = new ForallAgents(this.ae);
                q.setId(id);
            }
            else
            {
                ae.abort("MF: Unsupported quantifier type! !", typeString);
            }
        }
        else
        {
            ae.abort("MF: Quantifier without type!", String.valueOf(id));
        }

        addElement(q);
        this.rep.getQuantifiers().put(q.getId(), q);
        setAlicaElementAttributes(q, element);

        String scopePtr = element.getAttributes().getNamedItem("scope").getTextContent();
        long cid;
        if (scopePtr != null)
        {
            cid = stol(scopePtr);
            this.quantifierScopeReferences.add(new Pair(q.getId(), cid));
        }
        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
            String val = curChild.getNodeValue();
            if (sorts.equals(val))
            {
                q.getDomainIdentifiers().add(curChild.getTextContent());
            }
            else
            {
                ae.abort("MF: Unhandled Quantifier Child:", curChild.toString());
            }

            curChild = curChild.getNextSibling();
        }

        return q;
    }

    public FailureState createFailureState(Node element) {
        FailureState fail = new FailureState();
        fail.setId(this.parser.parserId(element));
        setAlicaElementAttributes(fail, element);

        addElement(fail);
        this.rep.getStates().put(fail.getId(), fail);

        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
			String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);
            if (inTransitions.equals(val))
            {
                this.stateInTransitionReferences.add(new Pair(fail.getId(), cid));
            }
            else if (postCondition.equals(val))
            {
                PostCondition postCon = createPostCondition(curChild);
                fail.setPostCondition(postCon);
            }
            else
            {
                ae.abort("MF: Unhandled FaulireState Child: ", curChild.getNodeValue());
            }

            curChild = curChild.getNextSibling();
        }
        return fail;
    }

    public SuccessState createSuccessState(Node element) {
        SuccessState suc = new SuccessState();
        suc.setId(this.parser.parserId(element));
        setAlicaElementAttributes(suc, element);

        addElement(suc);
        this.rep.getStates().put(suc.getId(), suc);

        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
			 String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);
            if (inTransitions.equals(val))
            {
                this.stateInTransitionReferences.add(new Pair(suc.getId(), cid));
            }
            else if (postCondition.equals(val))
            {
                PostCondition postCon = createPostCondition(curChild);
                suc.setPostCondition(postCon);
            }
            else
            {
                ae.abort("MF: Unhandled SuccesState Child:", curChild.getNodeValue());
            }

            curChild = curChild.getNextSibling();
        }
        return suc;
    }

    public PostCondition createPostCondition(Node element) {
        PostCondition pos = new PostCondition();
        pos.setId(this.parser.parserId(element));
        setAlicaElementAttributes(pos, element);
        addElement(pos);

        String conditionString = "";
        String conditionPtr = element.getAttributes().getNamedItem("conditionString").getTextContent();
        if (conditionPtr != null)
        {
            conditionString = conditionPtr;
            pos.setConditionString(conditionString);
        }
        if (!conditionString.isEmpty())
        {
            //TODO: ANTLRBUILDER
        }
        else
        {
            //TODO: aus c#
            //pos.ConditionFOL = null;
        }

        String pluginNamePtr = element.getAttributes().getNamedItem("pluginName").getTextContent();
        if (pluginNamePtr != null)
        {
            pos.setPlugInName(pluginNamePtr);
        }

        if (element.getFirstChild() != null)
        {
            ae.abort("MF: Unhandled Result child", element.getFirstChild().toString());
        }

        return pos;
    }

    public State createState(Node element) {
        State s = new State();
        s.setId(this.parser.parserId(element));
        setAlicaElementAttributes(s, element);

        addElement(s);
        this.rep.getStates().put(s.getId(), s);

        //TODO: move to parser
        Vector<Element> nodes = extractToList(element, plans);
        nodes.addAll(extractToList(element,inTransitions));
        nodes.addAll(extractToList(element,outTransitions));
        nodes.addAll(extractToList(element,parametrisation));

//        Node curChild = element.getFirstChild();
//        while (curChild != null)
        for (Element curChild : nodes)
        {
			String val = curChild.getTagName();
            long cid = this.parser.parserId(curChild);

            if (inTransitions.equals(val))
            {
                this.stateInTransitionReferences.add( new Pair(s.getId(), cid));
            }
            else if (outTransitions.equals(val))
            {
                this.stateOutTransitionReferences.add(new Pair(s.getId(), cid));
            }
            else if (plans.equals(val))
            {
                this.statePlanReferences.add(new Pair(s.getId(), cid));
            }
            else if (parametrisation.equals(val))
            {
                Parametrisation para = createParametrisation(curChild);
                s.getParametrisation().add(para);
            }
            else
            {
                ae.abort("MF: Unhandled State Child: ", val);
            }

//            curChild = curChild.getNextSibling();
        }
        return s;
    }

    //  TODO: move to parser
    private Vector extractToList(Node element, String tagName) {
        NodeList nodes = ((Element) element).getElementsByTagName(tagName);
        Vector newNodes = new Vector();

        for (int i = 0; i < nodes.getLength(); i++ ) {
            newNodes.add(nodes.item(i));
        }

        return newNodes;
    }

    private Parametrisation createParametrisation(Node element) {
        Parametrisation para = new Parametrisation();
        para.setId(this.parser.parserId(element));
        setAlicaElementAttributes(para, element);

        addElement(para);
        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
			String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);

            if (subplan.equals(val))
            {
                this.paramSubPlanReferences.add(new Pair(para.getId(), cid));
            }
            else if (subvar.equals(val))
            {
                this.paramSubVarReferences.add(new Pair(para.getId(), cid));
            }
            else if (var.equals(val))
            {
                this.paramVarReferences.add(new Pair(para.getId(), cid));
            }
            else
            {
                ae.abort("MF: Unhandled Parametrisation Child:", curChild.toString());
            }

            curChild = curChild.getNextSibling();
        }
        return para;
    }

    public EntryPoint createEntryPoint(Node element) {
        EntryPoint ep = new EntryPoint();
        ep.setId(this.parser.parserId(element));
        setAlicaElementAttributes(ep, element);
        String attr = element.getAttributes().getNamedItem("minCardinality").getTextContent();
        if (!attr.isEmpty())
        {
            ep.setMinCardinality(stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("maxCardinality").getTextContent();
        if (!attr.isEmpty())
        {
            ep.setMaxCardinality(stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("successRequired").getTextContent();
        if (!attr.isEmpty())
        {
//            transform(attr.begin(), attr.end(), attr.begin(), ::tolower);
            attr = attr.toLowerCase();
            ep.setSuccessRequired("true".equals(attr));
        }

        addElement(ep);
        this.rep.getEntryPoints().put(ep.getId(), ep);
//        Node curChild = element.getFirstChild();
        boolean haveState = false;
        long curChildId;

        //TODO: move to parser
        Vector<Element> nodes = extractToList(element, state);
        nodes.addAll(extractToList(element,task));

//        while (curChild != null)
        for (Element curChild : nodes)
        {
			String val = curChild.getTagName();
            curChildId = this.parser.parserId(curChild);

            if (state.equals(val))
            {
                this.epStateReferences.add(new Pair(ep.getId(), curChildId));
                haveState = true;
            }
            else if (task.equals(val))
            {
                this.epTaskReferences.add(new Pair(ep.getId(), curChildId));
            }
            else
            {
                ae.abort("MF: Unhandled EntryPoint Child: ", val);
            }
//            curChild = curChild.getNextSibling();
        }

        if (!haveState)
        {
            ae.abort("MF: No initial state identified for EntryPoint: ", String .valueOf(ep.getId()));
        }

        return ep;
    }

    private boolean isReferenceNode(Node node) {
//        Node curChild = node.getFirstChild();
        Node curChild = node.getNextSibling();
        while (curChild != null)
        {
            String textNode = curChild.getTextContent();
            if (textNode != null)
            {
                return true;
            }
            curChild = curChild.getNextSibling();

        }
        return false;
    }

    public void addElement(AlicaElement ael) {
        //TODO: Fix
//        if (this.elements.size()> 0 && this.elements.get(ael.getId()) != this.elements.values().toArray()[this.elements.values().size()-1])
        if (this.elements.size()> 0 && this.elements.get(ael.getId()) != null && this.elements.get(ael.getId()) != ael)
        {
            System.out.println( "ELEMENT >" + ael.getName() + "< >" + this.elements.get(ael.getId()).getName() + "<"  );
            String ss = "";
            ss += "MF: ERROR Double IDs: " + ael.getId();
//			cout << segfaultdebug::get_stacktrace() << endl;
            ae.abort(ss);
        }
        elements.put(ael.getId(), ael);
    }

    private void setAlicaElementAttributes(AlicaElement ae, Node ele) {
        String name = ele.getAttributes().getNamedItem("name").getTextContent();
        String comment = ele.getAttributes().getNamedItem("comment").getTextContent();

        if (!name.isEmpty())
        {
            ae.setName(name);
        }
        else
            ae.setName("MISSING_NAME");
        if (!comment.isEmpty())
        {
            ae.setComment(comment);
        }
        else
            ae.setComment("");
    }

    public AlicaEngine getAE() {
        return ae;
    }

    public PlanRepository getRep() {
        return rep;
    }

    public void attachPlanReferences() {
//        #ifdef MF_DEBUG
        System.out.println( "MF: Attaching Plan references.." );
//#endif
        //epTaskReferences
        for (Pair<Long, Long> pairs : this.epTaskReferences)
        {
            Task t = (Task)this.elements.get(pairs.snd);
            EntryPoint ep = (EntryPoint)this.elements.get(pairs.fst);
            ep.setTask(t);
        }
        this.epTaskReferences.clear();

        //transitionAimReferences
        for (Pair<Long, Long> pairs : this.transitionAimReferences)
        {
            Transition t = (Transition)this.elements.get(pairs.fst);
            State st = (State)this.elements.get(pairs.snd);
            if (st == null)
            {
                ae.abort("MF: Cannot resolve transitionAimReferences target: ", ""+pairs.fst);
            }
            t.setOutState(st);
            st.getInTransitions().add(t);
        }
        this.transitionAimReferences.clear();

        //epStateReferences
        for (Pair<Long, Long> pairs : this.epStateReferences)
        {
            State st = (State)this.elements.get(pairs.snd);
            EntryPoint ep = (EntryPoint)this.elements.get(pairs.fst);
            ep.setState(st);
            st.setEntryPoint(ep);
        }
        this.epStateReferences.clear();

        //stateInTransitionReferences
        for (Pair<Long, Long> pairs : this.stateInTransitionReferences)
        {
            Transition t = (Transition)this.elements.get(pairs.snd);
            State st = (State)this.elements.get(pairs.fst);
            if (st != t.getOutState())
            {
                ae.abort("MF: Unexpected reference in a transition! ", ""+pairs.fst);
            }
        }
        this.stateInTransitionReferences.clear();

        //stateOutTransitionReferences
        for (Pair<Long, Long> pairs : this.stateOutTransitionReferences)
        {
            State st = (State)this.elements.get(pairs.fst);
            Transition t = (Transition)this.elements.get(pairs.snd);
            st.getOutTransitions().add(t);
            t.setInState(st);
        }
        this.stateOutTransitionReferences.clear();

        //statePlanReferences
        for (Pair<Long, Long> pairs : this.statePlanReferences)
        {
            State st = (State)this.elements.get(pairs.fst);
            AbstractPlan p = (AbstractPlan)this.elements.get(pairs.snd);
            st.getPlans().add(p);
        }
        this.statePlanReferences.clear();

        //planTypePlanReferences
        for (Pair<Long, Long> pairs : this.planTypePlanReferences)
        {
            PlanType pt = (PlanType)this.elements.get(pairs.fst);
            Plan p = (Plan)this.elements.get(pairs.snd);
            pt.getPlans().add(p);
        }
        this.planTypePlanReferences.clear();

        //conditionVarReferences
        for (Pair<Long, Long> pairs : this.conditionVarReferences)
        {
            Condition c = (Condition)this.elements.get(pairs.fst);
            Variable v = (Variable)this.elements.get(pairs.snd);
            c.getVariables().add(v);
        }
        this.conditionVarReferences.clear();

        //paramSubPlanReferences
        for (Pair<Long, Long> pairs : this.paramSubPlanReferences)
        {
            Parametrisation p = (Parametrisation)this.elements.get(pairs.fst);
            AbstractPlan ap = (AbstractPlan)this.elements.get(pairs.snd);
            p.setSubPlan(ap);
        }
        this.paramSubPlanReferences.clear();

        //paramSubVarReferences
        for (Pair<Long, Long> pairs : this.paramSubVarReferences)
        {
            Parametrisation p = (Parametrisation)this.elements.get(pairs.fst);
            Variable ap = (Variable)this.elements.get(pairs.snd);
            p.setSubVar(ap);
        }
        this.paramSubVarReferences.clear();

        //paramVarReferences
        for (Pair<Long, Long> pairs : this.paramVarReferences)
        {
            Parametrisation p = (Parametrisation)this.elements.get(pairs.fst);
            Variable v = (Variable)this.elements.get(pairs.snd);
            p.setVar(v);
        }
        this.paramVarReferences.clear();

        //transitionSynchReferences
        for (Pair<Long, Long> pairs : this.transitionSynchReferences)
        {
            Transition t = (Transition)this.elements.get(pairs.fst);
            SyncTransition sync = (SyncTransition)this.elements.get(pairs.snd);
            t.setSyncTransition(sync);
            sync.getInSync().add(t);
        }
        this.transitionSynchReferences.clear();

        //planningProblemPlanReferences
        for (Pair<Long, Long> pairs : this.planningProblemPlanReferences)
        {
            PlanningProblem s = (PlanningProblem)this.elements.get(pairs.fst);
            AbstractPlan p = (AbstractPlan)this.elements.get(pairs.snd);
            s.getPlans().add(p);
        }
        this.planningProblemPlanReferences.clear();

        //planningProblemPlanWaitReferences
        for (Pair<Long, Long> pairs : this.planningProblemPlanWaitReferences)
        {
            PlanningProblem s = (PlanningProblem)this.elements.get(pairs.fst);
            Plan p = (Plan)this.elements.get(pairs.snd);
            s.setWaitPlan(p);
        }
        this.planningProblemPlanWaitReferences.clear();

        //planningProblemPlanAlternativeReferences
        for (Pair<Long, Long> pairs : this.planningProblemPlanAlternativeReferences)
        {
            PlanningProblem s = (PlanningProblem)this.elements.get(pairs.fst);
            Plan p = (Plan)this.elements.get(pairs.snd);
            s.setAlternativePlan(p);
        }
        this.planningProblemPlanAlternativeReferences.clear();

        //quantifierScopeReferences
        for (Pair<Long, Long> pairs : this.quantifierScopeReferences)
        {
            AlicaElement ae = (AlicaElement)this.elements.get(pairs.snd);
            Quantifier q = (Quantifier)this.elements.get(pairs.fst);
            q.setScope(this.ae, ae);
        }
        this.quantifierScopeReferences.clear();

        removeRedundancy();
//#ifdef MF_DEBUG
        System.out.println( "MF: DONE!");
//#endif

    }

    private void removeRedundancy() {
        for ( Plan plan : this.rep.getPlans().values())
        {
            ArrayList<Transition> transToRemove = new ArrayList<>();
            for (Transition tran : plan.getTransitions())
            {
                if (tran.getInState() == null)
                {
                    transToRemove.add(tran);
                }
            }

            for (Transition tran : transToRemove)
            {
                plan.getTransitions().remove(tran);
            }
        }
    }


    public void createBehaviour(Document node) {
        Element element = node.getDocumentElement();
        long id = this.parser.parserId(element);
        Behaviour beh = new Behaviour();
        beh.setId(id);

        setAlicaElementAttributes(beh, element);
        addElement(beh);
        this.rep.getBehaviours().put(beh.getId(), beh);
        Node curChild = element.getFirstChild().getNextSibling();
        while (curChild != null)
        {
			String val = curChild.getNodeName();
            long cid = this.parser.parserId(curChild);

            if (configurations.equals(val))
            {
                BehaviourConfiguration bc = createBehaviourConfiguration(curChild);
                this.rep.getBehaviourConfigurations().put(bc.getId(), bc);
                bc.setBehaviour(beh);
                beh.getConfigurations().add(bc);
            }
            else
            {
                ae.abort("MF: Unhandled Behaviour Child:", curChild.getNodeValue());
            }
            curChild = curChild.getNextSibling();

            if("#text".equals(curChild.getNodeName())) {
                curChild = curChild.getNextSibling();
            }
        }
    }

    private BehaviourConfiguration createBehaviourConfiguration(Node element) {
        BehaviourConfiguration b = new BehaviourConfiguration();
        b.setId(this.parser.parserId(element));
        b.setFileName(this.parser.getCurrentFile());

		String attr = element.getAttributes().getNamedItem("masterPlan").getTextContent();
        String attrString = "";
        if (!attr.isEmpty())
        {
            attrString = attr;
            if (attrString.equals("true"))
            {
                b.setMasterPlan(true);
            }
        }

        Node receiveRemoteCommand = element.getAttributes().getNamedItem("receiveRemoteCommand");
        if (receiveRemoteCommand != null && !receiveRemoteCommand.getTextContent().isEmpty())
        {
            attr = receiveRemoteCommand.getTextContent();
            attrString = attr;
            if (attrString.equals("true"))
            {
                b.setEventDriven(true);
            }
        }
        Node visionTriggered = element.getAttributes().getNamedItem("visionTriggered");

        if (visionTriggered != null && !visionTriggered.getTextContent().isEmpty())
        {
            attr = visionTriggered.getTextContent();
            attrString = attr;
            if (attrString.equals("true"))
            {
                b.setEventDriven(true);
            }
        }
        Node eventDriven = element.getAttributes().getNamedItem("eventDriven");

        if (eventDriven != null && !eventDriven.getTextContent().isEmpty())
        {
            attr = eventDriven.getTextContent();
            attrString = attr;
            if (attrString.equals("true"))
            {
                b.setEventDriven(true);
            }
        }
        Node deferring = element.getAttributes().getNamedItem("deferring");

        if (deferring != null && !deferring.getTextContent().isEmpty())
        {
            attr = deferring.getTextContent();
            b.setDeferring(stoi(attr));
        }
        Node frequency = element.getAttributes().getNamedItem("frequency");

        if (frequency != null && !frequency.getTextContent().isEmpty())
        {
            attr = frequency.getTextContent();
            b.setFrequency(stoi(attr));
        }
        setAlicaElementAttributes(b, element);
        this.elements.put(b.getId(), b);
        Node elementFirstChild = element.getFirstChild();

        if (elementFirstChild == null)
            return b;

        Node curChild = elementFirstChild.getNextSibling();

        while (curChild != null)
        {
			String val = curChild.getNodeValue();
            long cid = this.parser.parserId(curChild);
            if (vars.endsWith(val))
            {
                Variable v = createVariable(curChild);
                b.getVariables().add(v);
            }
            else if (parameters.equals(val))
            {
				String key = curChild.getAttributes().getNamedItem("key").getTextContent();
                String value = curChild.getAttributes().getNamedItem("value").getTextContent();

                if (key != null && value != null)
                {
                    b.getParameters().put(key, value);
                }
            }
            else
            {
                ae.abort("MF: Unhandled BehaviourConfiguration Child: " + curChild);
            }
            curChild = curChild.getNextSibling();
        }

        return b;
    }

    public RoleSet createRoleSet(Document doc, Plan masterPlan) {
        Element element = doc.getDocumentElement();

		String def = element.getAttribute("default");
        boolean isDefault = false;
        if (def != null)
        {
            String d = def;
            if (d.equals("true"))
            {
                isDefault = true;
            }
        }

		String pidPtr = element.getAttribute("usableWithPlanID");
        long pid = 0;

        if (pidPtr != null)
        {
            pid = stol(pidPtr);
        }

        boolean isUseable = false;
        if (ignoreMasterPlanId)
        {
            isUseable = true;
        }
        else
        {
            isUseable = pidPtr != null && (pid == masterPlan.getId());
        }

        if (!isDefault && !isUseable)
        {
            ae.abort("MF:Selected RoleSet is not default, nor useable with current masterplan");
        }

        RoleSet rs = new RoleSet();
        rs.setId(this.parser.parserId(element));
        setAlicaElementAttributes(rs, element);
        rs.setIsDefault(isDefault);
        rs.setUsableWithPlanId(pid);
        addElement(rs);

       Node curChild = element.getFirstChild();
        while (curChild != null)
        {
			String val = curChild.getNodeValue();
            if (mappings.equals(val))
            {
                RoleTaskMapping rtm = createRoleTaskMapping(curChild);
                rs.getRoleTaskMappings().add(rtm);
            }
            else
            {
                ae.abort("MF: Unhandled RoleSet Child:", curChild.getNodeValue());
            }
            curChild = curChild.getNextSibling();
        }

        return rs;
    }

    private RoleTaskMapping createRoleTaskMapping(Node element) {
        RoleTaskMapping rtm = new RoleTaskMapping();
        rtm.setId(this.parser.parserId(element));
        setAlicaElementAttributes(rtm, element);
        addElement(rtm);

        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
			String val = curChild.getNodeValue();

            if (taskPriorities.equals(val))
            {
                String keyPtr = curChild.getAttributes().getNamedItem("key").getTextContent();
                String valuePtr = curChild.getAttributes().getNamedItem("value").getTextContent();
                if (keyPtr != null && valuePtr != null)
                {
                    rtm.getTaskPriorities().put(stol(keyPtr), stod(valuePtr));
                }
            }
            else if (role.equals(val))
            {
                long cid = this.parser.parserId(curChild);
                this.rtmRoleReferences.add(new Pair<>(rtm.getId(), cid));
            }
            else
            {
                ae.abort("MF: Unhandled RoleTaskMapping Child ", curChild.getNodeValue());
            }
            curChild = curChild.getNextSibling();
        }

        return rtm;
    }

    public void createTasks(Document doc) {
        Element element = doc.getDocumentElement();
        TaskRepository tr = new TaskRepository();
        tr.setId(this.parser.parserId(element));
        tr.setFileName(this.parser.getCurrentFile());
        addElement(tr);
        setAlicaElementAttributes(tr, element);
        this.rep.getTaskRepositorys().put(tr.getId(), tr);
        long id = 0;
        String defaultTaskPtr = element.getAttribute("defaultTask");
        if (defaultTaskPtr != null)
        {
            id = stol(defaultTaskPtr);
            tr.setDefaultTask(id);
        }

        Node curChild = element.getFirstChild();

        if (curChild.getNodeValue().startsWith("\n"))
            curChild = curChild.getNextSibling();

        while (curChild != null)
        {
            long cid = this.parser.parserId(curChild);

            Task task = new Task(cid == id);
            task.setId(cid);
            setAlicaElementAttributes(task, curChild);
            String descriptionkPtr = curChild.getAttributes().getNamedItem("description").getTextContent();

            if (descriptionkPtr != null)
            {
                task.setDescription(descriptionkPtr);
            }
            addElement(task);
            this.rep.getTasks().put(task.getId(), task);
            task.setTaskRepository(tr);
            tr.getTasks().add(task);

            curChild = curChild.getNextSibling();
            if (curChild.getNodeValue().startsWith("\n"))
                curChild = curChild.getNextSibling();
        }
    }
}
