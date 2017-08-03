package de.uniks.vs.jalica.unknown;

import com.sun.tools.javac.util.Pair;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.parser.PlanParser;
import de.uniks.vs.jalica.teamobserver.PlanRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by alex on 14.07.17.
 */
public class ModelFactory {

    private static final String state = "state";
    private static final String states = "states";
    private static final String task = "task";
    private static final String transitions = "transitions";
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
    private static final String sorts = "sorts";


    private AlicaEngine ae;
    private PlanParser parser;
    private PlanRepository rep;
    private LinkedHashMap<Long, AlicaElement> elements;
    private String entryPoints;
    private String subplan;
    private String postCondition;

    private List<Pair<Long, Long>> epStateReferences;
    private List<Pair<Long, Long>> epTaskReferences;
    private List<Pair<Long, Long>> stateInTransitionReferences;
    private List<Pair<Long, Long>> stateOutTransitionReferences;
    private List<Pair<Long, Long>> statePlanReferences;
    private List<Pair<Long, Long>> paramSubPlanReferences;
    private List<Pair<Long, Long>> paramSubVarReferences;
    private List<Pair<Long, Long>> paramVarReferences;
    private List<Pair<Long, Long>> transitionAimReferences;
    private List<Pair<Long, Long>> conditionVarReferences;
    private List<Pair<Long, Long>> quantifierScopeReferences;
    private List<Pair<Long, Long>> transitionSynchReferences;

    public ModelFactory(AlicaEngine ae, PlanParser parser, PlanRepository rep) {

        this.ae = ae;
        this.parser = parser;
        this.rep = rep;
    }

    public void computeReachabilities() {

    }

    public void attachRoleReferences() {

    }

    public void attachCharacteristicReferences() {

    }

    public Plan createPlan(Document node) {
        Node element = node.getFirstChild();

        long id = this.parser.parserId(element);
        Plan plan = new Plan(id);
        plan.setFileName(this.parser.getCurrentFile());
        setAlicaElementAttributes(plan, element);

        String isMasterPlanAttr = element.getAttributes().getNamedItem("masterPlan").getTextContent();

        if (!isMasterPlanAttr.isEmpty())
        {
//            transform(isMasterPlanAttr.begin(), isMasterPlanAttr.end(), isMasterPlanAttr.begin(), ::tolower);

            isMasterPlanAttr = isMasterPlanAttr.toLowerCase();

            if ("true".equals(isMasterPlanAttr))
            {
                plan.setMasterPlan(true);
            }
        }

        String attr = element.getAttributes().getNamedItem("minCardinality").getTextContent();
        if (!attr.isEmpty())
        {
            plan.setMinCardinality(CommonUtils.stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("maxCardinality").getTextContent();
        if (!attr.isEmpty())
        {
            plan.setMaxCardinality(CommonUtils.stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("utilityThreshold").getTextContent();
        if (!attr.isEmpty())
        {
            plan.setUtilityThreshold(CommonUtils.stod(attr));
        }
        attr = element.getAttributes().getNamedItem("destinationPath").getTextContent();
        if (!attr.isEmpty())
        {
            plan.setDestinationPath(attr);
        }
        // insert into elements ma
        addElement(plan);
        // insert into plan repository map
        this.rep.getPlans().put(plan.getId(), plan);

        Node curChild = element.getFirstChild();
        while (curChild != null)
        {

            if (isReferenceNode(curChild))
            {
                ae.abort("MF: Plan child is reference", curChild.toString());
            }

            String val = curChild.getNodeValue();

            if (entryPoints.equals(val))
            {
                EntryPoint ep = createEntryPoint(curChild);
                plan.getEntryPoints().put(ep.getId(), ep);
                ep.setPlan(plan);
            }
            else if (states.equals(val))
            {
                String name = "";
				String typePtr = curChild.getAttributes().getNamedItem("xsi:type").getTextContent();
                String typeString = "";
                //Normal State
                if (typePtr != null)
                {
                    typeString = typePtr;
                }
                if (typeString.isEmpty())
                {
                    State state = createState(curChild);
                    plan.getStates().add(state);
                    state.setInPlan(plan);

                }
                else if ("alica:SuccessState".equals(typeString))
                {
                    SuccessState suc = createSuccessState(curChild);
                    suc.setInPlan(plan);
                    plan.getSuccessStates().add(suc);
                    plan.getStates().add(suc);

                }
                else if ("alica:FailureState".equals(typeString))
                {
                    FailureState fail = createFailureState(curChild);
                    fail.setInPlan(plan);
                    plan.getFailureStates().add(fail);
                    plan.getStates().add(fail);
                }
                else
                {
                    ae.abort("MF: Unknown State type:", typePtr);
                }
            }
            else if (transitions.equals(val))
            {
                Transition tran = createTransition(curChild, plan);
                plan.getTransitions().add(tran);
            }
//            else if (conditions.compare(val) == 0)
//            {
//				const char* typePtr = curChild.Attribute("xsi:type");
//                string typeString = "";
//                if (typePtr)
//                {
//                    typeString = typePtr;
//                }
//                if (typeString.empty())
//                {
//                    ae.abort("MF: Condition without xsi:type in plan", plan.getName());
//                }
//                else if (typeString.compare("alica:RuntimeCondition") == 0)
//                {
//                    RuntimeCondition* rc = createRuntimeCondition(curChild);
//                    rc.setAbstractPlan(plan);
//                    plan.setRuntimeCondition(rc);
//                }
//                else if (typeString.compare("alica:PreCondition") == 0)
//                {
//                    PreCondition* p = createPreCondition(curChild);
//                    p.setAbstractPlan(plan);
//                    plan.setPreCondition(p);
//                }
//                else if (typeString.compare("alica:PostCondition") == 0)
//                {
//                    PostCondition* p = createPostCondition(curChild);
//                    plan.setPostCondition(p);
//                }
//                else
//                {
//                    ae.abort("MF: Unknown Condition type", curChild);
//                }
//            }
//            else if (vars.compare(val) == 0)
//            {
//                Variable* var = createVariable(curChild);
//                plan.getVariables().push_back(var);
//            }
//            else if (synchronisations.compare(val) == 0)
//            {
//                SyncTransition* st = createSyncTransition(curChild);
//                st.setPlan(plan);
//                plan.getSyncTransitions().push_back(st);
//
//            }
//            else
//            {
//                ae.abort("MF: Unhandled Plan Child: ", val);
//            }
//            curChild = curChild.NextSiblingElement();
        }

        return plan;
    }

    private Transition createTransition(Node element, Plan plan) {
        Transition tran = new Transition();
        tran.setId(this.parser.parserId(element));
        setAlicaElementAttributes(tran, element);
        addElement(tran);
        this.rep.getTransitions().put(tran.getId(), tran);
        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
			String val = curChild.getNodeValue();
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
            curChild = curChild.getNextSibling();
        }
        return tran;
    }

    private PreCondition createPreCondition(Node element) {
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
            cid = CommonUtils.stol(scopePtr);
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

    private FailureState createFailureState(Node element) {
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

    private SuccessState createSuccessState(Node element) {
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

    private PostCondition createPostCondition(Node element) {
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

    private State createState(Node element) {
        State s = new State();
        s.setId(this.parser.parserId(element));
        setAlicaElementAttributes(s, element);

        addElement(s);
        this.rep.getStates().put(s.getId(), s);

        Node curChild = element.getFirstChild();
        while (curChild != null)
        {
			String val = curChild.getNodeValue();
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

            curChild = curChild.getNextSibling();
        }
        return s;
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

    private EntryPoint createEntryPoint(Node element) {
        EntryPoint ep = new EntryPoint();
        ep.setId(this.parser.parserId(element));
        setAlicaElementAttributes(ep, element);
        String attr = element.getAttributes().getNamedItem("minCardinality").getTextContent();
        if (!attr.isEmpty())
        {
            ep.setMinCardinality(CommonUtils.stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("maxCardinality").getTextContent();
        if (!attr.isEmpty())
        {
            ep.setMaxCardinality(CommonUtils.stoi(attr));
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
        Node curChild = element.getFirstChild();
        boolean haveState = false;
        long curChildId;

        while (curChild != null)
        {
			String val = curChild.getNodeValue();
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
            curChild = curChild.getNextSibling();
        }

        if (!haveState)
        {
            ae.abort("MF: No initial state identified for EntryPoint: ", String .valueOf(ep.getId()));
        }

        return ep;
    }

    private boolean isReferenceNode(Node node) {
        Node curChild = node.getFirstChild();
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

    private void addElement(AlicaElement ael) {
        if (this.elements.get(ael.getId()) != this.elements.values().toArray()[this.elements.values().size()-1])
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
}
