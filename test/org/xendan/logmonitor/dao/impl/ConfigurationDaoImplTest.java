package org.xendan.logmonitor.dao.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
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
    public static final String JOB_ERRORES_LOG = "same_errores.log";
    private String path;
    private ConfigurationDaoImpl dao;
    private Environment environment;
    private MatchConfig matchConfig;

    @Before
    public void setUp() {
        HomeResolver homeResolver = new HomeResolver();
        File directory = new File(homeResolver.getPath(TEST_PATH));
        if (directory.exists()) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                throw new IllegalStateException("Can't delete directory", e);
            }
        }
        path = homeResolver.joinMkDirs(JOB_ERRORES_LOG, TEST_PATH);
        try {
            IOUtils.copy(getClass().getResourceAsStream("same_errores.log"), new FileOutputStream(path));
        } catch (IOException e) {
            throw new IllegalStateException("Error copying file", e);
        }
        dao = new ConfigurationDaoImpl(homeResolver, TEST_PATH);
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
    public void test_general_error_grouped__full_text() throws Exception {

        List<LogEntry> entries = getJobErroresEntries(environment);
        assertEquals(entries.get(0).getMessage(), entries.get(2).getMessage());
        assertEquals(entries.get(0).getMessage(), entries.get(1).getMessage());
        assertEquals(entries.get(0).getMessage(), entries.get(3).getMessage());
        assertEquals(entries.get(0).getMessage(), entries.get(4).getMessage());

        dao.addEntries(entries);
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);

        assertEquals("Expect one group found", 1, groups.size());

        assertTrue(groups.get(0).getEntries().contains(entries.get(0)));
        assertTrue(groups.get(0).getEntries().contains(entries.get(1)));
        assertTrue(groups.get(0).getEntries().contains(entries.get(2)));
        assertTrue(groups.get(0).getEntries().contains(entries.get(3)));
        assertTrue(groups.get(0).getEntries().contains(entries.get(4)));
        assertEquals("Expect 5 entries found", 5, groups.get(0).getEntries().size());

    }

    public List<LogEntry> getJobErroresEntries(Environment environment) {
        return new LogFileReader(new LocalDateTime(1980, 1, 1, 1, 1), path, LogFileParserTest.DEF_PATTERN, environment).getEntries();
    }
}
