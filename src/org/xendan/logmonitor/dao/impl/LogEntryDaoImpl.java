package org.xendan.logmonitor.dao.impl;

import com.intellij.openapi.components.ServiceManager;
import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.parser.EntryMatcher;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public LogEntry getLastEntry(Environment settings) {
        List<LogEntry> entries = entityManager.createQuery(
                "SELECT e FROM LogEntry e where e.matchConfig in (:matchers) ORDER BY e.date DESC ",
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
                "SELECT e FROM LogEntry e where e.matchConfig = (:matcher) ORDER BY e.date DESC ",
                LogEntry.class)
                .setParameter("matcher", matchConfig)
                .getResultList();
    }

    @Override
    public void clearEntries(Environment settings) {
        entityManager.getTransaction().begin();
        entityManager.createQuery(
                "DELETE FROM LogEntry e where e.matchConfig = (:matchers) ")
                .setParameter("matchers", settings.getMatchConfigs())
                .executeUpdate();
        entityManager.getTransaction().commit();
    }

    @Override
    public void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, Environment settings) {
        entityManager.getTransaction().begin();
        matcher.setWeight(getMaxWeight(settings.getMatchConfigs()) + 1);
        settings.getMatchConfigs().add(matcher);
        entityManager.merge(settings);
        if (matcher.isUseArchive()) {
            List<LogEntry> entries = entityManager.createQuery(
                    "SELECT e FROM LogEntry e where e.matchConfig = (:matcher) ORDER BY e.date DESC ",
                    LogEntry.class)
                    .setParameter("matcher", parentMatcher)
                    .getResultList();
            List<LogEntry> filtered = new ArrayList<LogEntry>();
            EntryMatcher entryMatcher = new EntryMatcher(Arrays.asList(matcher));
            for (LogEntry entry : entries) {
                if (entryMatcher.match(entry)) {
                    filtered.add(entry);
                    entityManager.remove(entry);
                }
            }
            if (!filtered.isEmpty()) {
                entityManager.merge(filtered.get(0).createCopy(filtered.size(), matcher));
            }
        }
        entityManager.getTransaction().commit();
    }

    private int getMaxWeight(List<MatchConfig> matchConfigs) {
        Collections.sort(matchConfigs);
        if (matchConfigs.isEmpty()) {
            return 0;
        }
        return matchConfigs.get(0).getWeight() == null ? 0 : matchConfigs.get(0).getWeight();
    }

    @Override
    public void addEntries(List<LogEntry> entries) {
        entityManager.getTransaction().begin();
        for (LogEntry entry : entries) {
             entityManager.merge(getEntryToMerge(entry));
        }
        entityManager.getTransaction().commit();
    }

    private LogEntry getEntryToMerge(LogEntry entry) {
        if (!entry.getMatchConfig().isUseArchive()) {
            return entry;
        }
        List<LogEntry> entries = getMatchedEntries(entry.getMatchConfig());
        if (entries.isEmpty()) {
            return entry;
        }
        LogEntry first = entries.get(0);
        first.setDate(entry.getDate());
        first.setFoundNumber(first.getFoundNumber() + 1);
        for (int i = 1; i < entries.size(); i++) {
             entityManager.remove(entries.get(i));
        }
        return first;
    }
}
