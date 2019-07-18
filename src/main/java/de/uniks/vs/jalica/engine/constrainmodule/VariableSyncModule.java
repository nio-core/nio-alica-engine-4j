package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.common.Comparable;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.IAlicaCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.UtilityInterval;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.common.NotifyTimer;
import de.uniks.vs.jalica.engine.containers.SolverResult;
import de.uniks.vs.jalica.engine.containers.SolverVar;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public class VariableSyncModule {

    private ResultEntry ownResults;
    private ArrayList<ResultEntry> store;
    private SolverResult publishData;
    private AlicaTime ttl4Communication;
    private AlicaTime ttl4Usage;
    private AlicaEngine ae;
    private IAlicaCommunication communicator;
    private boolean running;
    private double distThreshold;
    private NotifyTimer<VariableSyncModule> timer;
    private Lock mutex;

    public VariableSyncModule(AlicaEngine ae) {
        this.ae = ae;
        this.publishData = new SolverResult();
        this.running = false;
        this.timer = null;
        this.distThreshold = 0;
        this.communicator = null;
        this.ttl4Communication = AlicaTime.zero();
        this.ttl4Usage = AlicaTime.zero();
        this.ownResults = null;
        this.store = new ArrayList<>();
    }

    public void init() {
        assert(!this.running);

        if (this.running)
            return;

        this.running = true;

        SystemConfig sc = this.ae.getSystemConfig();
//        ttl4Communication = 1000000 * (Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Communication")));
//        ttl4Usage = 1000000 * (Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Usage")));
        boolean communicationEnabled = (Boolean.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.EnableCommunication")));
        this.ttl4Communication = new AlicaTime().inMilliseconds(Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Communication")));
        this.ttl4Usage = new AlicaTime().inMilliseconds(Long.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedTTL4Usage")));
        this.distThreshold = (Double.valueOf((String) sc.get("Alica").get("Alica.CSPSolving.SeedMergingThreshold")));

        ID ownID = this.ae.getTeamManager().getLocalAgentID();
        synchronized (this) {
            this.ownResults = new ResultEntry(ownID);
            this.store.add(ownResults);
        }
        this.publishData.senderID = ownID;

        if (communicationEnabled) {
            this.communicator = this.ae.getCommunicator();
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

        if ((msg.senderID == ownResults.getId()) || (ae.getTeamManager().isAgentIgnored(msg.senderID)))
            return;
        ResultEntry resultEntry = null;

        for (ResultEntry re : this.store) {

            if ((re.getId()) == (msg.senderID)) {
                resultEntry = re;
                break;
            }
        }

        if (resultEntry == null) {
            ResultEntry newEntry = new ResultEntry(msg.senderID);
//            std::lock_guard < std::mutex > lock(_mutex);
            synchronized (this.store) {

                int index = CommonUtils.upperBound(this.store, newEntry, (Comparable<ResultEntry>) (a, b) -> a.getId().asLong() < b.getId().asLong());
                this.store.add(index, newEntry);
//                auto agent_sorted_loc = std::upperBound (_store.begin(), _store.end(), newEntry,
//                [](const std::unique_ptr < ResultEntry > & a, const std::unique_ptr < ResultEntry > & b){
//                    return (a.getId() < b.getId());
//                });
//                agent_sorted_loc = this.store.insert(agent_sorted_loc, std::move (newEntry));
//                resultEntry = ( * agent_sorted_loc).get();
            }
        }
        AlicaTime now = this.ae.getAlicaClock().now();

        for (SolverVar sv : msg.vars) {
            Variant v = new Variant();
            v.loadFrom(sv.value);
            resultEntry.addValue(sv.id, v, now);
        }
    }

    public void publishContent() {

        if ((!this.running) || (!ae.maySendMessages()))
            return;

        this.publishData.vars.clear();
        if (this.ae.getAlicaClock() != null) {
            AlicaTime now = this.ae.getAlicaClock().now();
            this.ownResults.getCommunicatableResults(new AlicaTime(now.time - this.ttl4Communication.time), this.publishData.vars);
            if (this.publishData.vars.isEmpty()) {
                return;
            }
            if (this.communicator != null) {
                this.communicator.sendSolverResult(this.publishData);
            }
        }
    }

    public void postResult(long vid, Variant result) {
        this.ownResults.addValue(vid, result, this.ae.getAlicaClock().now());
    }


    class VotedSeed {
        ArrayList<Variant> values;
        ArrayList<Integer> supporterCount; // WARNING: initializer order dependency! Do not move freely!
        double hash;
        int totalSupCount;

        public VotedSeed(ArrayList<Variant> vs) {
//            std::move(vs)
            this.values = new ArrayList<>(vs);
            vs.clear();
            this.supporterCount = new ArrayList<>(this.values.size());
            this.hash = 0;
            this.totalSupCount = 0;
            assert (this.values.size() == this.supporterCount.size());

            for (Variant v : this.values) {

                if (v.isSet()) {
                    this.totalSupCount++;
                }
            }
        }

        public VotedSeed(VotedSeed o) {
            this.transfer(o);
////        : _values(std::move(o._values))
//            this.values = new ArrayList<>(o.values);
//            o.values.clear();
////                , _supporterCount(std::move(o._supporterCount))
//            this.supporterCount = new ArrayList<>(o.supporterCount);
//            o.supporterCount.clear();
//            this.hash = o.hash;
//            this.totalSupCount = o.totalSupCount;
        }

        public VotedSeed transfer(VotedSeed o) {
//        : _values(std::move(o._values))
            this.values = new ArrayList<>(o.values);
            o.values.clear();
//            std::move(o._supporterCount)
            this.supporterCount = new ArrayList<>(o.supporterCount);
            o.supporterCount.clear();
            this.hash = o.hash;
            this.totalSupCount = o.totalSupCount;
            return this;
        }

        boolean takeVector(ArrayList<Variant> v, ArrayList<UtilityInterval> limits, double distThreshold) {
            int nans = 0;
            int dim = v.size();
            double distSqr = 0;

            for (int i = 0; i < dim; ++i) {
                if (v.get(i).isDouble()) {
                    if (this.values.get(i).isDouble()) {
                        double d = v.get(i).getDouble();

                        if (!Double.isNaN(d)) {
                            double cur = this.values.get(i).getDouble();
                            double dist = (d - cur) * (d - cur);
                            double size = limits.get(i).size();
                            if (size > 0.0) {
                                dist /= size * size;
                            }
                            distSqr += dist;
                        } else {
                            ++nans;
                        }
                    }
                } else {
                    ++nans;
                }
            }

            if (dim == nans) {
                return true; // silently absorb a complete NaN vector
            }
            if (distSqr / (dim - nans) < distThreshold) { // merge
                for (int i = 0; i < dim; ++i) {
                    if (v.get(i).isDouble()) {
                        double d = v.get(i).getDouble();
                        if (!Double.isNaN(d)) {
                            if (this.values.get(i).isDouble()) {
                                double nv = this.values.get(i).getDouble() * this.supporterCount.get(i) + d;
                                this.supporterCount.set(i, this.supporterCount.get(i)+1);
                                nv /= this.supporterCount.get(i);
                                this.values.get(i).setDouble(nv);
                            } else {
                                this.supporterCount.set(i, 1);
                                this.values.get(i).setDouble(d);
                            }
                            this.totalSupCount++;
                        }
                    }
                }
                // recalc hash:
                this.hash = 0;
                for ( Variant v2 : this.values) {
                    if (v2.isDouble()) {
                        this.hash += v2.getDouble();
                    }
                }
                return true;
            }
            return false;
        }

    }

//
//    public ArrayList<ArrayList<ArrayList<Integer>>> getSeeds(ArrayList<Variable> query, ArrayList<ArrayList<Double>> limits) {
//        //Lockguard here!
//        int dim = query.size();
//         if (CommonUtils.VSM_DEBUG_debug) {
//             System.out.print("VSyncMod:");
//             for (Variable avar : query) {
//                 System.out.print(" " + avar.getID());
//             }
//             System.out.println();
//         }
//        ArrayList<VotedSeed> seeds = new ArrayList<>();
//        ArrayList<Double> scaling = new ArrayList<>();
//        scaling.setSize(dim);
//
//        for(int i=0; i<dim; i++) {
//            scaling.set(i, (limits.get(i).get(1)-limits.get(i).get(0)));
//            scaling.set(i, scaling.get(i)*scaling.get(i)); //Sqr it for dist calculation speed up
//        }
////		cout << "VSM: Number of Seeds in Store: " << this.store.size() << endl;
//
//        for(int i=0; i<this.store.size(); i++) {
//            ResultEntry re = this.store.get(i); //allow for lock free iteration (no value is deleted from store)
//            ArrayList<ArrayList<Integer>> vec = re.getValues(query, this.ttl4Usage.time);
//
//            if(vec==null) {
//                continue;
//            }
//            boolean found = false;
//
//            for(VotedSeed s : seeds) {
//
//                if(s.takeVector(vec,scaling,distThreshold, true)) {
//                    found = true;
//                    break;
//                }
//            }
//
//            if(!found) {
//                seeds.add(new VotedSeed(dim,vec));
//            }
//        }
////#ifdef RS_DEBUG
////        cout << "RS: Generated "<< seeds.size() << "seeds" << endl;
////        for(int i=0; i<seeds.size(); i++)
////        {
////            cout << "Seed " << i; // (sup:{1}): ",i);
////            int i=0;
////            for(auto j=seeds.begin(); j!=seeds.end(); j++, i++)
////            {
////                cout << (*j).values.at(i) << "\t";
////            }
////            cout << endl;
////        }
////
////#endif
//
//        int maxNum = Math.min((int)seeds.size(),dim);
//        ArrayList<ArrayList<ArrayList<Integer>>> ret = new ArrayList<ArrayList<ArrayList<Integer>>>();
//        ret.setSize(maxNum);
//
//        seeds.sort(new Comparator<VotedSeed>() {
//            @Override
//            public int compare(VotedSeed a, VotedSeed b) {
//
//                if(a.getTotalSupCount() != b.getTotalSupCount()) {
//                    return a.getTotalSupCount() > b.getTotalSupCount() ? 1 : -1;
//                }
//                else {
//                    if (a.getValues() == null || a.getValues().size() == 0) return 1;
//                    if (b.getValues() == null || b.getValues().size() == 0) return -1;
//
//                    return a.getHash() > b.getHash() ? 1 : -1;
//                    //return &*a > &*b;
//                }
//            }
//        });
//
////        seeds.sort([](shared_ptr<VotedSeed>& a, shared_ptr<VotedSeed>& b){
////
////            if(a.totalSupCount != b.totalSupCount) {
////                return a.totalSupCount > b.totalSupCount;
////            }
////            else
////            {
////                if(a.values == null || a.values.size()==0) return true;
////                if(b.values == null || b.values.size()==0) return false;
////
////                return a.hash > b.hash;
////                //return &*a > &*b;
////            }
////        });
//
//        for(int i=0; i<maxNum; i++) {
//            VotedSeed iter = seeds.get(i);
//            ret.set(i, iter.getValues());
//        }
////		cout << "VSM: Number of present seeds: " << ret.size() << " dim: "<< dim << " seedcount: "<< seeds.size() << endl;
//        return ret;
//    }
}
