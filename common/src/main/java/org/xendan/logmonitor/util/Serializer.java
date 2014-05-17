package org.xendan.logmonitor.util;

import java.io.*;

/**
 * User: id967161
 * Date: 09/09/13
 */
@SuppressWarnings("unchecked")
public class Serializer {

    public <T> T doCopy(T source) {
        ByteArrayOutputStream bos = null;
        ObjectInputStream in = null;
        try {
            bos = toByteArrayOutputStream(source);
            in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return (T) in.readObject();
        } catch (Exception e) {
            throw new IllegalStateException("Error creating copy", e);
        } finally {
            close(bos, in);
        }
    }

    public static void close(Closeable... str) {
        for (Closeable closeable : str) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    //TODO log
                }
            }
        }
    }

    public <T> ByteArrayOutputStream toByteArrayOutputStream(T source) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(source);
        out.flush();
        out.close();
        return bos;
    }
}
