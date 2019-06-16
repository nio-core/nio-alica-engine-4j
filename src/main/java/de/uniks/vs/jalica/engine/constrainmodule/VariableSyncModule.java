package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.model.Variable;
import de.uniks.vs.jalica.engine.common.NotifyTimer;
import de.uniks.vs.jalica.engine.containers.SolverResult;
import de.uniks.vs.jalica.engine.containers.SolverVar;
import de.uniks.vs.jalica.engine.common.VotedSeed;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class VariableSyncModule {

    protected AlicaTime ttl4Communication;
    protected AlicaTime ttl4Usage;
    protected Vector<ResultEntry> store;
    protected ResultEntry ownResults;
    protected SolverResult publishData;

    protected AlicaEngine ae;
    protected AlicaCommunication communicator;
    protected NotifyTimer timer;
    protected long ownID;
    protected double distThreshold;
    protected boolean running;
    protected boolean communicationEnabled;

    public VariableSyncModule(AlicaEngine ae) {
        this.running = false;
        this.ae = ae;
        this.timer = null;
        this.publishData = new SolverResult();
        this.store = new Vector<>();
    }

    public void init() {

        if (running)
            return;
        running = true;

        SystemConfig sc = ae.getSystemConfig();
//        ttl4Communication = 1000000 * (Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Communication")));
//        ttl4Usage = 1000000 * (Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Usage")));
        communicationEnabled = (Boolean.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.EnableCommunication")));
        ttl4Communication = new AlicaTime().inMilliseconds(Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Communication")));
        ttl4Usage = new AlicaTime().inMilliseconds(Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Usage")));
        distThreshold = (Double.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedMergingThreshold")));

        ownID = ae.getTeamObserver().getOwnID();
        ownResults = new ResultEntry(ownID, ae);
        store.add(ownResults);

        this.publishData.senderID = ownID;

        if (communicationEnabled) {
            communicator = ae.getCommunicator();
//            int interval = (int) Math.round(1000.0 / (Double.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.CommunicationFrequency"))));
            AlicaTime interval =  new AlicaTime().inSeconds(1.0 / (Double.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.CommunicationFrequency"))));

            if (timer == null)
                timer = new NotifyTimer(interval.inMilliseconds(), this);
            timer.start();
        }
    }

    public void close() {
        this.running = false;

        if (timer != null) {
            timer.stopIt();
        }
    }

    public void clear() {

        for (ResultEntry r : store) {
            r.clear();
        }
    }

    public void onSolverResult(SolverResult msg) {

        if ((msg.senderID == ownID) || (ae.getTeamObserver().isAgentIgnored(msg.senderID)))
            return;
        ResultEntry resultEntry = null;

        for (ResultEntry re : this.store) {

            if ((re.getId()) == (msg.senderID)) {
                resultEntry = re;
                break;
            }
        }

        if (resultEntry == null) {
            resultEntry = new ResultEntry(msg.senderID, ae);
//            auto agent_sorted_loc = std::upper_bound(_store.begin(), _store.end(), new_entry,
//            [](const std::unique_ptr<ResultEntry>& a, const std::unique_ptr<ResultEntry>& b) {
//                return (a->getId() < b->getId());
//            });
//            agent_sorted_loc = this.store.add(agent_sorted_loc, std::move(new_entry));
//            re = (*agent_sorted_loc).get();
            int index = 0;

            for (; index < this.store.size(); index++)

                if (this.store.get(index).getId() > resultEntry.getId())
                    break;

            this.store.insertElementAt(resultEntry, index);
            CommonUtils.aboutCallNotification("VSM: ResultEntry:" + resultEntry +"inserted at position " + index);
        }
        AlicaTime now = ae.getAlicaClock().now();

        for (SolverVar sv : msg.vars) {
            Vector<Integer> tmp = new Vector<>(sv.value);
            resultEntry.addValue(sv.id, tmp, now);
            //TODO: implementation of Variant
//            Variant v;
//            v.loadFrom(sv.value);
//            re.addValue(sv.id, v, now);
        }

//        boolean found = false;
//
//        for (int i = 0; i < this.store.size(); ++i) {
//            re = store.get(i);
//
//            if (re.getId() == msg.senderID) {
//                found = true;
//                break;
//            }
//        }
//
//        if (!found) {
//            re = new ResultEntry(msg.senderID, ae);
//            synchronized (this.store) {
//                this.store.add(re);
//            }
//        }
//        for (SolverVar sv : msg.vars) {
//            Vector<Integer> tmp = new Vector<Integer>(sv.value);
//            re.addValue(sv.id, tmp);
//        }
    }
    public void publishContent() {

        if ((!this.running) || (!ae.isMaySendMessages()))
            return;

        AlicaTime now = ae.getAlicaClock().now();
        Vector<SolverVar> lv = ownResults.getCommunicatableResults(now.time - ttl4Communication.time);
//        Vector<SolverVar> lv = ownResults.getCommunicatableResults(ttl4Communication.time);

        if (lv.size() == 0)
            return;
        SolverResult sr = new SolverResult();
        sr.senderID = ownID;
        sr.vars = lv;
        communicator.sendSolverResult(sr);
    }

    public void postResult(long vid, Vector<Integer> result) {
        this.ownResults.addValue(vid, result, ae.getAlicaClock().now());
    }

    public Vector<Vector<Vector<Integer>>> getSeeds(Vector<Variable> query, Vector<Vector<Double>> limits) {
        //Lockguard here!
        int dim = query.size();
         if (CommonUtils.VSM_DEBUG_debug) {
             System.out.print("VSyncMod:");
             for (Variable avar : query) {
                 System.out.print(" " + avar.getID());
             }
             System.out.println();
         }
        List<VotedSeed> seeds = new Vector<>();
        Vector<Double> scaling = new Vector<>();
        scaling.setSize(dim);

        for(int i=0; i<dim; i++) {
            scaling.set(i, (limits.get(i).get(1)-limits.get(i).get(0)));
            scaling.set(i, scaling.get(i)*scaling.get(i)); //Sqr it for dist calculation speed up
        }
//		cout << "VSM: Number of Seeds in Store: " << this.store.size() << endl;

        for(int i=0; i<this.store.size(); i++) {
            ResultEntry re = this.store.get(i); //allow for lock free iteration (no value is deleted from store)
            Vector<Vector<Integer>> vec = re.getValues(query, this.ttl4Usage.time);

            if(vec==null) {
                continue;
            }
            boolean found = false;

            for(VotedSeed s : seeds) {

                if(s.takeVector(vec,scaling,distThreshold, true)) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                seeds.add(new VotedSeed(dim,vec));
            }
        }
//#ifdef RS_DEBUG
//        cout << "RS: Generated "<< seeds.size() << "seeds" << endl;
//        for(int i=0; i<seeds.size(); i++)
//        {
//            cout << "Seed " << i; // (sup:{1}): ",i);
//            int i=0;
//            for(auto j=seeds.begin(); j!=seeds.end(); j++, i++)
//            {
//                cout << (*j).values.at(i) << "\t";
//            }
//            cout << endl;
//        }
//
//#endif

        int maxNum = Math.min((int)seeds.size(),dim);
        Vector<Vector<Vector<Integer>>> ret = new Vector<Vector<Vector<Integer>>>();
        ret.setSize(maxNum);

        seeds.sort(new Comparator<VotedSeed>() {
            @Override
            public int compare(VotedSeed a, VotedSeed b) {

                if(a.getTotalSupCount() != b.getTotalSupCount()) {
                    return a.getTotalSupCount() > b.getTotalSupCount() ? 1 : -1;
                }
                else {
                    if (a.getValues() == null || a.getValues().size() == 0) return 1;
                    if (b.getValues() == null || b.getValues().size() == 0) return -1;

                    return a.getHash() > b.getHash() ? 1 : -1;
                    //return &*a > &*b;
                }
            }
        });

//        seeds.sort([](shared_ptr<VotedSeed>& a, shared_ptr<VotedSeed>& b){
//
//            if(a.totalSupCount != b.totalSupCount) {
//                return a.totalSupCount > b.totalSupCount;
//            }
//            else
//            {
//                if(a.values == null || a.values.size()==0) return true;
//                if(b.values == null || b.values.size()==0) return false;
//
//                return a.hash > b.hash;
//                //return &*a > &*b;
//            }
//        });

        for(int i=0; i<maxNum; i++) {
            VotedSeed iter = seeds.get(i);
            ret.set(i, iter.getValues());
        }
//		cout << "VSM: Number of present seeds: " << ret.size() << " dim: "<< dim << " seedcount: "<< seeds.size() << endl;
        return ret;
    }
}
