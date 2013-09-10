package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Matchers;
import org.xendan.logmonitor.model.ServerSettings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;

/**
 * User: id967161
 * Date: 06/09/13
 */
public class MatcherService {
    public static final String MATCHERS_XML = "matchers.xml";
    private final HomeResolver homeResolver;

    public MatcherService() {
        this(ServiceManager.getService(HomeResolver.class));
    }

    public MatcherService(HomeResolver homeResolver) {
        this.homeResolver = homeResolver;

    }

    public void exportMatchers(ServerSettings settings) {
        //TODO
    }

    public void importMatchers(ServerSettings settings) {
        //TODO
    }

    /*
    public Map<ServerSettings, Matchers> getMatchers(LogMonitorConfiguration config) {
        Map<ServerSettings, Matchers> mathcers = new HashMap<ServerSettings, Matchers>();
        for (ServerSettings settings : config.getServerSettings()) {
            mathcers.put(settings, getOrCreateMatcher(settings));
        }
        return mathcers;
    }*/

    public Matchers getLocalMatchers(String project, ServerSettings serverSettings) {
        String path = homeResolver.joinMkDirs(MATCHERS_XML, project);
        File file = new File(path);
        if (!file.exists()) {
            return getOrCreateMatcher(serverSettings);
        }
        return readFromFile(file);
    }

    private Matchers getOrCreateMatcher(ServerSettings settings) {
        ScpSynchroniser reader = new ScpSynchroniser(settings);
        String localFile = reader.downloadFile(MATCHERS_XML, getMatchersFile(settings));
        return readFromFile(localFile == null ? null : new File(localFile));
    }

    private Matchers readFromFile(File file) {
        if (file == null) {
            return new Matchers();
        }
        try {
            return (Matchers) createContext().createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Error unmarshalling file", e);
        }
    }

    private JAXBContext createContext() throws JAXBException {
        return JAXBContext.newInstance(Matchers.class);
    }

    private String getMatchersFile(ServerSettings settings) {
        return homeResolver.join(settings.getName(), MATCHERS_XML);
    }
     /*
    public void save(Map<ServerSettings, Matchers> matchers) {
        for (Map.Entry<ServerSettings, Matchers> entry : matchers.entrySet()) {
            saveMatcher(entry.getKey(), entry.getValue());
        }
    }

    private void saveMatcher(ServerSettings settings, Matchers matcher) {
        String localFile = homeResolver.getPath(getMatchersFile(settings));
        try {
            Marshaller jaxbMarshaller = createContext().createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(matcher, new File(localFile));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Error marshaling " + matcher, e);
        }
        ScpSynchroniser synchroniser = new ScpSynchroniser(settings);
        synchroniser.uploadFile("", getMatchersFile(settings));
    }  */
}
