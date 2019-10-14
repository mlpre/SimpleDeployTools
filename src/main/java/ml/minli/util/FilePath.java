package ml.minli.util;

public class FilePath {

    private String path;

    private boolean isDirectory;

    public String getPath() {
        return path;
    }

    public FilePath setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public FilePath setDirectory(boolean directory) {
        isDirectory = directory;
        return this;
    }
}
