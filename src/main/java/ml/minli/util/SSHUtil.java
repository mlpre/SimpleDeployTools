package ml.minli.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.InputStream;
import java.lang.reflect.Field;

public class SSHUtil {

    public static String ip = null;
    public static int port = 22;
    public static String username = null;
    public static String password = null;

    public static synchronized void upload(Session session, InputStream fileStream, String path, String fileName) throws Exception {
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        Field field = ChannelSftp.class.getDeclaredField("server_version");
        field.setAccessible(true);
        field.set(sftp, 2);
        sftp.connect();
        createDir(path, sftp);
        sftp.cd(path);
        sftp.put(fileStream, fileName, ChannelSftp.OVERWRITE);
        sftp.quit();
        sftp.disconnect();
    }

    public static void createDir(String createPath, ChannelSftp sftp) {
        try {
            if (dirIsExist(createPath, sftp)) {
                sftp.cd(createPath);
                return;
            }
            String[] pathArray = createPath.split("/");
            StringBuilder filePath = new StringBuilder("/");
            for (String path : pathArray) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path).append("/");
                if (!dirIsExist(filePath.toString(), sftp)) {
                    sftp.mkdir(filePath.toString());
                }
                sftp.cd(filePath.toString());
            }
            sftp.cd(createPath);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    public static boolean dirIsExist(String directory, ChannelSftp sftp) {
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            return sftpATTRS.isDir();
        } catch (SftpException ignored) {
        }
        return false;
    }

    public static void initSSHParam(String ip, int port, String username, String password) {
        SSHUtil.ip = ip;
        SSHUtil.port = port;
        SSHUtil.username = username;
        SSHUtil.password = password;
    }

}
