package ml.minli.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class SSHUtil {

    private static Channel channel;

    private static OutputStream outputStream;

    public static Session getJSchSession(String username, String password, String host, int port) throws Exception {
        JSch jSch = new JSch();
        Session session = jSch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    public static void execCommand(Session session, String command, TextArea textArea) throws Exception {
        if (command == null || command.isEmpty()) {
            return;
        }
        setOut(textArea);
        if (channel == null || command.contains("bash")) {
            channel = session.openChannel("shell");
            channel.setOutputStream(System.out, true);
            outputStream = channel.getOutputStream();
            channel.connect();
        }
        if (command.contains("cd") || command.contains("bash") || command.contains("exit")) {
            command = command + " \r";
        } else {
            command = command + " | sed -r \"s/\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]//g\" \r";
        }
        outputStream.write(command.getBytes());
        outputStream.flush();
        if (command.contains("exit")) {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public static void setOut(TextArea textArea) {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                String text = String.valueOf((char) b);
                Platform.runLater(() -> textArea.appendText(text));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String text = new String(b, off, len, StandardCharsets.UTF_8);
                Platform.runLater(() -> textArea.appendText(text));
            }
        }, true));
    }
}
