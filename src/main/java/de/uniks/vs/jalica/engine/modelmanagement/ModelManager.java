package de.uniks.vs.jalica.engine.modelmanagement;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.PlanRepository;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.modelmanagement.parser.PlanParser;

import java.util.HashMap;

public class ModelManager {

    private final PlanParser planParser;

//    private SystemConfig sc;
//
//    private String domainConfigFolder;
//    private String domainSourceFolder;
//    private String planPath;
//    private String rolePath;
//    private String taskPath;
//
//    private ArrayList<String> filesToParse;
//    private ArrayList<String> filesParsed;
//
//    private PlanRepository planRepository;
    private HashMap<ID, AlicaElement> elements;

    public ModelManager(PlanRepository planRepository, AlicaEngine engine) {
//        this.planRepository = planRepository;
//        this.sc = engine.getSystemConfig();
//        this.filesParsed = new ArrayList<>();
//        this.filesToParse = new ArrayList<>();
////        this.domainConfigFolder = this.sc.getConfigPath();
//        this.domainConfigFolder = this.sc.getRootPath() + this.sc.getConfigPath();
//        this.domainSourceFolder = this.sc.getRootPath();
//        this.planPath = getBasePath("Alica.PlanDir");
//        this.rolePath = getBasePath("Alica.RoleDir");
//        this.taskPath = getBasePath("Alica.TaskDir");
        this.planParser = new PlanParser(engine, planRepository);
    }

    public Plan loadPlanTree(String masterPlanName) {
        return this.planParser.parsePlanTree(masterPlanName);
    }

    public Plan parsePlanTree(String masterPlanName) {
        return this.planParser.parsePlanTree(masterPlanName);
    }

    public RoleSet loadRoleSet(String roleSetName) {
        return this.planParser.parseRoleSet(roleSetName);
    }

    public boolean idExists(ID id) {
        return this.elements.containsKey(id);
    }

//    public RoleSet loadRoleSet(String roleSetName) {
//        std::string roleSetPath;
//        if (!essentials::FileSystem::findFile(this->rolePath, roleSetName + alica::Strings::roleset_extension, roleSetPath)) {
//            roleSetPath = findDefaultRoleSet(rolePath);
//        }
//
//        if (!essentials::FileSystem::pathExists(roleSetPath)) {
//            AlicaEngine::abort("MM: Cannot find RoleSet '" + roleSetPath + "'");
//        }
//
//        RoleSet* roleSet = (RoleSet*) parseFile(roleSetPath, alica::Strings::roleset);
//        RoleSetFactory::attachReferences();
//        ALICA_INFO_MSG("MM: Parsed the following role set: \n" << roleSet->toString());
//    }
//
//    public boolean idExists(long id) {
//        return this.elements.containsKey(id);
//    }
//
//    public EntryPoint generateIdleEntryPoint();
//
//    private AlicaElement getElement(long id) {
//        return this.elements.get(id);
//    }

//    private String getBasePath(String configKey) {
//        std::string basePath = "";
//        try {
//            basePath = (*this->sc)["Alica"]->get<std::string>(configKey.c_str(), NULL);
//        } catch (const std::runtime_error& error) {
//            AlicaEngine::abort("MM: Directory for config key " + configKey + " does not exist.\n", error.what());
//        }
//
//        if (!essentials::FileSystem::endsWith(basePath, essentials::FileSystem::PATH_SEPARATOR)) {
//            basePath = basePath + essentials::FileSystem::PATH_SEPARATOR;
//        }
//
//        if (!essentials::FileSystem::isPathRooted(basePath)) {
//            basePath = this->domainConfigFolder + basePath;
//        }
//
//        ALICA_INFO_MSG("MM: config key '" + configKey + "' maps to '" + basePath + "'");
//
//        if (!essentials::FileSystem::pathExists(basePath)) {
//            AlicaEngine::abort("MM: base path does not exist: " + planPath);
//        }
//        return basePath;
//    }
//
//    private AlicaElement parseFile(String currentFile, String type) {
//        YAML::Node node;
//        try {
//            node = YAML::LoadFile(currentFile);
//        } catch (YAML::BadFile badFile) {
//            AlicaEngine::abort("MM: Could not parse file: ", badFile.msg);
//        }
//
//        if (alica::Strings::plan.compare(type) == 0) {
//            Plan* plan = PlanFactory::create(node);
//            plan->setFileName(currentFile);
//            return plan;
//        } else if (alica::Strings::behaviour.compare(type) == 0) {
//            Behaviour* behaviour = BehaviourFactory::create(node);
//            behaviour->setFileName(currentFile);
//            return behaviour;
//        } else if (alica::Strings::plantype.compare(type) == 0) {
//            PlanType* planType = PlanTypeFactory::create(node);
//            planType->setFileName(currentFile);
//            return planType;
//        } else if (alica::Strings::taskrepository.compare(type) == 0) {
//            TaskRepository* taskrepository = TaskRepositoryFactory::create(node);
//            taskrepository->setFileName(currentFile);
//            return taskrepository;
//        } else if (alica::Strings::roleset.compare(type) == 0) {
//            RoleSet* roleSet = RoleSetFactory::create(node);
//            roleSet->setFileName(currentFile);
//            return roleSet;
//        } else {
//            AlicaEngine::abort("MM: Parsing type not handled: ", type);
//            return nullptr;
//        }
//    }
//
//    private String findDefaultRoleSet(String dir) {
//        std::string rolesetDir = dir;
//        if (!essentials::FileSystem::isPathRooted(rolesetDir)) {
//            rolesetDir = essentials::FileSystem::combinePaths(this->rolePath, rolesetDir);
//        }
//        if (!essentials::FileSystem::isDirectory(rolesetDir)) {
//            AlicaEngine::abort("MM: RoleSet directory does not exist: " + rolesetDir);
//        }
//
//        std::vector<std::string> files = essentials::FileSystem::findAllFiles(rolesetDir, alica::Strings::roleset_extension);
//
//        // find default role set and return first you find
//        for (std::string file : files) {
//            YAML::Node node;
//            try {
//                node = YAML::LoadFile(file);
//                if (Factory::isValid(node[alica::Strings::defaultRoleSet]) && Factory::getValue<bool>(node, alica::Strings::defaultRoleSet)) {
//                    return file;
//                }
//            } catch (YAML::BadFile badFile) {
//                AlicaEngine::abort("MM: Could not parse roleset file: ", badFile.msg);
//            }
//        }
//
//        AlicaEngine::abort("MM: Could not find any default role set in '" + rolesetDir + "'");
//
//        // need to return something, but it should never be reached (either found something, or abort)
//        return files[0];
//    }
//
//    private void attachReferences() {
//        PlanFactory::attachReferences();
//        PlanTypeFactory::attachReferences();
//        BehaviourFactory::attachReferences();
//    }
//
//    private void generateTemplateVariables() {
//        for (std::pair<const int64_t, Quantifier*> p : this->planRepository->_quantifiers) {
//            Quantifier* q = p.second;
//            for (const std::string& s : q->getDomainIdentifiers()) {
//                int64_t id = Hash64(s.c_str(), s.size());
//                Variable* v;
//                PlanRepository::MapType<Variable>::iterator vit = this->planRepository->_variables.find(id);
//                if (vit != this->planRepository->_variables.end()) {
//                    v = vit->second;
//                } else {
//                    v = new Variable(id, s, "Template");
//                    this->planRepository->_variables.emplace(id, v);
//                }
//                q->_templateVars.push_back(v);
//            }
//        }
//    }
//void ModelManager::computeReachabilities()
//    {
//        for (const std::pair<const int64_t, EntryPoint*>& ep : this->planRepository->_entryPoints) {
//        ep.second->computeReachabilitySet();
//        // set backpointers:
//        for (const State* s : ep.second->_reachableStates) {
//            this->planRepository->_states[s->getId()]->_entryPoint = ep.second;
//        }
//    }
//    }

    public void computeReachabilities(PlanRepository repo) {
        for ( EntryPoint ep : repo.getEntryPoints().values()) {
            ep.computeReachabilitySet();

            for ( State s : ep.getReachableStates()) {
                repo.getStates().get(s.getID()).setEntryPoint(ep);
            }
        }
    }



}
