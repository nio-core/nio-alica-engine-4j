package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.engine.AlicaCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
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

    protected NotifyTimer timer;
    protected long ttl4Communication;
    protected long ttl4Usage;

    AlicaEngine ae;
    long ownId;
    AlicaCommunication communicator;
    boolean running;
    boolean communicationEnabled;



    Vector<ResultEntry> store;
    ResultEntry ownResults;
    double distThreshold;

    public VariableSyncModule(AlicaEngine ae) {
        running = false;
        this.ae = ae;
        this.timer = null;
        this.store = new Vector<>();
    }

    public void init() {

        if (running) {
            return;
        }

        running = true;
        SystemConfig sc = ae.getSystemConfig();
        communicationEnabled = (Boolean.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.EnableCommunication")));
        ttl4Communication = 1000000 * (Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Communication")));
        ttl4Usage = 1000000 * (Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Usage")));
        ownId = ae.getTeamObserver().getOwnID();
        ownResults = new ResultEntry(ownId, ae);
        store.add(ownResults);
        distThreshold = (Double.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedMergingThreshold")));

        if (communicationEnabled) {
            communicator = ae.getCommunicator();
            int interval = (int) Math.round(1000.0 / (Double.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.CommunicationFrequency"))));
            // TODO: implementation
//            timer = new NotifyTimer<VariableSyncModule>(interval, VariableSyncModule::publishContent, this);
            timer = new NotifyTimer(interval, this);
            timer.start();
        }
    }

    public void close() {
        this.running = false;

        if (timer != null) {
            // TODO: implementation
//            timer.stop();
        }
        timer = null;
    }

    public void clear() {

        for (ResultEntry r : store) {
            r.clear();
        }
    }

    public void onSolverResult(SolverResult msg) {

        if (msg.senderID == ownId) {
            return;
        }

        if (ae.getTeamObserver().isAgentIgnored(msg.senderID)) {
            return;
        }
        boolean found = false;
        ResultEntry re = null;

        for (int i = 0; i < this.store.size(); ++i) {
            re = store.get(i);

            if (re.getId() == msg.senderID) {
                found = true;
                break;
            }
        }

        //Lockguard here!
        if (!found) {
            re = new ResultEntry(msg.senderID, ae);
            this.store.add(re);
        }
        for (SolverVar sv : msg.vars) {
            Vector<Integer> tmp = new Vector<Integer>(sv.value);
            re.addValue(sv.id, tmp);
        }
    }
    public void publishContent() {

        if (!this.running)
            return;

        if (!ae.isMaySendMessages())
            return;
        Vector<SolverVar> lv = ownResults.getCommunicatableResults(ttl4Communication);

        if (lv.size() == 0)
            return;
        SolverResult sr = new SolverResult();
        sr.senderID = ownId;
        sr.vars = lv;
        communicator.sendSolverResult(sr);
    }
    public void postResult(long vid, Vector<Integer> result) {
        this.ownResults.addValue(vid, result);
    }

    public Vector<Vector<Vector<Integer>>> getSeeds(Vector<Variable> query, Vector<Vector<Double>> limits) {
        //Lockguard here!
        int dim = query.size();
		/*cout << "VSyncMod:";
		for(auto& avar : *query) {
			cout << " " << avar.getId();
		}
		cout << endl;*/
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
            Vector<Vector<Integer>> vec = re.getValues(query, this.ttl4Usage);

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
