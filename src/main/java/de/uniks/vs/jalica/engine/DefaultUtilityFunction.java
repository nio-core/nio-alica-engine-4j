package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.planselection.IAssignment;

import java.util.ArrayList;

public class DefaultUtilityFunction extends UtilityFunction {

    public DefaultUtilityFunction(Plan plan) {
        super("DefaultUtility", new ArrayList <USummand>(), 1.0, 0.0, plan);
    }

    public double eval(RunningPlan newRP, RunningPlan oldRP)
    {
        if (newRP.getAssignment() == null)
        {
            System.out.println("DefUF: The Assignment of the RunningPlan is null!" );
            CommonUtils.aboutError("");
        }
        // Invalid Assignments have an Utility of -1 changed from 0 according teamObserver specs
        if (!newRP.getAssignment().isValid())
        {
            return -1.0;
        }
        UtilityInterval sumOfUI = new UtilityInterval(0.0, 0.0);
        double sumOfWeights = 0.0;

        // Sum up priority summand
        UtilityInterval prioUI = this.getPriorityResult(newRP.getAssignment());
        sumOfUI.setMax(sumOfUI.getMax() + this.priorityWeight * prioUI.getMax());
        sumOfUI.setMin(sumOfUI.getMin() + this.priorityWeight * prioUI.getMin());
        sumOfWeights += this.priorityWeight;

        if (oldRP != null && this.similarityWeight > 0.0)
        {
            // Sum up similarity summand
            UtilityInterval simUI = this.getSimilarity(newRP.getAssignment(), oldRP.getAssignment());
            sumOfUI.setMax(sumOfUI.getMax() + this.similarityWeight * simUI.getMax());
            sumOfUI.setMin(sumOfUI.getMin() + this.similarityWeight * simUI.getMin());
            sumOfWeights += this.similarityWeight;
        }

        // Normalize teamObserver 0..1
        if (sumOfWeights > 0.0)
        {
            sumOfUI.setMax(sumOfUI.getMax() / sumOfWeights);
            sumOfUI.setMin(sumOfUI.getMin() / sumOfWeights);

            if ((sumOfUI.getMax() - sumOfUI.getMin()) > DIFFERENCETHRESHOLD)
            {
                System.out.println("DefUF: The Min and Max utility differs more than " + DIFFERENCETHRESHOLD
                        + " for a complete Assignment!");
            }
            return sumOfUI.getMax();
        }

        return 0.0;
    }


    public UtilityInterval eval(IAssignment newAss, IAssignment oldAss) {
        UtilityInterval sumOfUI  = new UtilityInterval(0.0, 0.0);
        double sumOfWeights = 0.0;

        // Sum up priority summand
        UtilityInterval prioUI = this.getPriorityResult(newAss);
        sumOfUI.setMax(sumOfUI.getMax() + this.priorityWeight * prioUI.getMax());
        sumOfUI.setMin(sumOfUI.getMin() + this.priorityWeight * prioUI.getMin());
        sumOfWeights += this.priorityWeight;
//#ifdef UFDEBUG
        System.out.println("DF: prioUI.Min = " + prioUI.getMin() );
        System.out.println("DF: prioUI.Max = " + prioUI.getMax() );
        System.out.println("DF: priorityWeight = " + priorityWeight);
//#endif
        if (oldAss != null && this.similarityWeight > 0.0)
        {
            // Sum up similarity summand
            UtilityInterval simUI = this.getSimilarity(newAss, oldAss);
            sumOfUI.setMax(sumOfUI.getMax() + this.similarityWeight * simUI.getMax());
            sumOfUI.setMin(sumOfUI.getMin() + this.similarityWeight * simUI.getMin());
            sumOfWeights += this.similarityWeight;
        }

        // Normalize teamObserver 0..1
        if (sumOfWeights > 0.0)
        {
            sumOfUI.setMax(sumOfUI.getMax() / sumOfWeights);
            sumOfUI.setMin(sumOfUI.getMin() / sumOfWeights);
            return sumOfUI;
        }

        sumOfUI.setMin(0.0);
        sumOfUI.setMax(0.0);
        return sumOfUI;
    }
}
