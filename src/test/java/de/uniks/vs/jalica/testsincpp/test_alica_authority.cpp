/*
 * test_alica_authority.cpp
 *
 *  Created on: Oct 27, 2014
 *      Author: Stefan Jakob
 */

#include <gtest/gtest.h>
#include <engine/AlicaEngine.h>
#include <engine/IAlicaClock.h>
#include "engine/IAlicaCommunication.h"
#include "BehaviourCreator.h"
#include "ConditionCreator.h"
#include "ConstraintCreator.h"
#include "UtilityFunctionCreator.h"
#include <clock/AlicaROSClock.h>
#include <communication/AlicaRosCommunication.h>
#include "TestWorldModel.h"
#include "engine/PlanRepository.h"
#include "engine/UtilityFunction.h"
#include "engine/model/Plan.h"
#include "DummyTestSummand.h"
#include "engine/teamobserver/TeamObserver.h"
#include "engine/PlanBase.h"
#include "engine/model/State.h"

class AlicaEngineAuthorityManager : public ::testing::Test
{
protected:
	supplementary::SystemConfig* sc;
	alica::AlicaEngine* ae;
	alica::AlicaEngine* ae2;
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
		sc->setHostname("nase");

		// setup the engine
		ae = new alica::AlicaEngine();
		bc = new alica::BehaviourCreator();
		cc = new alica::ConditionCreator();
		uc = new alica::UtilityFunctionCreator();
		crc = new alica::ConstraintCreator();
		ae->setIAlicaClock(new alicaRosProxy::AlicaROSClock());
		ae->setCommunicator(new alicaRosProxy::AlicaRosCommunication(ae));
	}

	virtual void TearDown()
	{
		ae->shutdown();
		sc->shutdown();
		ae2->shutdown();
		delete ae->getCommunicator();
		delete ae2->getCommunicator();
		delete ae->getIAlicaClock();
		delete ae2->getIAlicaClock();
		delete cc;
		delete bc;
		delete uc;
		delete crc;
	}

};

TEST_F(AlicaEngineAuthorityManager, authority)
{
	sc->setHostname("nase");
	ae = new alica::AlicaEngine();
	ae->setIAlicaClock(new alicaRosProxy::AlicaROSClock());
	ae->setCommunicator(new alicaRosProxy::AlicaRosCommunication(ae));
	EXPECT_TRUE(ae->init(bc, cc, uc, crc, "RolesetTA", "AuthorityTestMaster", ".", true)) << "Unable to initialise the Alica Engine!";

	sc->setHostname("hairy");
	ae2 = new alica::AlicaEngine();
	ae2->setIAlicaClock(new alicaRosProxy::AlicaROSClock());
	ae2->setCommunicator(new alicaRosProxy::AlicaRosCommunication(ae2));
	EXPECT_TRUE(ae2->init(bc, cc, uc, crc, "RolesetTA", "AuthorityTestMaster", ".", true)) << "Unable to initialise the Alica Engine!";

	auto uSummandAe = *((ae->getPlanRepository()->getPlans().find(1414403413451))->second->getUtilityFunction()->getUtilSummands().begin());
	DummyTestSummand* dbr = dynamic_cast<DummyTestSummand*>(uSummandAe);
	dbr->robotId = ae->getTeamObserver()->getOwnId();
	auto uSummandAe2 = *((ae2->getPlanRepository()->getPlans().find(1414403413451))->second->getUtilityFunction()->getUtilSummands().begin());
	DummyTestSummand* dbr2 = dynamic_cast<DummyTestSummand*>(uSummandAe2);
	dbr2->robotId = ae2->getTeamObserver()->getOwnId();
	ae->start();
	ae2->start();

	alicaTests::TestWorldModel::getOne()->robotsXPos.push_back(0);
	alicaTests::TestWorldModel::getOne()->robotsXPos.push_back(2000);

	alicaTests::TestWorldModel::getTwo()->robotsXPos.push_back(2000);
	alicaTests::TestWorldModel::getTwo()->robotsXPos.push_back(0);

	for (int i = 0; i < 21; i++)
	{
		ae->stepNotify();
		chrono::milliseconds duration(33);
		this_thread::sleep_for(duration);
		ae2->stepNotify();
		this_thread::sleep_for(duration);
		if (i == 1)
		{
			EXPECT_EQ((*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId(), 1414403553717);
			EXPECT_EQ((*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId(), 1414403553717);
		}

		if (i == 20)
		{
			EXPECT_EQ((*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId(), 1414403553717);
			EXPECT_EQ((*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId(), 1414403429950);
		}
	}

}
