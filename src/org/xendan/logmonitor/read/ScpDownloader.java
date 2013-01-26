package org.xendan.logmonitor.read;

import com.jcraft.jsch.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class ScpDownloader {

    private String host;
    private UserInfo info;
    private String path;
    private String login;
    private int port;

    public ScpDownloader(String host, String login, UserInfo info, String path, int port) {
        this.host = host;
        this.path = path;
        this.info = info;
        this.login = login;
        this.port = port;
    }

    public String downloadToLocal() {
        String prefix = System.getProperty("user.home") + "/.logmonitor/";
        String localPath = null;
        FileOutputStream fileOutputStream = null;
        try {

            Session session = createSession();
            Channel channel = createChannel(session);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            sendZero(out, buf);

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }
                // read '0644 '
                in.read(buf, 0, 5);

                long fileSize = getFileSize(in, buf);

                String file = getFileName(in, buf);
                sendZero(out, buf);

                localPath = prefix + file;
                fileOutputStream = readToLocalFile(localPath, in, buf, fileSize);

                if (checkAck(in) != 0) {
                    //TODO add warning here System.exit(0);
                }

                sendZero(out, buf);
            }

            session.disconnect();

        } catch (Exception e) {
            handleException(fileOutputStream, e);

        }
        return localPath;
    }

    private void handleException(FileOutputStream fileOutputStream, Exception e) {
        //            System.out.println(e);
        //TODO Event logger or smth
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception exc) {
            //TODO exception
        }
    }

    private void sendZero(OutputStream out, byte[] buf) throws IOException {
        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
    }

    private long getFileSize(InputStream in, byte[] buf) throws IOException {
        long fileSize = 0L;
        while (true) {
            if (in.read(buf, 0, 1) < 0) {
                // error
                break;
            }
            if (buf[0] == ' ') break;
            fileSize = fileSize * 10L + (long) (buf[0] - '0');
        }
        return fileSize;
    }

    private FileOutputStream readToLocalFile(String localPath,  InputStream in, byte[] buf, long fileSize) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(localPath);

        int tempSize;
        while (true) {
            if (buf.length < fileSize) {
                tempSize = buf.length;
            } else {
                tempSize = (int) fileSize;
            }
            tempSize = in.read(buf, 0, tempSize);
            if (tempSize < 0) {
                // error
                break;
            }
            fileOutputStream.write(buf, 0, tempSize);
            fileSize -= tempSize;
            if (fileSize == 0L) {
                break;
            }
        }
        fileOutputStream.close();
        return fileOutputStream;
    }

    private String getFileName(InputStream in, byte[] buf) throws IOException {
        for (int i = 0; ; i++) {
            in.read(buf, i, 1);
            if (buf[i] == (byte) 0x0a) {
                return new String(buf, 0, i);
            }
        }
    }

    private Channel createChannel(Session session) throws JSchException {
        // exec 'scp -f path' remotely
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand("scp -f " + path);
        return channel;
    }

    private Session createSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(login, host, port);
        session.setUserInfo(info);
        session.connect();
        return session;
    }

    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuffer stringBuffer = new StringBuffer();
            int c;
            do {
                c = in.read();
                stringBuffer.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                //TODO: log error   System.out.print(stringBuffer.toString());
            }
            if (b == 2) { // fatal error
                //TODO: log error   System.out.print(stringBuffer.toString());
            }
        }
        return b;
    }

}
