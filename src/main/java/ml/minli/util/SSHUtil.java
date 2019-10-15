package ml.minli.util;

import com.jcraft.jsch.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SSHUtil {

    private static Channel channel;

    private static ChannelSftp sftp;

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

    private static void execOnceCommand(Session session, String command) throws Exception {
        if (command == null || command.isEmpty()) {
            return;
        }
        channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        channel.connect();
    }

    public static synchronized void upload(Session session, String localPath, String serverPath, SftpProgressMonitor sftpProgressMonitor) throws Exception {
        if (sftp == null || sftp.isClosed()) {
            sftp = (ChannelSftp) session.openChannel("sftp");
            Field field = ChannelSftp.class.getDeclaredField("server_version");
            field.setAccessible(true);
            field.set(sftp, 2);
        }
        sftp.connect();
        sftp.setFilenameEncoding(System.getProperty("file.encoding"));
        List<FilePath> filePathList = new ArrayList<>();
        UploadUtil.getAllFilePath(localPath, filePathList);
        List<String> commandList = UploadUtil.getAllCreateDirectoryCommand(filePathList, localPath, serverPath);
        if (commandList != null) {
            commandList.forEach(s -> {
                try {
                    execOnceCommand(session, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        List<FilePath> localFileList = filePathList.stream().filter(filePath -> !filePath.isDirectory()).collect(Collectors.toList());
        List<String> serverFileList = UploadUtil.getAllServerPath(localPath, serverPath, localFileList);
        if (serverFileList == null || localFileList.isEmpty() || serverFileList.isEmpty() || localFileList.size() != serverFileList.size()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("上传结果");
                alert.setResizable(false);
                alert.setContentText("上传失败");
                alert.show();
            });
        } else {
            for (int i = 0; i < localFileList.size(); i++) {
                try {
                    sftp.put(localFileList.get(i).getPath(), serverFileList.get(i), sftpProgressMonitor, ChannelSftp.OVERWRITE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sftp.quit();
            sftp.disconnect();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("上传结果");
                alert.setResizable(false);
                alert.setContentText("上传成功");
                alert.show();
            });
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
                String text = new String(b, off, len, System.getProperty("file.encoding"));
                Platform.runLater(() -> textArea.appendText(text));
            }
        }, true));
    }
}
