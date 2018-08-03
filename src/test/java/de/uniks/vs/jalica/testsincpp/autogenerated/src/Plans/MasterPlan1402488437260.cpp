#include "Plans/MasterPlan1402488437260.h"
using namespace alica;
/*PROTECTED REGION ID(eph1402488437260) ENABLED START*/ //Add additional using directives here
/*PROTECTED REGION END*/
namespace alicaAutogenerated
{
//Plan:MasterPlan

/* generated comment
 
 Task: DefaultTask  -> EntryPoint-ID: 1402488437263

 */
shared_ptr<UtilityFunction> UtilityFunction1402488437260::getUtilityFunction(Plan* plan)
{
  /*PROTECTED REGION ID(1402488437260) ENABLED START*/

  shared_ptr < UtilityFunction > defaultFunction = make_shared < DefaultUtilityFunction > (plan);
  return defaultFunction;

  /*PROTECTED REGION END*/
}

//State: Attack in Plan: MasterPlan

/*
 *		
 * Transition:
 *   - Name: MISSING_NAME, ConditionString: , Comment : AttackToGoal 
 *
 * Plans in State: 				
 *   - Plan - (Name): AttackDefault, (PlanID): 1402488866727 
 *
 * Tasks: 
 *   - DefaultTask (1225112227903) (Entrypoint: 1402488437263)
 *
 * States:
 *   - Attack (1402488437261)
 *   - Defend (1402488463437)
 *   - Goal (1402488470615)
 *   - MidField (1402488477650)
 *   - SucGoalState (1402488536570)
 *
 * Vars:
 */
bool TransitionCondition1402488519140::evaluate(shared_ptr<RunningPlan> rp)
{
  /*PROTECTED REGION ID(1402488517667) ENABLED START*/
  return false;
  /*PROTECTED REGION END*/

}

/*
 *		
 * Transition:
 *   - Name: MISSING_NAME, ConditionString: , Comment : AttackToDefend 
 *
 * Plans in State: 				
 *   - Plan - (Name): AttackDefault, (PlanID): 1402488866727 
 *
 * Tasks: 
 *   - DefaultTask (1225112227903) (Entrypoint: 1402488437263)
 *
 * States:
 *   - Attack (1402488437261)
 *   - Defend (1402488463437)
 *   - Goal (1402488470615)
 *   - MidField (1402488477650)
 *   - SucGoalState (1402488536570)
 *
 * Vars:
 */
bool TransitionCondition1409218319990::evaluate(shared_ptr<RunningPlan> rp)
{
  /*PROTECTED REGION ID(1409218318661) ENABLED START*/
  return false;
  /*PROTECTED REGION END*/

}

//State: Defend in Plan: MasterPlan

//State: Goal in Plan: MasterPlan

/*
 *		
 * Transition:
 *   - Name: MISSING_NAME, ConditionString: , Comment : GoalToSucGoal 
 *
 * Plans in State: 				
 *   - Plan - (Name): GoalPlan, (PlanID): 1402488870347 
 *
 * Tasks: 
 *   - DefaultTask (1225112227903) (Entrypoint: 1402488437263)
 *
 * States:
 *   - Attack (1402488437261)
 *   - Defend (1402488463437)
 *   - Goal (1402488470615)
 *   - MidField (1402488477650)
 *   - SucGoalState (1402488536570)
 *
 * Vars:
 */
bool TransitionCondition1402488558741::evaluate(shared_ptr<RunningPlan> rp)
{
  /*PROTECTED REGION ID(1402488557864) ENABLED START*/
  return false;
  /*PROTECTED REGION END*/

}

//State: MidField in Plan: MasterPlan

/*
 *		
 * Transition:
 *   - Name: MISSING_NAME, ConditionString: , Comment : MidFieldToGoal 
 *
 * Plans in State: 				
 *   - Plan - (Name): NewBehaviourDefault, (PlanID): 1402488712657 				
 *   - Plan - (Name): MidDefendDefault, (PlanID): 1402488763903 				
 *   - Plan - (Name): MidFieldPlayPlan, (PlanID): 1402488770050 
 *
 * Tasks: 
 *   - DefaultTask (1225112227903) (Entrypoint: 1402488437263)
 *
 * States:
 *   - Attack (1402488437261)
 *   - Defend (1402488463437)
 *   - Goal (1402488470615)
 *   - MidField (1402488477650)
 *   - SucGoalState (1402488536570)
 *
 * Vars:
 */
bool TransitionCondition1402488520968::evaluate(shared_ptr<RunningPlan> rp)
{
  /*PROTECTED REGION ID(1402488519757) ENABLED START*/
  return false;
  /*PROTECTED REGION END*/

}

//State: SucGoalState in Plan: MasterPlan

}
