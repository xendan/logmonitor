package org.xendan.logmonitor.web.dao;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.model.*;

import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface ConfigurationDao {

    List<Configuration> getConfigs();

    List<LogEntry> getNotGroupedEntries(Long matchConfigId, Long environmentId, LocalDateTime last);

    List<LogEntryGroup> getEntryGroups(Long matchConfigId, Long environmentId, LocalDateTime last);

    void removeMatchConfig(Environment environment, MatchConfig config);

    void remove(BaseObject group);

    void removeAllEntries(Long environmentId, List<Long> matcherId);

    void clearAll();

    void persist(BaseObject baseObject);

    void merge(BaseObject config);

    Configuration getConfig(Long configId);

    List<Server> getAllServers();

    Environment getEnvironment(long environmentId);

    Configuration getConfigByEnvironment(Long envId);
}
