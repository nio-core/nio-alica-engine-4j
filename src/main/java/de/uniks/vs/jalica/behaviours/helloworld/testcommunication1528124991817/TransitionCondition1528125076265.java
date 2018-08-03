package de.uniks.vs.jalica.behaviours.helloworld.testcommunication1528124991817;

import de.uniks.vs.jalica.behaviours.DomainCondition;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.RunningPlan;

public class TransitionCondition1528125076265 extends DomainCondition {
//Plan:TestCommunication

    /* generated comment

     Task: Search  -> EntryPoint-ID: 1528125008668

     Task: Fetch  -> EntryPoint-ID: 1528125016366


        //State: Publish in Plan: TestCommunication

        //State: Subscribe in Plan: TestCommunication

        /*
         *
         * Transition:
         *   - Name: MISSING_NAME, ConditionString: , Comment : Read from publisher
         *
         * Plans in State:
         *   - Plan - (Name): SubscribeDefault, (PlanID): 1528125256074
         *
         * Tasks:
         *   - Search (1481544910904) (Entrypoint: 1528125008668)
         *   - Fetch (1468494562045) (Entrypoint: 1528125016366)
         *
         * States:
         *   - Publish (1528125008667)
         *   - Subscribe (1528125050252)
         *   - Success (1528125060365)
         *
         * Vars:
         */
    protected boolean evaluate(RunningPlan rp) {
        /*PROTECTED REGION ID(1528125075116) ENABLED START*/
        System.out.println("TC: " + rp.getActiveState().getName());
        CommonUtils.aboutImplIncomplete();
        return true;
        /*PROTECTED REGION END*/
    }

//State: Success in Plan: TestCommunication
}

