package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class AuthorityManager {

    Vector<AllocationAuthorityInfo> queue = new Vector<>();
    AlicaEngine ae;
    int ownID;

    public AuthorityManager(AlicaEngine ae) {
        this.ae = ae;
        this.ownID = 0;
    }

    public void init() {
        this.ownID = ae.getTeamObserver().getOwnID();
    }

    public void tick(RunningPlan rp) {
//        #ifdef AM_DEBUG
        System.out.println("AM: Tick called! <<<<<<" );
//#endif
//        lock_guard<mutex> lock(mu);
        processPlan(rp);
        this.queue.clear();
    }

    private void processPlan(RunningPlan rp)
    {
        if (rp == null || rp.isBehaviour())
        {
            return;
        }
        if (rp.getCycleManagement().needsSending())
        {
            sendAllocation(rp);
            rp.getCycleManagement().sent();
        }
//#ifdef AM_DEBUG
        System.out.println("AM: Queue size of AuthorityInfos is " + this.queue.size() );
//#endif
        for (int i = 0; i < this.queue.size(); i++)
        {
            if (authorityMatchesPlan(this.queue.get(i), rp))
            {
//#ifdef AM_DEBUG
                System.out.println( "AM: Found AuthorityInfo, which matches the plan " + rp.getPlan().getName() );
//#endif
                rp.getCycleManagement().handleAuthorityInfo(this.queue.get(i));
                this.queue.remove(this.queue.get(i));
                i--;
            }
        }
        for (RunningPlan c : rp.getChildren())
        {
            processPlan(c);
        }

    }

    private boolean authorityMatchesPlan(AllocationAuthorityInfo aai, RunningPlan p) {
        RunningPlan shared = p.getParent();
//        auto shared = p.getParent().lock();
/*#ifdef AM_DEBUG
		if (!p.getParent().expired())
		{
			cout << "AM: Parent-WeakPtr is NOT expired!" << endl;
			cout << "AM: Parent-ActiveState is: " << (shared.getActiveState() != nullptr ? shared.getActiveState().getID() : NULL) << endl;
			cout << "AM: AAI-ParentState is: " << aai.parentState << endl;
		}
		else
		{
			cout << "AM: Parent-WeakPtr is expired!" << endl;
			cout << "AM: Current-ActiveState is: " << p.getActiveState().getID() << endl;
			cout << "AM: AAI-ParentState is: " << aai.parentState << endl;
		}
#endif*/

        if ((p.getParent() != null && aai.parentState == -1)
                || (p.getParent() == null && shared.getActiveState() != null && shared.getActiveState().getId() == aai.parentState)) {

            if (p.getPlan().getId() == aai.planId) {
                return true;
            }
			else if (aai.planType != -1 && p.getPlanType() != null && p.getPlanType().getId() == aai.planType) {
                return true;
            }
        }
        return false;
    }

    public void handleIncomingAuthorityMessage(AllocationAuthorityInfo aai) {
        CommonUtils.aboutNoImpl();
    }

    public void close() {CommonUtils.aboutNoImpl();}

    public void sendAllocation(RunningPlan p) {CommonUtils.aboutNoImpl();}
}
