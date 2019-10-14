package ml.minli.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UploadUtil {

    public static void getAllFilePath(String path, List<FilePath> filePathList) {
        File root = new File(path);
        if (!root.exists()) {
            return;
        }
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                filePathList.add(new FilePath().setPath(file.getAbsolutePath()).setDirectory(file.isDirectory()));
                if (file.isDirectory()) {
                    getAllFilePath(file.getAbsolutePath(), filePathList);
                }
            }
        } else {
            filePathList.add(new FilePath().setPath(root.getAbsolutePath()).setDirectory(false));
        }
    }

    private static List<String> getAllDirectoryPath(String localPath, List<FilePath> list) {
        if (!list.isEmpty()) {
            List<FilePath> directoryList = list.stream().filter(FilePath::isDirectory).collect(Collectors.toList());
            List<String> referencePathList = new ArrayList<>();
            for (FilePath filePath : directoryList) {
                referencePathList.add(filePath.getPath().replace(localPath, ""));
            }
            return referencePathList;
        }
        return null;
    }

    private static List<String> generateCreateDirectoryCommand(String serverPath, List<String> referencePathList) {
        List<String> pathList = new ArrayList<>();
        for (String referencePath : referencePathList) {
            pathList.add(("mkdir -p " + serverPath + referencePath).replace("\\", "/"));
        }
        return pathList;
    }

    public static List<String> getAllServerPath(String localPath, String serverPath, List<FilePath> list) {
        if (!list.isEmpty()) {
            List<FilePath> fileList = list.stream().filter(filePath -> !filePath.isDirectory()).collect(Collectors.toList());
            List<String> referencePathList = new ArrayList<>();
            for (FilePath filePath : fileList) {
                referencePathList.add(serverPath + filePath.getPath().replace(localPath, "").replace("\\", "/"));
            }
            return referencePathList;
        }
        return null;
    }

    public static List<String> getAllCreateDirectoryCommand(List<FilePath> filePathList, String localPath, String serverPath) {
        List<String> referencePathList = getAllDirectoryPath(localPath, filePathList);
        if (referencePathList != null && !referencePathList.isEmpty()) {
            return generateCreateDirectoryCommand(serverPath, referencePathList);
        }
        return null;
    }

}
