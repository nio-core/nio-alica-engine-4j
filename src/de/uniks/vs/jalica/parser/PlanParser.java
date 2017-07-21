package de.uniks.vs.jalica.parser;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.supplementary.FileSystem;
import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.RoleSet;
import de.uniks.vs.jalica.teamobserver.PlanRepository;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class PlanParser {

    AlicaEngine ae;
    SystemConfig sc;
    ModelFactory mf;
    PlanRepository rep;
    Plan masterPlan;
    String planDir;
    String roleDir;
    String basePlanPath;
    String baseRolePath;
    String currentDirectory;
    String domainConfigFolder;
    String currentFile;

    ArrayList<String> filesToParse = new ArrayList<>();
    ArrayList<String> filesParsed = new ArrayList<>();


    public PlanParser(AlicaEngine ae, PlanRepository planRepository) {
        this.ae = ae;
        this.rep = planRepository;

        this.masterPlan = null;
        this.mf = new ModelFactory(ae, this, rep);
        this.sc = SystemConfig.getInstance();
        this.domainConfigFolder = this.sc.getConfigPath();

        this.planDir = this.sc.get("Alica").get("Alica.PlanDir");
        this.roleDir = this.sc.get("Alica").get("Alica.RoleDir");

        if (domainConfigFolder.lastIndexOf(FileSystem.PATH_SEPARATOR) != domainConfigFolder.length() - 1)
        {
            domainConfigFolder = domainConfigFolder + FileSystem.PATH_SEPARATOR;
        }
        if (planDir.lastIndexOf(FileSystem.PATH_SEPARATOR) != planDir.length() - 1)
        {
            planDir = planDir + FileSystem.PATH_SEPARATOR;
        }
        if (roleDir.lastIndexOf(FileSystem.PATH_SEPARATOR) != roleDir.length() - 1)
        {
            roleDir = roleDir + FileSystem.PATH_SEPARATOR;
        }
        if (!(FileSystem.isPathRooted(this.planDir)))
        {
            basePlanPath = domainConfigFolder + planDir;
        }
		else
        {
            basePlanPath = planDir;
        }
        if (!(FileSystem.isPathRooted(this.roleDir)))
        {
            baseRolePath = domainConfigFolder + roleDir;
        }
		else
        {
            baseRolePath = roleDir;
        }
//#ifdef PP_DEBUG
        System.out.println( "PP: basePlanPath: " + basePlanPath );
        System.out.println(  "PP: baseRolePath: " + baseRolePath );
//#endif
        if (!(FileSystem.pathExists(basePlanPath)))
        {
            ae.abort("PP: BasePlanPath does not exists " + basePlanPath);
        }
        if (!(FileSystem.pathExists(baseRolePath)))
        {
            ae.abort("PP: BaseRolePath does not exists " + baseRolePath);
        }
        
    }

    public Plan parsePlanTree(String masterplan) {
        String masterPlanPath = null;
        masterplan = FileSystem.findFile(this.basePlanPath, masterplan + ".pml", masterPlanPath);
        boolean found = masterplan != null;
//#ifdef PP_DEBUG
        System.out.println( "PP: masterPlanPath: " + masterPlanPath );
//#endif
        if (!found)
        {
            ae.abort("PP: Cannot find MasterPlan '" + masterplan + "'");
        }
        this.currentFile = masterPlanPath;
        this.currentDirectory = FileSystem.getParent(masterPlanPath);
//#ifdef PP_DEBUG
        System.out.println( "PP: CurFile: " + this.currentFile + " CurDir: " + this.currentDirectory );
//#endif

        this.masterPlan = parsePlanFile(masterPlanPath);
        this.filesParsed.add(masterPlanPath);
        parseFileLoop();

        this.mf.computeReachabilities();
        return this.masterPlan;
    }



    public RoleSet parseRoleSet(String roleSetName, String roleSetDir) {

        if (roleSetName.isEmpty())
        {
            roleSetName = findDefaultRoleSet(roleSetDir);
        }
        else
        {
            if (roleSetDir.lastIndexOf(FileSystem.PATH_SEPARATOR) != roleSetDir.length() - 1
                    && roleSetDir.length() > 0)
            {
                roleSetDir = roleSetDir + FileSystem.PATH_SEPARATOR;
            }
            if (!FileSystem.isPathRooted(roleSetDir))
            {
                roleSetName = FileSystem.combinePaths(FileSystem.combinePaths(baseRolePath, roleSetDir), roleSetName);
            }
			else
            {
                roleSetName = FileSystem.combinePaths(roleSetDir, roleSetName);
            }
        }

        if (!FileSystem.endsWith(roleSetName, ".rset"))
        {
            roleSetName = roleSetName + ".rset";
        }
        if (!FileSystem.pathExists(roleSetName))
        {
            ae.abort("PP: Cannot find roleset: " + roleSetName);
        }

//#ifdef PP_DEBUG
        System.out.println( "PP: Parsing RoleSet " + roleSetName );
//#endif

        this.currentDirectory = FileSystem.getParent(roleSetName);

//        XMLDocument doc;
//        doc.LoadFile(roleSetName);
//        if (doc.ErrorID() != tinyxml2.XML_NO_ERROR)
//        {
//            System.out.println( "PP: doc.ErrorCode: " + tinyxml2.XMLErrorStr[doc.ErrorID()] );
//            throw new exception();
//        }
//
//        RoleSet r = this.mf.createRoleSet(doc, this.masterPlan);
//
//        filesParsed.push_back(roleSetName);
//
//        while (this.filesToParse.size() > 0)
//        {
//            String fileToParse = this.filesToParse.front();
//            this.filesToParse.pop_front();
//            this.currentDirectory = FileSystem.getParent(fileToParse);
//            this.currentFile = fileToParse;
//
//            if (!FileSystem.pathExists(fileToParse))
//            {
//                ae.abort("PP: Cannot Find referenced file " + fileToParse);
//            }
//            if (FileSystem.endsWith(fileToParse, ".rdefset"))
//            {
//                parseRoleDefFile(fileToParse);
//            }
//			else if (FileSystem.endsWith(fileToParse, ".cdefset"))
//            {
//                parseCapabilityDefFile(fileToParse);
//            }
//			else
//            {
//                ae.abort("PP: Cannot Parse file " + fileToParse);
//            }
//            filesParsed.add(fileToParse);
//
//        }

        this.mf.attachRoleReferences();
        this.mf.attachCharacteristicReferences();
//        return r;
        return new RoleSet();
    }

    private void parseFileLoop() {
    }

    private void parseTaskFile(String currentFile) {}

    private void parseRoleDefFile(String currentFile){}

    private void parseCapabilityDefFile(String currentFile){}

    private void parsePlanTypeFile(String currentFile){}

    private void parseBehaviourFile(String currentFile){}

    private void parsePlanningProblem(String currentFile){}

    private Plan parsePlanFile(String planFile){ return null;}

    private long fetchId(String idString, long id){return 0;}

    private String findDefaultRoleSet(String dir){return null;}
}
