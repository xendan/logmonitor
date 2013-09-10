package org.xendan.logmonitor.dao.impl;

import com.intellij.openapi.components.ServiceManager;
import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.model.ServerSettings;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class LogEntryDaoImpl implements LogEntryDao {

    private final EntityManager entityManager;

    public LogEntryDaoImpl() {
        this.entityManager = ServiceManager.getService(LogMonitorSettingsDao.class).getEntityManager();
    }

    @Override
    public LogEntry getLastEntry(ServerSettings settings) {
        List<LogEntry> entries = entityManager.createQuery(
                "SELECT e FROM LogEntry e where e.matcher in (:matchers) ORDER BY e.date DESC ",
                LogEntry.class)
                .setParameter("matchers", settings.getMatchConfigs())
                .setMaxResults(1)
                .getResultList();
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(0);
    }

    @Override
    public List<LogEntry> getMatchedEntries(MatchConfig matchConfig) {
        return entityManager.createQuery(
                "SELECT e FROM LogEntry e where e.matcher = (:matcher) ORDER BY e.date DESC ",
                LogEntry.class)
                .setParameter("matcher", matchConfig)
                .getResultList();
    }

    @Override
    public void addEntries(List<LogEntry> entries) {
        entityManager.getTransaction().begin();
        for (LogEntry entry : entries) {
            entityManager.merge(entry);
        }
        entityManager.getTransaction().commit();
    }
}
