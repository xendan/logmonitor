package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.Matchers;
import org.xendan.logmonitor.model.ServerSettings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: id967161
 * Date: 06/09/13
 */
public class MatcherService {
    private static final String MATCHERS_XML = "matchers.xml";
    private final HomeResolver homereolver;

    public MatcherService() {
        homereolver = ServiceManager.getService(HomeResolver.class);
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
        String matchersFile = getMatchersFile(settings);
        String localFile = reader.downloadFile(MATCHERS_XML, settings.getHost() + matchersFile);
        if (localFile == null) {
            return new Matchers();
        }
        try {
            return (Matchers) createContext().createUnmarshaller().unmarshal(new File(localFile));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Error unmarshaling file", e);
        }

    }

    private JAXBContext createContext() throws JAXBException {
        return JAXBContext.newInstance(Matchers.class);
    }

    private String getMatchersFile(ServerSettings settings) {
        homereolver.mkdir(settings.getName());
        return homereolver.join(settings.getName(), MATCHERS_XML);
    }

    public void save(Map<ServerSettings, Matchers> matchers) {
        for (Map.Entry<ServerSettings, Matchers> entry : matchers.entrySet()) {
            String localFile = homereolver.getPath(getMatchersFile(entry.getKey()));
            try {
                Marshaller jaxbMarshaller = createContext().createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                jaxbMarshaller.marshal(entry.getValue(), new File(localFile));
            } catch (JAXBException e) {
                throw new IllegalArgumentException("Error marshaling " + entry.getValue(), e);
            }
            ScpSynchroniser reader = new ScpSynchroniser(entry.getKey());
            reader.uploadFile("", getMatchersFile(entry.getKey()));
        }
    }
}
