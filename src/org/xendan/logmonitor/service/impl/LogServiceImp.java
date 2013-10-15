package org.xendan.logmonitor.service.impl;

import org.xendan.logmonitor.dao.ConfigurationDao;
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

    private final ConfigurationDao configDao;


    public LogServiceImp(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    @Override
    public List<Configuration> getConfigs() {
        return configDao.getConfigs();
    }

    @Override
    public List<LogEntry> getMatchedEntries(MatchConfig matchConfig, Environment environment) {
        return configDao.getMatchedEntries(matchConfig, environment);
    }

    @Override
    public void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, Environment environment) {
        configDao.addMatchConfig(matcher, parentMatcher, environment);
    }

    @Override
    public void clearEntries(Environment environment) {
        configDao.clearEntries(environment);
    }

    @Override
    public LogEntry getLastEntry(Environment environment) {
        return configDao.getLastEntry(environment);
    }

    @Override
    public void addEntries(List<LogEntry> entries) {
        configDao.addEntries(entries);
    }

    @Override
    public void saveConfigs(List<Configuration> configs) {
        configDao.save(configs);
    }
}
