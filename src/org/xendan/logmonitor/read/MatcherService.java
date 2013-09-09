package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.apache.commons.io.FileUtils;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.Matchers;
import org.xendan.logmonitor.model.ServerSettings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: id967161
 * Date: 06/09/13
 */
public class MatcherService {
    public static final String MATCHERS_XML = "matchers.xml";
    private final HomeResolver homereolver;

    public MatcherService() {
        this(ServiceManager.getService(HomeResolver.class));
    }

    public MatcherService(HomeResolver homeResolver) {
        this.homereolver = homeResolver;

    }

    public Map<ServerSettings, Matchers> getMatchers(LogMonitorConfiguration config) {
        Map<ServerSettings, Matchers> mathcers = new HashMap<ServerSettings, Matchers>();
        for (ServerSettings settings : config.getServerSettings()) {
            mathcers.put(settings, getOrCreateMatcher(settings));
        }
        return mathcers;
    }

    private Matchers getOrCreateMatcher(ServerSettings settings) {
        ScpSynchroniser reader = new ScpSynchroniser(settings);
        String localFile = reader.downloadFile(MATCHERS_XML, getMatchersFile(settings));
        return readFromFile(localFile);
    }

    private Matchers readFromFile(String localFile) {
        if (localFile == null) {
            return new Matchers();
        }
        try {
            return (Matchers) createContext().createUnmarshaller().unmarshal(new File(localFile));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Error unmarshaling file", e);
        }
    }

    public Matchers getLocalMatchers(String project) {
        return readFromFile(homereolver.joinMkDirs(MATCHERS_XML, project));
    }

    private JAXBContext createContext() throws JAXBException {
        return JAXBContext.newInstance(Matchers.class);
    }

    private String getMatchersFile(ServerSettings settings) {
        return homereolver.join(settings.getName(), MATCHERS_XML);
    }

    public void save(Map<ServerSettings, Matchers> matchers) {
        for (Map.Entry<ServerSettings, Matchers> entry : matchers.entrySet()) {
            saveMatcher(entry.getKey(), entry.getValue());
        }
    }

    private void saveMatcher(ServerSettings settings, Matchers matcher) {
        String localFile = homereolver.getPath(getMatchersFile(settings));
        try {
            Marshaller jaxbMarshaller = createContext().createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(matcher, new File(localFile));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Error marshaling " + matcher, e);
        }
        ScpSynchroniser synchroniser = new ScpSynchroniser(settings);
        synchroniser.uploadFile("", getMatchersFile(settings));
    }

    private void toFileAndToSever(String content, String fileName, ScpSynchroniser synchroniser) {
        String localPath = homereolver.join(synchroniser.getSeverName(), fileName);
        try {
            FileUtils.writeStringToFile(new File(homereolver.getPath(localPath)), content);
        } catch (IOException e) {
            throw new IllegalStateException("Error saving to file " + fileName, e);
        }
        synchroniser.uploadFile("", localPath);
    }
}
