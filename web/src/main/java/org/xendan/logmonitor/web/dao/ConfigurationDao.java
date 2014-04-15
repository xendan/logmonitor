package org.xendan.logmonitor.web.dao;

import org.xendan.logmonitor.model.*;

import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface ConfigurationDao {

    List<Configuration> getConfigs();

    List<LogEntry> getNotGroupedMatchedEntries(MatchConfig matchConfig, Environment environment);

    List<LogEntryGroup> getMatchedEntryGroups(MatchConfig matchConfig, Environment environment);

    void removeMatchConfig(Environment environment, MatchConfig config);

    void remove(BaseObject group);

    void removeAllEntries(Environment environment);

    void clearAll();

    void persist(BaseObject baseObject);

    void merge(BaseObject config);

    Configuration getConfig(Long configId);

    List<Server> getAllServers();
}
