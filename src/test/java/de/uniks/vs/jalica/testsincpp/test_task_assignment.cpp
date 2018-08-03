/*
 * test_task_assignment.cpp
 *
 *  Created on: Jul 29, 2014
 *      Author: Stefan Jakob
 */

using namespace std;

#include <gtest/gtest.h>
#include <list>
#include <vector>
#include <memory>

#include "engine/AlicaEngine.h"
#include "engine/planselector/TaskAssignment.h"
#include "engine/planselector/PlanSelector.h"
#include "engine/IPlanSelector.h"
#include "engine/ITeamObserver.h"
#include "engine/collections/RobotEngineData.h"
#include "engine/model/AbstractPlan.h"
#include "engine/RunningPlan.h"
#include "engine/PlanRepository.h"
#include "engine/model/Plan.h"
#include <clock/AlicaROSClock.h>
#include "BehaviourCreator.h"
#include "ConditionCreator.h"
#include "ConstraintCreator.h"
#include "UtilityFunctionCreator.h"


class TaskAssignmentTest : public ::testing::Test
{
protected:
	alica::AlicaEngine* ae;
	supplementary::SystemConfig* sc;
	alica::BehaviourCreator* bc;
	alica::ConditionCreator* cc;
	alica::UtilityFunctionCreator* uc;
	alica::ConstraintCreator* crc;

	virtual void SetUp()
	{
		// determine the path to the test config
		string path = supplementary::FileSystem::getSelfPath();
		int place = path.rfind("devel");
		path = path.substr(0, place);
		path = path + "src/alica/alica_test/src/test";

		// bring up the SystemConfig with the corresponding path
		sc = supplementary::SystemConfig::getInstance();
		sc->setRootPath(path);
		sc->setConfigPath(path + "/etc");
		cout << sc->getConfigPath() << endl;

		sc->setHostname("nase");
		ae = new alica::AlicaEngine();
		bc = new alica::BehaviourCreator();
		cc = new alica::ConditionCreator();
		uc = new alica::UtilityFunctionCreator();
		crc = new alica::ConstraintCreator();
		ae->setIAlicaClock(new alicaRosProxy::AlicaROSClock());
		ae->init(bc, cc, uc, crc, "RolesetTA", "MasterPlanTaskAssignment", ".", false);
	}

	virtual void TearDown()
	{
		ae->shutdown();
		sc->shutdown();
		delete bc;
		delete cc;
		delete uc;
		delete crc;
		delete ae->getIAlicaClock();
	}
};

TEST_F(TaskAssignmentTest, constructTaskAssignment)
{
	// fake a list of existing robots
	auto robots = make_shared<vector<int> >();
	for (int i = 8; i <= 11; i++)
	{
		robots->push_back(i);
	}

	// fake inform the team observer about roles of none existing robots
	auto& roles = ae->getPlanRepository()->getRoles();
	int i = 8;
	for (auto& role : roles)
	{
		ae->getTeamObserver()->getRobotById(i)->setLastRole(role.second);
		i++;
		if (i > 11)
			break;
	}

	auto planMap = ae->getPlanRepository()->getPlans();
	auto rp = make_shared<alica::RunningPlan>(ae, (*planMap.find(1407152758497)).second);
	list<alica::AbstractPlan*>* planList = new list<alica::AbstractPlan*>();
	planList->push_back((*planMap.find(1407152758497)).second);
	alica::IPlanSelector* ps = ae->getPlanSelector();
	auto plans = ps->getPlansForState(rp, planList, robots);
}

