package org.xendan.logmonitor.dao.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.LogFileReader;
import org.xendan.logmonitor.read.LogFileParserTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * User: id967161
 * Date: 15/10/13
 */
public class ConfigurationDaoImplTest {

    public static final String TEST_PATH = "test_db";
    public static final String TEST_FILES = "test_files";
    private ConfigurationDao dao;
    private Environment environment;
    private MatchConfig matchConfig;
    private HomeResolver homeResolver;

    @Before
    public void setUp() {
        homeResolver = new HomeResolver();
        File directory = new File(homeResolver.getPath(TEST_FILES));
        if (directory.exists()) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                throw new IllegalStateException("Can't delete directory", e);
            }
        }
        TestConfigurationDaoImpl daoImpl = new TestConfigurationDaoImpl(homeResolver, TEST_PATH);
        daoImpl.clearAll();
        //new to recreate droped tables
        dao =  new TestConfigurationDaoImpl(homeResolver, TEST_PATH);
        environment = new Environment();
        matchConfig = new MatchConfig();
        matchConfig.setGeneral(true);
        matchConfig.setLevel(Level.ERROR.toString());
        Configuration config = new Configuration();
        config.getEnvironments().add(environment);
        environment.getMatchConfigs().add(matchConfig);

        dao.save(Arrays.asList(config));
    }

    @Test
    public void test_similar_start() throws Exception {
        List<LogEntry> entries = getJobErroresEntries("similar_start.log");
        dao.addEntries(entries);
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);
        assertEquals("Expect single group", 1, groups.size());
        assertTrue(groups.get(0).getMessagePattern().endsWith("(.*)"));
    }

    @Test
    public void test_similar_end() throws Exception {
        List<LogEntry> entries = getJobErroresEntries("similar_end.log");
        dao.addEntries(entries);
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);
        assertEquals("Expect single group", 1, groups.size());
        assertTrue(groups.get(0).getMessagePattern().substring(0, 10), groups.get(0).getMessagePattern().startsWith("(.*)Caused by"));
    }

    @Test
    public void test_general_error_grouped__full_text() throws Exception {
        List<LogEntry> entries = getJobErroresEntries("same_errores.log");
        for (int i = 1; i < entries.size(); i++) {
            assertEquals(entries.get(0).getMessage(), entries.get(i).getMessage());
        }
        dao.addEntries(entries);
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);
        assertEntriesFound(entries, groups);
    }

    @Test
    public void test_general_error_grouped__middle_match() throws Exception {
        List<LogEntry> entries = getJobErroresEntries("different_internal_errores.log");
        dao.addEntries(entries);
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);
        List<LogEntry> groupedEntries = groups.get(0).getEntries();
        assertEquals("123456", groupedEntries.get(0).getMessage());
        assertEquals("124356", groupedEntries.get(1).getMessage());
        assertEquals("7776WWW", groupedEntries.get(2).getMessage());
        assertEquals("3333", groupedEntries.get(3).getMessage());
        assertEquals("666", groupedEntries.get(4).getMessage());
        assertEntriesFound(entries, groups);
    }

    private void assertEntriesFound(List<LogEntry> entries, List<LogEntryGroup> groups) {
        assertEquals("Expect one group found", 1, groups.size());
        for (LogEntry entry : entries) {
            assertTrue(groups.get(0).getEntries().contains(entry));
        }
        assertEquals("Expect 5 entries found", 5, groups.get(0).getEntries().size());
    }

    public List<LogEntry> getJobErroresEntries(String fileName) throws IOException {
        String path = homeResolver.joinMkDirs(fileName, TEST_FILES);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(path);
            IOUtils.copy(getClass().getResourceAsStream(fileName), output);
        } catch (IOException e) {
            throw new IllegalStateException("Error copying file", e);
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return new LogFileReader(new LocalDateTime(1980, 1, 1, 1, 1), path, LogFileParserTest.DEF_PATTERN, environment).getEntries();
    }

    private class TestConfigurationDaoImpl extends ConfigurationDaoImpl {
        public TestConfigurationDaoImpl(HomeResolver homeResolver, String testPath) {
            super(homeResolver, testPath);
        }

        public void clearAll() {
            entityManager.getTransaction().begin();
            entityManager.createNativeQuery("DROP ALL OBJECTS ").executeUpdate();
            entityManager.getTransaction().commit();
        }
    }
}
