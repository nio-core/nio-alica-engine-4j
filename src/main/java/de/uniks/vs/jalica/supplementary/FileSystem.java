package de.uniks.vs.jalica.supplementary;

import de.uniks.vs.jalica.engine.common.CommonUtils;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by alex on 14.07.17.
 */
public class FileSystem {
//    public static final String PATH_SEPARATOR = ".";
    public static final String PATH_SEPARATOR = "/";
    public static final String CURDIR = ".";
    public static final String PARENTDIR = "..";

    public static String PACKAGE_ROOT = ".";
    public static String PACKAGE_SRC = "src/main/java/de/uniks/vs/jalica";

    public static boolean pathExists(String basePlanPath) {
        return (getAbsolutePath(basePlanPath)!= null);
    }

    public static String getAbsolutePath(String basePlanPath) {
        File basePlanFile = new File(PACKAGE_ROOT + basePlanPath);

        if(basePlanFile.exists())
            return basePlanFile.getAbsolutePath();

        String workDir = extractWorkDir()+ PATH_SEPARATOR + PACKAGE_SRC + PATH_SEPARATOR;

        if (CommonUtils.FS_DEBUG_debug) System.out.println("FS: getAbsolutePath workpath=" + workDir);

        if (!basePlanPath.startsWith(workDir)) {
            workDir = workDir + basePlanPath;
        }
        File path = new File(workDir);

        if (CommonUtils.FS_DEBUG_debug)  System.out.println("FS: " +path.exists() +" " +path.getAbsolutePath());

        if (path.exists()) {
            if (CommonUtils.FS_DEBUG_debug)  System.out.println("FS: getAbsolutePath existing path=" + path);
            return path.getAbsolutePath();
        }

        path = new File(basePlanPath);
        if (CommonUtils.FS_DEBUG_debug)  System.out.println("FS: getAbsolutePath path=" + path);

        return path.exists() ? path.getAbsolutePath(): null;
    }

    public static String extractWorkDir() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    public static boolean isPathRooted(String path) {
        if (!path.isEmpty() && path.indexOf(PATH_SEPARATOR) == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static String getParent(String path) {
        return getParentAsFile(path).getAbsolutePath().toString();
    }

    public static File getParentAsFile(String pathString) {
        File path = new File(pathString);

        if (path.exists()) {
            return path.getParentFile();
        }
        String workDir = extractWorkDir()+ PATH_SEPARATOR + PACKAGE_SRC + PATH_SEPARATOR;

        if (!pathString.startsWith(workDir)) {
            File file = new File(workDir + pathString);

            if (file.exists())
                pathString = workDir + pathString;
            else
                pathString = PACKAGE_ROOT + pathString;
        }
        path = new File(pathString);
//        String workDir = extractWorkDir()+ PATH_SEPARATOR + PACKAGE_SRC + PATH_SEPARATOR;
//        File _path = new File(workDir);
//
//        if (!_path.exists()){
//            System.err.println(_path + " not exists !!!!!");
//            System.exit(0);
//        }
//        return _path.getParentFile();

//        File file = new File(path);

        if (!path.exists()){
            System.err.println("ABORT: FS: " + path + " not exists !!!!!");
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return path.getParentFile();
    }

    public static String combinePaths(Object combinePaths) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    public static String combinePaths(String path1, String path2) {
        if (path1.length() == 0)
        {
            return path2;
        }
        else if (path1.endsWith(path2) || path2.length() == 0)
        {
            return path1;
        }
        else if (path1.endsWith(path2)){

        }
        if (path1.lastIndexOf(PATH_SEPARATOR) != path1.length() - 1)
        {
            return path1 + PATH_SEPARATOR + path2;
        }
        return path1 + path2;
    }

    public static boolean endsWith(String string, String prefix) {
        return string.endsWith(prefix);
    }

    public static String findFile(String path, String file) {

        System.out.println("FS: Path: " + path + " file: " + file);

        File expectedPath = new File(path);

        if (!expectedPath.exists() || !pathExists(path)) {

            if(!path.startsWith(PACKAGE_ROOT))
                return findFile(PACKAGE_ROOT + path, file);
            return null;
        }
        String workDir = extractWorkDir()+ PATH_SEPARATOR + PACKAGE_SRC + PATH_SEPARATOR;
        // FIXME: replace workaround "if (..)"

        if (!path.contains(workDir)) {
//            path = workDir + path;
            workDir = path;
        }

//        struct dirent **namelist;
        int i, n;
//        n = scandir(path.c_str(), &namelist, 0, alphasort);
        File _path = new File(path);
        File[] files = _path.listFiles();
        n = files.length;


//        if (n < 0)
//        {
//            perror("FileSystem::findFile");
//            free(namelist);
//            return false;
//        }

        if (!_path.isDirectory() || files.length < 1)
        {
            System.err.println("FS:: findFile");
//            free(namelist);
            return null;
        }
        boolean fileFound = false;
        String found = null;

//        for (i = 0; i < n; i++)
        for (File curFullFile: files) {

            //cout << "ff: Namelist " << i << ": " << namelist[i].d_name << endl;
//            String curFile = namelist[i].d_name;
//            string curFullFile = combinePaths(path, curFile);

//            if (isDirectory(curFullFile))
            if (curFullFile.isDirectory())
            {
                // ignore current or parent directory
//                if (CURDIR.compare(curFile) == 0 || PARENTDIR.compare(curFile) == 0)
                if (CURDIR.equals(curFullFile.getName()) || PARENTDIR.equals(curFullFile.getName()))
                {
//                    free(namelist[i]);
                    continue;
                }

                // recursively call this method for regular directories
                found = findFile(curFullFile + "/", file);

                if (found != null)
                {
                    fileFound = true;
                    break;
                }
            }
//            else if (isFile(curFullFile))
            else if (curFullFile.isFile())
            {
//                if (file.compare(namelist[i].d_name) == 0)
                if (file.equals(curFullFile.getName()))
                {
                    // file found, so return the full path
                    fileFound = true;
                    return curFullFile.getAbsolutePath();
//                    break;
                }
            }
            else
            {
                System.err.println("FS: Found a symlink, or something else, which is not a regular file or directory: " + curFullFile);
            }

//            free(namelist[i]);
        }

//        for (; i < n; i++)
//        {
//            free(namelist[i]);
//        }
//
//        free(namelist);
        return found;

//
//        masterPlanPath = ".";
//        return masterPlanPath;
    }

    public static String realpath(String path, String resolved_path) {
        String absolutePath = path;
//        CommonUtils.aboutImplIncomplete();
        return absolutePath;
    }
}
