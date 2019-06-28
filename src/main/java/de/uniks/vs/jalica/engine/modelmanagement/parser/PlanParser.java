package de.uniks.vs.jalica.engine.modelmanagement.parser;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.PlanBase;
import de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json.*;
import de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml.*;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.common.FileSystem;
import de.uniks.vs.jalica.engine.PlanRepository;
import de.uniks.vs.jalica.engine.model.AlicaElement;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.RoleSet;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.color.CMMException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
    String taskDir;
    String basePlanPath;
    String baseRolePath;
    String baseTaskPath;
    String currentDirectory;
    String domainConfigFolder;
    String domainSourceFolder;
    String currentFile;

    ArrayList<String> filesToParse = new ArrayList<>();
    ArrayList<String> filesParsed = new ArrayList<>();
    private ArrayList<XMLHandler> xmlTagHandlers;
    private ArrayList<JSONHandler> jsonEntryHandlers;

    public PlanParser(AlicaEngine ae, PlanRepository planRepository) {
        this.ae = ae;
        this.rep = planRepository;

        initTagHandler();
        initJSONEntryHandler();

        this.masterPlan = null;
        this.mf = new ModelFactory(ae, this, rep);
        this.sc = ae.getSystemConfig();
        this.domainConfigFolder = this.sc.getRootPath() + this.sc.getConfigPath();
        this.domainSourceFolder = this.sc.getRootPath();

        this.planDir = (String) this.sc.get("Alica").get("Alica.PlanDir");
        this.roleDir = (String) this.sc.get("Alica").get("Alica.RoleDir");
        this.taskDir = (String) this.sc.get("Alica").get("Alica.TaskDir");


        if (domainConfigFolder.lastIndexOf(FileSystem.PATH_SEPARATOR) != domainConfigFolder.length() - 1) {
            domainConfigFolder = domainConfigFolder + FileSystem.PATH_SEPARATOR;
        }
        if (planDir.lastIndexOf(FileSystem.PATH_SEPARATOR) != planDir.length() - 1) {
            planDir = planDir + FileSystem.PATH_SEPARATOR;
        }
        if (roleDir.lastIndexOf(FileSystem.PATH_SEPARATOR) != roleDir.length() - 1) {
            roleDir = roleDir + FileSystem.PATH_SEPARATOR;
        }
        if (taskDir.lastIndexOf(FileSystem.PATH_SEPARATOR) != taskDir.length() - 1) {
            taskDir = taskDir + FileSystem.PATH_SEPARATOR;
        }
        if (!(FileSystem.isPathRooted(this.planDir))) {
            basePlanPath = domainSourceFolder + planDir;
        }
		else {
            basePlanPath = planDir;
        }
        if (!(FileSystem.isPathRooted(this.roleDir))) {
            baseRolePath = domainSourceFolder + roleDir;
        }
		else {
            baseRolePath = roleDir;
        }
        if (!(FileSystem.isPathRooted(this.taskDir))) {
            baseTaskPath = domainSourceFolder + taskDir;
        }
		else {
            baseTaskPath = taskDir;
        }

		if (CommonUtils.PP_DEBUG_debug) System.out.println( "PP: basePlanPath: " + basePlanPath );
        if (CommonUtils.PP_DEBUG_debug) System.out.println(  "PP: baseRolePath: " + baseRolePath );
        if (CommonUtils.PP_DEBUG_debug) System.out.println(  "PP: baseTaskPath: " + baseTaskPath );

        if (!(FileSystem.pathExists(basePlanPath))) {
            CommonUtils.aboutError("PP: BasePlanPath does not exists " + basePlanPath);
        }
        if (!(FileSystem.pathExists(baseRolePath))) {
            CommonUtils.aboutError("PP: BaseRolePath does not exists " + baseRolePath);
        }
        if (!(FileSystem.pathExists(baseTaskPath))) {
            CommonUtils.aboutError("PP: BaseRolePath does not exists " + baseTaskPath);
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

    private void initJSONEntryHandler() {
        jsonEntryHandlers = new ArrayList<>();

        jsonEntryHandlers.add(new JSONAttributeHandler());
        jsonEntryHandlers.add(new JSONEntryPointHandler());
        jsonEntryHandlers.add(new JSONStatesHandler());
        jsonEntryHandlers.add(new JSONTransitionsHandler());
        jsonEntryHandlers.add(new JSONConditionsHandler());
        jsonEntryHandlers.add(new JSONVarsHandler());
        jsonEntryHandlers.add(new JSONSynchonisationsHandler());
        jsonEntryHandlers.add(new JSONIgnoreHandler());
//        jsonEntryHandlers.add(new JSONErrorHandler());
    }

    public Plan parsePlanTree(String masterplan) {

        String masterPlanPath  = FileSystem.findFile(this.basePlanPath, masterplan + ".pml");
        boolean found = masterplan != null;
        if (CommonUtils.PP_DEBUG_debug) System.out.println( "PP: masterPlanPath: " + masterPlanPath );
        if (!found) {
            CommonUtils.aboutError("PP: Cannot find MasterPlan '" + masterplan + "'");
        }
        this.currentFile = masterPlanPath;
        this.currentDirectory = FileSystem.getParent(masterPlanPath);
        if (CommonUtils.PP_DEBUG_debug) System.out.println( "PP: CurFile: " + this.currentFile + " CurDir: " + this.currentDirectory );

        this.masterPlan = parsePlanFile(masterPlanPath);
        this.filesParsed.add(masterPlanPath);
        parseFileLoop();

        this.mf.computeReachabilities();
        return this.masterPlan;
    }

    public RoleSet parseRoleSet(String roleSetName) {
        String roleSetDir = this.baseRolePath;

        CommonUtils.aboutCallNotification("check rolesetdir: " +roleSetDir);

        if (roleSetName.isEmpty()) {
            roleSetName = findDefaultRoleSet(roleSetDir);
        }
        else {

            if (roleSetDir.lastIndexOf(FileSystem.PATH_SEPARATOR) != roleSetDir.length() - 1
                    && roleSetDir.length() > 0) {
                roleSetDir = roleSetDir + FileSystem.PATH_SEPARATOR;
            }

            if (!FileSystem.isPathRooted(roleSetDir)) {
                roleSetName = FileSystem.combinePaths(FileSystem.combinePaths(baseRolePath, roleSetDir), roleSetName);

                if (!FileSystem.endsWith(roleSetName, ".rset")) {
                    roleSetName = roleSetName + ".rset";
                }
                String tempRoleSetName = FileSystem.getAbsolutePath(roleSetName);

                if (tempRoleSetName == null || !FileSystem.pathExists(roleSetName)) {
                    roleSetName = roleSetName.substring(0, roleSetName.length()-5) + ".rst";

                    if (!FileSystem.pathExists(roleSetName)) {
                        CommonUtils.aboutError("PP: Cannot find roleset: " + roleSetName);
                    }
                } else
                    roleSetName = tempRoleSetName;
            } else
            {
                roleSetName = FileSystem.combinePaths(roleSetDir, roleSetName);
            }
        }

        if (!FileSystem.endsWith(roleSetName, ".rset") && !FileSystem.endsWith(roleSetName, ".rst"))
        {
            roleSetName = roleSetName + ".rset";
        }

        if (!FileSystem.pathExists(roleSetName))
        {
            roleSetName = roleSetName.substring(0, roleSetName.length()-5) + ".rst";

            if (!FileSystem.pathExists(roleSetName)) {
                CommonUtils.aboutError("PP: Cannot find roleset: " + roleSetName);
            }
        }

//#ifdef PP_DEBUG
        if (CommonUtils.PP_DEBUG_debug) System.out.println( "PP: Parsing RoleSet " + roleSetName );
//#endif

        this.currentDirectory = FileSystem.getParent(roleSetName);
        String absolutePath = FileSystem.getAbsolutePath(roleSetName);
        File file = new File(absolutePath);

        if (!file.exists()) {
            CommonUtils.aboutError("PP: " + file +" not exists!!!");
        }
        RoleSet r = null;
        try {
//         Document doc;
//         doc.LoadFile(roleSetName);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

//            if (doc.ErrorID() != tinyxml2.XML_NO_ERROR)
//            {
//                System.out.println( "PP: doc.ErrorCode: " + tinyxml2.XMLErrorStr[doc.ErrorID()] );
//                throw new exception();
//            }
            if (doc == null)
            {
                CommonUtils.aboutError("PP: doc.ErrorCode: " + file);
            }
            doc.getDocumentElement().normalize();

            r = this.mf.createRoleSet(doc, this.masterPlan);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
//            System.err.println("PP: doc.ErrorCode: ");
//            e.printStackTrace();
            JSONObject jsonDoc = null;
            try {
                jsonDoc = (JSONObject) new JSONParser().parse(new FileReader(file));
                r = this.mf.createRoleSet(jsonDoc, this.masterPlan);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }


        filesParsed.add(roleSetName);

        while (this.filesToParse.size() > 0)
        {
            String fileToParse = this.filesToParse.get(0);
            this.filesToParse.remove(0);
            this.currentDirectory = FileSystem.getParent(fileToParse);
            this.currentFile = fileToParse;

            if (!FileSystem.pathExists(fileToParse))
            {
                CommonUtils.aboutError("PP: Cannot Find referenced file " + fileToParse);
            }
            if (FileSystem.endsWith(fileToParse, ".rdefset"))
            {
                parseRoleDefFile(fileToParse);
            }
			else if (FileSystem.endsWith(fileToParse, ".cdefset"))
            {
                parseCapabilityDefFile(fileToParse);
            }
			else
            {
                CommonUtils.aboutError("PP: Cannot Parse file " + fileToParse);
            }
            filesParsed.add(fileToParse);

        }

        this.mf.attachRoleReferences();
        this.mf.attachCharacteristicReferences();
        return r;
    }

    private void parseFileLoop() {
        while (this.filesToParse.size() > 0)
        {
            String fileToParse = this.filesToParse.get(0);
            this.filesToParse.remove(0);
            this.currentDirectory = FileSystem.getParent(fileToParse);
            this.currentFile = fileToParse;

            if (!FileSystem.pathExists(fileToParse))
            {
                CommonUtils.aboutError("PP: Cannot Find referenced file " + fileToParse);
            }
            if (FileSystem.endsWith(fileToParse, ".pml"))
            {
                parsePlanFile(fileToParse);
            }
			else if (FileSystem.endsWith(fileToParse, ".tsk"))
            {
                parseTaskFile(fileToParse);
            }
			else if (FileSystem.endsWith(fileToParse, ".beh"))
            {
                parseBehaviourFile(fileToParse);
            }
			else if (FileSystem.endsWith(fileToParse, ".pty"))
            {
                parsePlanTypeFile(fileToParse);
            }
			else if (FileSystem.endsWith(fileToParse, ".pp"))
            {
                parsePlanningProblem(fileToParse);
            }
			else
            {
                CommonUtils.aboutError("PP: Cannot Parse file" + fileToParse);
            }
            filesParsed.add(fileToParse);
        }
        this.mf.attachPlanReferences();
    }

    private void parseTaskFile(String file) {
        if (CommonUtils.PP_DEBUG_debug) System.out.println( "PP: parsing Task file: " + currentFile );
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            if (doc == null)
            {
                CommonUtils.aboutError("PP: doc.ErrorCode: " + file);
            }
            doc.getDocumentElement().normalize();
            this.mf.createTasks(doc);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
//            System.err.println("PP: doc.ErrorCode: ");
//            e.printStackTrace();

            JSONObject jsonDoc = null;
            try {
                jsonDoc = (JSONObject) new JSONParser().parse(new FileReader(file));
                this.mf.createTasks(jsonDoc);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }
    }

    private void parseRoleDefFile(String currentFile) {
        if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: parsing RoleDef file: " + currentFile);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(currentFile);

            if (doc == null)
            {
                CommonUtils.aboutError("PP: doc.ErrorCode: " + currentFile);
            }
            doc.getDocumentElement().normalize();
            this.mf.createRoleDefinitionSet(doc);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }
    }

    private void parseCapabilityDefFile(String currentFile){
        if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: parsing RoleDef file: " + currentFile);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(currentFile);

            if (doc == null)
            {
                CommonUtils.aboutError("PP: doc.ErrorCode: " + currentFile);
            }
            doc.getDocumentElement().normalize();
            this.mf.createCapabilityDefinitionSet(doc);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }
    }

    private void parsePlanTypeFile(String currentFile){
        if (CommonUtils.PP_DEBUG_debug) System.out.println( "PP: parsing PlanType file: " + currentFile );
        Document doc = null;
        DocumentBuilder docBuilder = null;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(currentFile);

        }  catch (SAXException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }
        this.mf.createPlanType(doc);
    }

    private void parseBehaviourFile(String currentFile){
        if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: parsing Behaviour file: " + currentFile);;
        File file = new File(currentFile);

        if (!file.exists()) {
            CommonUtils.aboutError("PP: " + file +" not exists!!!");
        }

//        XMLDocument doc;
//        doc.LoadFile(planFile.c_str());
        Document doc = null;
        DocumentBuilder docBuilder = null;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(file);
            this.mf.createBehaviour(doc);
        }  catch (SAXException e) {
//            System.err.println("PP: doc.ErrorCode: ");
//            e.printStackTrace();

            JSONObject jsonDoc = null;
            try {
                jsonDoc = (JSONObject) new JSONParser().parse(new FileReader(file));
                this.mf.createBehaviour(jsonDoc);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }

    }

    private void parsePlanningProblem(String currentFile){
        if (CommonUtils.PP_DEBUG_debug) System.out.println( "PP: parsing Planning Problem file: " + currentFile );
        Document doc;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(currentFile);
            this.mf.createPlanningProblem(doc);
        } catch (ParserConfigurationException e) {
            CommonUtils.aboutError("PP: doc.ErrorCode: ");
        } catch (SAXException e) {
            CommonUtils.aboutError("PP: doc.ErrorCode: ");
        } catch (IOException e) {
            CommonUtils.aboutError("PP: doc.ErrorCode: ");
        }
    }

    private Plan parsePlanFile(String planFile){
        Plan p = null;
        if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: parsing Plan file: " + planFile );

        File file = new File(planFile);

        if (!file.exists()) {
            CommonUtils.aboutError("PP: " + file +" not exists!!!");
        }

        DocumentBuilder docBuilder = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            if (doc == null) {
                CommonUtils.aboutError("PP: " + "can not parse " + file);
            }
            doc.getDocumentElement().normalize();

             p = this.mf.createPlan(doc);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            //System.err.println("PP: doc.ErrorCode: ");
//            e.printStackTrace();
            try {
                JSONObject jsonDoc = (JSONObject) new JSONParser().parse(new FileReader(file));
                p = this.mf.createPlan(jsonDoc);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("PP: doc.ErrorCode: ");
            e.printStackTrace();
        }

        return p;
    }

/*  long id = (Long) jsonObject.get("id");
    this.parser.fetchId(jsonObject.get("id").toString(), id); */

    public Long extractID(String idString) {
        if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: extract ID " + idString);
        int hashPos = idString.indexOf("#");
        return Long.valueOf(hashPos > 0 ? idString.substring(hashPos+1) : idString);
    }

    public long fetchId(String idString) {
        if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: fetch ID " + idString);
        int hashPos = idString.indexOf("#");
        long id = -1;
		String temp = null;
        String temp2 = null;
        String locator = hashPos > 0 ? idString.substring(0, hashPos) : "";

        while(locator.indexOf(".") < locator.lastIndexOf(".")) {
            locator = locator.substring(0, locator.indexOf("."))+ "/" + locator.substring(locator.indexOf(".")+1);
        }

        if (!locator.isEmpty())
        {
            if (!FileSystem.endsWith(this.currentDirectory, "/"))
            {
                this.currentDirectory = this.currentDirectory + "/";
            }
            String path = this.currentDirectory + locator;

            if (locator.endsWith(".tsk")) {
                path = path.replace(this.planDir,this.taskDir);
            }
            //not working no clue why
            //char s[2048];
            //char s2[2048];
            temp = FileSystem.realpath(path, null);
            String pathNew = temp;
//            free(temp);
            //This is not very efficient but necessary teamObserver keep the paths as they are
            //Here we have teamObserver check whether the file has already been parsed / is in the list for toparse files
            //problem is the normalization /home/etc/plans != /home/etc/misc/../plans
            //list<string>::iterator findIterParsed = find(filesParsed.begin(), filesParsed.end(), pathNew);
            boolean found = false;
            for(String it : filesParsed) {
                temp2 = FileSystem.realpath(it, null);
                String pathNew2 = temp2;
//            free(temp2);
                if (pathNew2.equals(pathNew)) {
                    found = true;
                    break;
                }
            }

            //list<string>::iterator findIterToParse = find(filesToParse.begin(), filesToParse.end(), pathNew);
            if(!found) {
                for(String filePath : filesToParse) {
                    temp2 = FileSystem.realpath(filePath, null);
                    String pathNew2 =temp2;
//                    free(temp2);
                    if(pathNew2.equals(pathNew)) {
                        found = true;
                        break;
                    }
                }
            }


            if (!found)
            {
                if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: Adding " + path + " teamObserver parse queue ");
                filesToParse.add(path);
            }
        }
        String tokenId = idString.substring(hashPos + 1, idString.length());
        try
        {
            id = Long.valueOf(tokenId);
        }
        catch (Exception e)
        {
            CommonUtils.aboutError("PP: Cannot convert ID teamObserver long: " + tokenId + " WHAT?? " + e.getMessage());
        }
        return id;
    }

    private String findDefaultRoleSet(String dir){
        CommonUtils.aboutNoImpl();
        return null;
    }

    public long parserId(Node node) {
        long id = -1;
        String idString1 = "";
        Node idItem = node.getAttributes() != null ? node.getAttributes().getNamedItem("id") : null;

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
                CommonUtils.aboutError("PP: Cannot convert ID teamObserver long: " + idString1 + " WHAT?? " + e.getMessage());
            }
            return id;
        }
        else
        {
            String idString2 = "";
			Node idChar = node.getAttributes() != null ? node.getAttributes().getNamedItem("href") : null;
            if (idChar != null)
                idString2 = idChar.getTextContent();
            if (idString2.length() > 0)
            {
                id = fetchId(idString2);
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
                        id = fetchId(textContent);
                        return id;
                    }

                    currNode = currNode.getNextSibling();
                }
            }
        }

        System.err.println("PP: Cannot resolve remote reference!\n    Attributes of node in question are:" );

        if (node.getAttributes() != null)

            for ( int i = 0; i < node.getAttributes().getLength(); i++) {
                Node curAttribute = node.getAttributes().item(i);
                System.out.println("PP: "+curAttribute.getNodeName() + " : " + curAttribute.getNodeName() );
            }

        CommonUtils.aboutError("PP: Couldn't resolve remote reference: " + (node.getNodeName()));
        return -1;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public void handleTag(Node node, Plan plan, ModelFactory modelFactory) {

        for (XMLHandler handler : xmlTagHandlers) {

            if (handler.handle(node, plan, modelFactory))
                return;
        }

        CommonUtils.aboutError("PP: Cannot handle XML Tag: " + node.getNodeName());
    }

    public void handleEntry(Object entry, Plan plan, ModelFactory modelFactory) {
        if (CommonUtils.PP_DEBUG_debug) System.out.println("PP: handleEntry " + entry );
        for (JSONHandler handler : jsonEntryHandlers) {

            if (handler.handle(entry, plan, modelFactory))
                return;
        }

        CommonUtils.aboutError("PP: Cannot handle JSON Entry: " + entry.toString());
    }

    public LinkedHashMap<Long, AlicaElement> getParsedElements() {
        return mf.getElements();
    }
}
