package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.LogEntry;

import java.io.*;
import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
@SuppressWarnings("unchecked")
public class Serializer {

    public static final String ENTRIES_FILE = "entries.bo";
    private final HomeResolver resolver;

    public Serializer() {
        this(ServiceManager.getService(HomeResolver.class));
    }

    public Serializer(HomeResolver resolver) {
        this.resolver = resolver;
    }

    public List<LogEntry> readEntries(String project) {
        FileInputStream fin = null;
        ObjectInputStream in = null;
        try {
            fin = new FileInputStream(getEntriesPath(project));
            in = new ObjectInputStream(fin);
            return (List<LogEntry>) in.readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading entires", e);
        } finally {
            close(fin, in);
        }
    }

    public void writeEntries(List<LogEntry> entries, String project) {
        String filePath = getEntriesPath(project);
        File file = new File(filePath);
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new IllegalStateException("Error creating file " + filePath);
            }
            OutputStream outputStream = new FileOutputStream(filePath);
            toByteArrayOutputStream(entries).writeTo(outputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Error writing entries", e);
        }
    }

    private String getEntriesPath(String project) {
        return resolver.joinMkDirs(ENTRIES_FILE, project);
    }

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

    private void close(Closeable... str) {
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

    private <T> ByteArrayOutputStream toByteArrayOutputStream(T source) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(source);
        out.flush();
        out.close();
        return bos;
    }
}
