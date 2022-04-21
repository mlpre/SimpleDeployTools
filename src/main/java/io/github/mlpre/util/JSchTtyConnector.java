package io.github.mlpre.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class JSchTtyConnector<T extends Channel> implements TtyConnector {

    public static final int DEFAULT_PORT = 22;

    private InputStream myInputStream = null;
    private OutputStream myOutputStream = null;
    private Session mySession;
    private T myChannelShell;
    private AtomicBoolean isInitiated = new AtomicBoolean(false);

    private int myPort = DEFAULT_PORT;

    private String myUser = null;
    private String myHost = null;
    private String myPassword = null;

    private Dimension myPendingTermSize;
    private Dimension myPendingPixelSize;
    private InputStreamReader myInputStreamReader;
    private OutputStreamWriter myOutputStreamWriter;

    public JSchTtyConnector() {

    }

    public JSchTtyConnector(String host, String user, String password) {
        this(host, DEFAULT_PORT, user, password);
    }

    public JSchTtyConnector(String host, int port, String user, String password) {
        this.myHost = host;
        this.myPort = port;
        this.myUser = user;
        this.myPassword = password;
    }

    public void resize(Dimension termSize, Dimension pixelSize) {
        myPendingTermSize = termSize;
        myPendingPixelSize = pixelSize;
        if (myChannelShell != null) {
            resizeImmediately();
        }
    }

    abstract protected void setPtySize(T channel, int col, int row, int wp, int hp);

    private void resizeImmediately() {
        if (myPendingTermSize != null && myPendingPixelSize != null) {
            setPtySize(myChannelShell, myPendingTermSize.width, myPendingTermSize.height, myPendingPixelSize.width, myPendingPixelSize.height);
            myPendingTermSize = null;
            myPendingPixelSize = null;
        }
    }

    public void close() {
        if (mySession != null) {
            mySession.disconnect();
            mySession = null;
            myInputStream = null;
            myOutputStream = null;
        }
    }

    abstract protected T openChannel(Session session) throws JSchException;

    abstract protected void configureChannelShell(T channel);

    public boolean init(Questioner q) {

        getAuthDetails(q);

        try {
            mySession = connectSession(q);
            myChannelShell = openChannel(mySession);
            configureChannelShell(myChannelShell);
            myInputStream = myChannelShell.getInputStream();
            myOutputStream = myChannelShell.getOutputStream();
            myInputStreamReader = new InputStreamReader(myInputStream, StandardCharsets.UTF_8);
            myChannelShell.connect();
            resizeImmediately();
            return true;
        } catch (final IOException | JSchException e) {
            q.showMessage(e.getMessage());
            return false;
        } finally {
            isInitiated.set(true);
        }
    }

    private Session connectSession(Questioner questioner) throws JSchException {
        JSch jsch = new JSch();
        configureJSch(jsch);

        Session session = jsch.getSession(myUser, myHost, myPort);

        final QuestionerUserInfo ui = new QuestionerUserInfo(questioner);
        if (myPassword != null) {
            session.setPassword(myPassword);
            ui.setPassword(myPassword);
        }
        session.setUserInfo(ui);

        final java.util.Properties config = new java.util.Properties();
        config.put("compression.s2c", "zlib,none");
        config.put("compression.c2s", "zlib,none");
        configureSession(session, config);
        session.connect();
        session.setTimeout(0);

        return session;
    }

    protected void configureJSch(JSch jsch) throws JSchException {

    }

    protected void configureSession(Session session, final java.util.Properties config) throws JSchException {
        session.setConfig(config);
        session.setTimeout(5000);
        session.setConfig("StrictHostKeyChecking", "no");
    }

    private void getAuthDetails(Questioner q) {
        myHost = SSHUtil.ip;
        myPort = SSHUtil.port;
        myUser = SSHUtil.username;
        myPassword = SSHUtil.password;
    }

    public String getName() {
        return myHost != null ? myHost : "Remote";
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return myInputStreamReader.read(buf, offset, length);
    }

    public int read(byte[] buf, int offset, int length) throws IOException {
        return myInputStream.read(buf, offset, length);
    }

    public void write(byte[] bytes) throws IOException {
        if (myOutputStream != null) {
            myOutputStream.write(bytes);
            myOutputStream.flush();
        }
    }

    @Override
    public boolean isConnected() {
        return myChannelShell != null && myChannelShell.isConnected();
    }

    @Override
    public void write(String string) throws IOException {
        write(string.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int waitFor() throws InterruptedException {
        while (!isInitiated.get() || isRunning(myChannelShell)) {
            Thread.sleep(100);
        }
        return myChannelShell.getExitStatus();
    }

    private static boolean isRunning(Channel channel) {
        return channel != null && channel.getExitStatus() < 0 && channel.isConnected();
    }

    public Session getMySession() {
        return mySession;
    }

}
