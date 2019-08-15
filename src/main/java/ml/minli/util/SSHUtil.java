package ml.minli.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class SSHUtil {

    public static Session getJSchSession(String username, String password, String host, int port) throws Exception {
        JSch jSch = new JSch();
        Session session = jSch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    public static String execCommand(Session session, String command) throws Exception {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        InputStream inputStream = channelExec.getInputStream();
        channelExec.setCommand(command);
        channelExec.setErrStream(System.err);
        channelExec.connect();
        String result = IOUtils.toString(inputStream, "UTF-8");
        channelExec.disconnect();
        return result;
    }

}
