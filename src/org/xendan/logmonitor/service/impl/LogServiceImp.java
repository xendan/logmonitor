package org.xendan.logmonitor.service.impl;

import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.service.LogService;

import java.util.List;

/**
 * User: id967161
 * Date: 20/09/13
 */
public class LogServiceImp implements LogService {

    private final LogEntryDao logEntryDao;
    private final ConfigurationDao configDao;


    public LogServiceImp(LogEntryDao logEntryDao, ConfigurationDao configDao) {
        this.logEntryDao = logEntryDao;
        this.configDao = configDao;
    }

    @Override
    public List<Configuration> getConfigs() {
        return configDao.getConfigs();
    }

    @Override
    public List<LogEntry> getMatchedEntries(MatchConfig matchConfig) {
        return logEntryDao.getMatchedEntries(matchConfig);
    }

    @Override
    public void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, Environment settings) {
        logEntryDao.addMatchConfig(matcher, parentMatcher, settings);
    }

    @Override
    public void clearEntries(Environment settings) {
        logEntryDao.clearEntries(settings);
    }

    @Override
    public LogEntry getLastEntry(Environment environment) {
        return logEntryDao.getLastEntry(environment);
    }

    @Override
    public void addEntries(List<LogEntry> entries) {
        logEntryDao.addEntries(entries);
    }
}
