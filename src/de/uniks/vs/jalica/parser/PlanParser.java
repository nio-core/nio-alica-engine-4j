package de.uniks.vs.jalica.parser;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.supplementary.FileSystem;
import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.RoleSet;
import de.uniks.vs.jalica.teamobserver.PlanRepository;
import de.uniks.vs.jalica.unknown.parser.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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
    private ArrayList<XMLHandler> xmlTagHandlers;


    public PlanParser(AlicaEngine ae, PlanRepository planRepository) {
        this.ae = ae;
        this.rep = planRepository;

        initTagHandler();

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

    private void initTagHandler() {
        xmlTagHandlers = new ArrayList<>();

        xmlTagHandlers.add(new AttributeHandler());
        xmlTagHandlers.add(new EntryPointHandler());
        xmlTagHandlers.add(new StatesHandler());
        xmlTagHandlers.add(new TransitionsHandler());
        xmlTagHandlers.add(new ConditionsHandler());
        xmlTagHandlers.add(new VarsHandler());
        xmlTagHandlers.add(new SynchonisationsHandler());
        xmlTagHandlers.add(new EOLHandler());
        xmlTagHandlers.add(new ErrorHandler());
    }

    public Plan parsePlanTree(String masterplan) {

        String masterPlanPath = null;
        masterPlanPath = FileSystem.findFile(this.basePlanPath, masterplan + ".pml", masterPlanPath);
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

    private Plan parsePlanFile(String planFile){
        Plan p = null;
//#ifdef PP_DEBUG
        System.out.println("PP: parsing Plan file: " + planFile );
//#endif

        File file = new File(planFile);

        if (!file.exists()) {
            ae.abort("PP: " + file +" not exists!!!");
        }

//        XMLDocument doc;
//        doc.LoadFile(planFile.c_str());

        DocumentBuilder docBuilder = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            if (doc == null)
            {
                ae.abort("PP: " + "can not parse " + file);
            }
            doc.getDocumentElement().normalize();

             p = this.mf.createPlan(doc);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }

        return p;
    }

    private long fetchId(String idString, long id){
//        int hashPos = idString.indexOf("#");
//		String temp = null;
//        String temp2 = null;
//        String locator = idString.substring(0, hashPos);
//
//        if (!locator.isEmpty())
//        {
//            if (!FileSystem.endsWith(this.currentDirectory, "/"))
//            {
//                this.currentDirectory = this.currentDirectory + "/";
//            }
//            String path = this.currentDirectory + locator;
//            //not working no clue why
//            //char s[2048];
//            //char s2[2048];
//            temp = realpath(path, null);
//            String pathNew = temp;
////            free(temp);
//            //This is not very efficient but necessary to keep the paths as they are
//            //Here we have to check whether the file has already been parsed / is in the list for toparse files
//            //problem is the normalization /home/etc/plans != /home/etc/Misc/../plans
//            //list<string>::iterator findIterParsed = find(filesParsed.begin(), filesParsed.end(), pathNew);
//            boolean found = false;
//            for(String it : filesParsed) {
//            temp2 = realpath(it, null);
//            String pathNew2 =temp2;
////            free(temp2);
//            if(pathNew2 == pathNew) {
//                found = true;
//                break;
//            }
//        }
//
//            //list<string>::iterator findIterToParse = find(filesToParse.begin(), filesToParse.end(), pathNew);
//            if(!found) {
//                for(auto& it : filesToParse) {
//                    temp2 = realpath(it.c_str(), null);
//                    string pathNew2 =temp2;
//                    free(temp2);
//                    if(pathNew2 == pathNew) {
//                        found = true;
//                        break;
//                    }
//                }
//            }
//
//
//            if (!found)
//            {
//#ifdef PP_DEBUG
//                cout << "PP: Adding " + path + " to parse queue " << endl;
//#endif
//                filesToParse.add(path);
//            }
//        }
//        string tokenId = idString.substr(hashPos + 1, idString.length() - hashPos);
//        try
//        {
//            id = stol(tokenId);
//        }
//        catch (exception e)
//        {
//            ae.abort("PP: Cannot convert ID to long: " + tokenId + " WHAT?? " + e.what());
//        }
        return id;
    }

    private String findDefaultRoleSet(String dir){return null;}

    public long parserId(Node node) {
        long id = -1;
        String idString1 = "";
        Node idItem = node.getAttributes().getNamedItem("id");

        if (idItem != null)
            idString1 = idItem.getTextContent();
        if (idString1.length() > 0)
        {
            try
            {
                id = Long.parseLong(idString1);
            }
            catch (Exception e)
            {
                ae.abort("PP: Cannot convert ID to long: " + idString1 + " WHAT?? " + e.getMessage());
            }
            return id;
        }
        else
        {
            String idString2 = "";
			Node idChar = node.getAttributes().getNamedItem("href");
            if (idChar != null)
                idString2 = idChar.getTextContent();
            if (idString2.length() > 0)
            {
                id = fetchId(idString2, id);
                return id;
            }
            else
            {
                Node currNode = node.getFirstChild();
                while (currNode != null)
                {
                    String textContent = currNode.getTextContent();
                    if (textContent.length() > 0)
                    {
                        id = fetchId(textContent, id);
                        return id;
                    }

                    currNode = currNode.getNextSibling();
                }
            }
        }

        System.err.println("Cannot resolve remote reference!\nAttributes of node in question are:" );

        for ( int i = 0; i < node.getAttributes().getLength(); i++) {

            Node curAttribute = node.getAttributes().item(i);
            System.out.println(curAttribute.getNodeName() + " : " + curAttribute.getNodeName() );
        }

        ae.abort("PP: Couldn't resolve remote reference: " + (node.getNodeName()));
        return -1;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public void handleTag(Node node, Plan plan, ModelFactory modelFactory) {

        for (XMLHandler handler:xmlTagHandlers) {

            if (handler.handle(node, plan, modelFactory))
                return;
        }

        ae.abort("PP: Cannot handle XML Tag: " + node.getNodeName());
    }
}
