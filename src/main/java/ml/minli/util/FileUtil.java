package ml.minli.util;

import java.io.InputStream;
import java.net.URL;

public class FileUtil {

    public static URL getResource(String filePath) {
        return FileUtil.class.getClassLoader().getResource(filePath);
    }

    public static InputStream getResourceAsStream(String filePath) {
        return FileUtil.class.getClassLoader().getResourceAsStream(filePath);
    }

}
