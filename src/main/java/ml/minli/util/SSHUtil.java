package ml.minli.util;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class SSHUtil {

    private static ChannelShell channelShell;

    public static Session getJSchSession(String username, String password, String host, int port) throws Exception {
        JSch jSch = new JSch();
        Session session = jSch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    public static void execCommand(Session session, String command, TextArea textArea) throws Exception {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                String text = String.valueOf((char) b);
                Platform.runLater(() -> textArea.appendText(text));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String text = new String(b, off, len);
                Platform.runLater(() -> textArea.appendText(text));
            }
        }, true));
        if (channelShell == null) {
            channelShell = (ChannelShell) session.openChannel("shell");
            channelShell.connect();
            channelShell.setOutputStream(System.out, true);
        }
        PrintWriter printWriter = new PrintWriter(channelShell.getOutputStream());
        String isColor = "";
        if (command.contains("ls") || command.contains("ll")) {
            isColor = " --color=never";
        }
        printWriter.println(command + isColor);
        printWriter.flush();
    }

}
