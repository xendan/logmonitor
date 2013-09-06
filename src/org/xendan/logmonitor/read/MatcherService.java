package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.apache.commons.io.FileUtils;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.EntryMatcher;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.Matchers;
import org.xendan.logmonitor.model.ServerSettings;
import org.xendan.logmonitor.parser.LogParser;

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
        String localFile = reader.downloadFile(MATCHERS_XML, getMatchersFile(settings));
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

    public void save(Map<ServerSettings, Matchers> matchers, LogParser logParser) {
        for (Map.Entry<ServerSettings, Matchers> entry : matchers.entrySet()) {
            saveMatcher(entry.getKey(), entry.getValue(), logParser);
        }
    }

    private void saveMatcher(ServerSettings settings, Matchers matcher, LogParser logParser) {
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
        StringBuilder match = new StringBuilder();
        StringBuilder ingore = new StringBuilder();
        for (EntryMatcher entryMatcher : matcher.getMatchers()) {
            String pattern = logParser.getEntryMatcherPattern(entryMatcher) + "\n";
            if (entryMatcher.isError()) {
                match.append(pattern);
            } else {
                ingore.append(pattern);
            }
        }
        toFileAndToSever(match.toString(), "match_pattern.txt", synchroniser);
        toFileAndToSever(ingore.toString(), "ignore_pattern.txt", synchroniser);
        toFileAndToSever(logParser.getCommonRegexp(), "content_pattern.txt", synchroniser);
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
