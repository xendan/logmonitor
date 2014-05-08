package org.xendan.logmonitor.web.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.util.Serializer;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.read.parse.PatternUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: id967161
 * Date: 22/11/13
 */
public class LogServiceImpl implements LogServicePartial {
    private static final Logger logger = Logger.getLogger(LogServiceImpl.class);

    private static final String DEF_PATH = "db";
    private static final int MATCHING_INDEX = 5;

    protected final ConfigurationDao dao;
    private final HomeResolver resolver;

    public LogServiceImpl(HomeResolver resolver, ConfigurationDao dao) {
        this.resolver = resolver;
        this.dao = dao;
    }

    @Override
    public void setEnvironmentStatus(Environment environment, EnvironmentStatus status) {
        environment.setStatus(status);
        dao.persist(environment);
    }

    public List<LogEntryGroup> getMatchedEntryGroups(final MatchConfig matchConfig, final Environment environment) {
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);
        Collections.sort(groups, new GroupComparator());
        return groups;
    }

    public void addMatchConfig(final Environment environment, final MatchConfig newConfig) {
        environment.getMatchConfigs().add(newConfig);
        dao.persist(environment);
        for (MatchConfig matchConfig : environment.getMatchConfigs()) {
            if (!matchConfig.equals(newConfig)) {
                checkMatchConfigContainsExampleForNew(environment, newConfig, matchConfig);
            }
        }
    }

    private void checkMatchConfigContainsExampleForNew(Environment environment, MatchConfig newConfig, MatchConfig matchConfig) {
        if (matchConfig.isGeneral()) {
            for (LogEntryGroup group : dao.getMatchedEntryGroups(matchConfig, environment)) {
                boolean groupChanged = false;
                for (LogEntry entry : new ArrayList<LogEntry>(group.getEntries())) {
                    restoreMessage(entry, group.getMessagePattern());
                    if (PatternUtils.isMatch(entry, newConfig)) {
                        group.getEntries().remove(entry);
                        groupChanged = true;
                        //TODO message!
                        entry.setMatchConfig(newConfig);
                        dao.persist(entry);
                    }
                }
                if (groupChanged) {
                    if (group.getEntries().isEmpty()) {
                        dao.remove(group);
                    } else {
                        dao.persist(group);
                    }
                }
            }
            for (LogEntry entry : dao.getNotGroupedMatchedEntries(matchConfig, environment)) {
                if (!StringUtils.isEmpty(matchConfig.getMessage())) {
                    restoreMessage(entry, matchConfig.getMessage());
                }
                if (PatternUtils.isMatch(entry, newConfig)) {
                    //TODO message!
                    entry.setMatchConfig(newConfig);
                    dao.persist(entry);
                }
            }
        }
    }

    private void restoreMessage(LogEntry entry, String messagePattern) {
        entry.setMessage(PatternUtils.restoreMessage(entry, messagePattern));
    }

    public void remove(final BaseObject object) {
        if (object instanceof LogEntryGroup) {
            LogEntryGroup group = (LogEntryGroup) object;
            for (LogEntry logEntry : group.getEntries()) {
                dao.remove(logEntry);
            }
        }
        dao.remove(object);

    }

    public void save(final List<Configuration> configs) {
        tmpSaveConfig(configs);
        for (Configuration config : configs) {
            dao.persist(config);
        }
    }


    private void tmpSaveConfig(List<Configuration> configs) {
        HomeResolver resolver = new HomeResolver();
        String filePath = resolver.joinMkDirs("tmp.config", "tmpconfig");
        File file = new File(filePath);
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new IllegalStateException("Error creating file " + filePath);
            }
            OutputStream outputStream = new FileOutputStream(filePath);
            new Serializer().toByteArrayOutputStream(configs).writeTo(outputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Error writing entries", e);
        }
    }

    public void clearAll(final boolean createTestTmp) {
        dao.clearAll();
        //TODO  recreate dao
        if (createTestTmp) {
            for (Configuration config : createTmpConfig()) {
                dao.merge(config);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Configuration> createTmpConfig() {
        ObjectInputStream in = null;
        InputStream rin = null;
        try {
            rin = getClass().getResourceAsStream("/tmp.config");
            in = new ObjectInputStream(rin);
            return (List<Configuration>) in.readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading entries", e);
        } finally {
            Serializer.close(rin, in);
        }
    }

    public void removeMatchConfig(final MatchConfig match, final Environment environment) {
        environment.getMatchConfigs().remove(match);
        dao.persist(environment);
        dao.removeMatchConfig(environment, match);
    }


    @Override
    public void addEntries(final List<LogEntry> entries) {
        for (LogEntry entry : entries) {
            if (!entry.getMatchConfig().isGeneral()) {
                dao.persist(getLogEntryForNotGeneral(entries, entry));
            } else {
                List<LogEntryGroup> groups = dao.getMatchedEntryGroups(entry.getMatchConfig(), entry.getEnvironment());
                persistGroupCandidate(entry, getMatchedGroup(groups, entry));
            }
        }
    }

    private LogEntryGroup getMatchedGroup(List<LogEntryGroup> groups, LogEntry entry) {
        for (LogEntryGroup group : groups) {
            if (Pattern.matches(group.getMessagePattern(), entry.getMessage())) {
                return group;
            }
        }
        return null;
    }

    private void persistGroupCandidate(LogEntry entry, LogEntryGroup matchedGroup) {
        if (matchedGroup != null) {
            entry.setMessage(getGroupContent(matchedGroup.getMessagePattern(), entry.getMessage()));
            matchedGroup.getEntries().add(entry);
            dao.persist(entry);
            dao.persist(matchedGroup);
        } else {
            List<LogEntry> oldEntries = dao.getNotGroupedMatchedEntries(entry.getMatchConfig(), entry.getEnvironment());
            boolean matchFound = false;
            for (Iterator<LogEntry> iterator = oldEntries.iterator(); iterator.hasNext() && !matchFound; ) {
                matchFound = checkIfShouldBeSameGroup(entry, iterator.next());
            }
            if (!matchFound) {
                dao.persist(entry);
            }
        }
    }

    private String getGroupContent(String commonPattern, String message) {
        if (PatternUtils.simpleToRegexp(message).equals(commonPattern)) {
            return "";
        }
        Matcher matcher = Pattern.compile(commonPattern, Pattern.DOTALL).matcher(message);
        if (matcher.matches() && matcher.groupCount() > 0) {
            return matcher.group(1);
        }
        //TODO log or something
        throw new IllegalArgumentException("No match of  *******\n" + commonPattern + "\n to message ********\n" + message + "\n********\n");
    }

    private boolean checkIfShouldBeSameGroup(LogEntry entry, LogEntry oldEntry) {
        String commonPattern = getCommonPattern(entry.getMessage(), oldEntry.getMessage());
        if (commonPattern != null) {
            LogEntryGroup group = new LogEntryGroup();
            group.setMessagePattern(commonPattern);
            oldEntry.setMessage(getGroupContent(commonPattern, oldEntry.getMessage()));
            entry.setMessage(getGroupContent(commonPattern, entry.getMessage()));
            group.getEntries().add(oldEntry);
            group.getEntries().add(entry);
            dao.persist(group);
            return true;
        }
        return false;
    }

    private String getCommonPattern(String message1, String message2) {
        if (message1.equals(message2)) {
            return PatternUtils.simpleToRegexp(message1);
        }
        if (message1.isEmpty() || message2.isEmpty()) {
            return null;
        }
        int noMatchStart = -1;
        int separatorPosition = 0;
        boolean doMatch = true;
        while (doMatch) {
            noMatchStart++;
            if (noMatchStart >= message1.length() || noMatchStart >= message2.length()) {
                doMatch = false;
            } else {
                if (message1.charAt(noMatchStart) != message2.charAt(noMatchStart)) {
                    doMatch = false;
                }
                if (doMatch && isSeparator(message1, noMatchStart)) {
                    separatorPosition = noMatchStart + 1;
                }
            }
        }
        boolean includeAtStart = true;
        if (noMatchStart < message1.length() && noMatchStart < message2.length()) {
            includeAtStart = false;
            noMatchStart = separatorPosition;
        }
        int noMatchEnd = 0;
        separatorPosition = 0;
        boolean sameEnd = false;
        doMatch = true;
        while (doMatch && !includeAtStart) {
            noMatchEnd++;
            int pos1 = message1.length() - noMatchEnd;
            int pos2 = message2.length() - noMatchEnd;
            if (pos1 > 0 && pos2 > 0) {
                if (message1.charAt(pos1) != message2.charAt(pos2)) {
                    doMatch = false;
                }
                if (doMatch && isSeparator(message1, pos1)) {
                    separatorPosition = noMatchEnd;
                }
            } else {
                doMatch = false;
                sameEnd = true;
            }
        }
        if (!sameEnd && !includeAtStart) {
            noMatchEnd = separatorPosition;
        }
        int matchingLength = 2 * noMatchStart + 2 * noMatchEnd;
        int notMatchLength = message1.length() + message2.length() - 2 * noMatchStart - 2 * noMatchEnd;
        if (matchingLength / notMatchLength > MATCHING_INDEX) {
            return PatternUtils.simpleToRegexp(message1.substring(0, noMatchStart)) + PatternUtils.ALL_GROUP +
                    PatternUtils.simpleToRegexp(message1.substring(message1.length() - noMatchEnd));
        }
        return null;
    }

    private boolean isSeparator(String message1, int noMatchStart) {
        return Pattern.matches("[^\\w\\d_]", String.valueOf(message1.charAt(noMatchStart)));
    }

    private LogEntry getLogEntryForNotGeneral(List<LogEntry> entries, LogEntry entry) {
        List<LogEntry> oldEntries = dao.getNotGroupedMatchedEntries(entry.getMatchConfig(), entry.getEnvironment());
        if (!oldEntries.isEmpty()) {
            LogEntry first = oldEntries.get(0);
            first.setDate(entry.getDate());
            first.setFoundNumber(first.getFoundNumber() + 1);
            for (int i = 1; i < entries.size(); i++) {
                dao.remove(entries.get(i));
            }
            entry = first;
        }
        return entry;
    }

    private class GroupComparator implements Comparator<LogEntryGroup> {
        @Override
        public int compare(LogEntryGroup o1, LogEntryGroup o2) {
            return o2.getEntries().size() - o1.getEntries().size();
        }
    }
}
