package de.uniks.vs.jalica.supplementary;

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
    public static final String PACKAGE = "src/de/uniks/vs/jalica";

    public static boolean pathExists(String basePlanPath) {
        String workDir = extractWorkDir()+ PATH_SEPARATOR + PACKAGE + PATH_SEPARATOR;
        File path = new File(workDir + basePlanPath);
//        System.out.println(path.exists() +" " +path.getAbsolutePath());
        return path.exists();
    }

    private static String extractWorkDir() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    public static boolean isPathRooted(String roleDir) {
        return false;
    }

    public static String getParent(String path) {
        return getParentAsFile(path).getAbsolutePath().toString();
    }

    private static File getParentAsFile(String path) {
//        String workDir = extractWorkDir()+ PATH_SEPARATOR + PACKAGE + PATH_SEPARATOR;
//        File _path = new File(workDir);
//
//        if (!_path.exists()){
//            System.err.println(_path + " not exists !!!!!");
//            System.exit(0);
//        }
//        return _path.getParentFile();

        File file = new File(path);

        if (!file.exists()){
            System.err.println("ABORT: FS: " + file + " not exists !!!!!");
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file.getParentFile();
    }

    public static String combinePaths(Object combinePaths) {
        return null;
    }

    public static String combinePaths(String baseRolePath, String roleSetDir) {
        return null;
    }

    public static boolean endsWith(String roleSetName, String s) {
        return false;
    }

    public static String findFile(String path, String file, String path_found) {

        //cout << "ff: Path: " << path << " file: " << file << endl;

        if (!pathExists(path)) {
            return null;
        }
        String workDir = extractWorkDir()+ PATH_SEPARATOR + PACKAGE + PATH_SEPARATOR;
        path = workDir+ path;

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
                String found = findFile(curFullFile + "/", file, path_found);

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
                    path_found = curFullFile.getAbsolutePath();
                    break;
                }
            }
            else
            {
                System.out.println("FS: Found a symlink, or something else, which is not a regular file or directory: " + curFullFile);
            }

//            free(namelist[i]);
        }

//        for (; i < n; i++)
//        {
//            free(namelist[i]);
//        }
//
//        free(namelist);
        return path_found;

//
//        masterPlanPath = ".";
//        return masterPlanPath;
    }
}
