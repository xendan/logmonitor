package org.xendan.logmonitor.web.service;

import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.guice.GuiceServletConfig;
import org.xendan.logmonitor.web.read.parse.LogFileReader;
import org.xendan.logmonitor.web.read.parse.LogFileReaderTest;
import org.xendan.logmonitor.web.read.parse.PatternUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class LogServiceImplTest {

    public static final String TEST_PATH = "test_db";
    public static final String TEST_FILES = "test_files";
    private LogServicePartial service;
    private Environment environment;
    private MatchConfig matchConfig;
    private HomeResolver homeResolver;
    private ConfigurationDao dao;

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
        createDao();
        dao.clearAll();
        createDao();
        for (Configuration config : createConfigs()) {
            dao.persist(config);
        }
        service = new LogServiceImpl(dao);
    }

    private void createDao() {
        Injector injector = GuiceServletConfig.createInjector(TEST_PATH);
        injector.getInstance(PersistService.class).start();
        dao = injector.getInstance(ConfigurationDao.class);
    }

    private MatchConfig createMatchConfig() {
        matchConfig = new MatchConfig();
        matchConfig.setGeneral(true);
        matchConfig.setLevel(Level.ERROR.toString());
        return matchConfig;
    }

    private List<Configuration> createConfigs() {
        Configuration config = new Configuration();
        config.getEnvironments().add(createEnvironment());
        return Arrays.asList(config);
    }

    private Environment createEnvironment() {
        environment = new Environment();
        environment.getMatchConfigs().add(createMatchConfig());
        return environment;
    }

    @Test
    public void test_empty_messages() throws Exception {
        List<LogEntry> entries = getJobErrorsEntries("empty_messages.log");
        assertAllHaveMessages(entries);
        service.addEntries(entries);
        for (MatchConfig config : environment.getMatchConfigs())  {
            assertAllHaveMessages(dao.getNotGroupedMatchedEntries(config.getId(), environment.getId()));
        }
    }

    private void assertAllHaveMessages(List<LogEntry> entries) {
        for (LogEntry entry : entries) {
            assertFalse("Entry " + entry + " has empty message", StringUtils.isEmpty(entry.getMessage()));
        }
    }

    @Test
    public void testAddMatchConfig() throws Throwable {
        List<LogEntry> entries = new ArrayList<LogEntry>();
        String error1 = "this. is .a long [error]";
        for (int i = 0; i < 3; i++) {
            entries.add(createError(error1));
        }
        entries.add(createError("and[] it. is a short [error]#"));
        service.addEntries(entries);
        checkGroupNum(1);
        checkEntriesInGroup(0, 3);
        checkNotGrouped(1);

        final MatchConfig textError = new MatchConfig();
        textError.setLevel(Level.ERROR.toString());
        textError.setMessage("\\[Error\\]");

        service.addMatchConfig(environment, textError);
        environment.getMatchConfigs().add(textError);
        checkGroupNum(0);
        checkNotGrouped(0);
        entries = dao.getNotGroupedMatchedEntries(textError.getId(), environment.getId());
        assertEquals("All errors are now for new config",
                4, entries.size());

    }

    private void checkNotGrouped(final int entriesNum) {
        List<LogEntry> answer = dao.getNotGroupedMatchedEntries(matchConfig.getId(), environment.getId());
        assertEquals(entriesNum, answer.size());
    }

    private void checkEntriesInGroup(final int groupIndex, final int entriesNum) {
        List<LogEntryGroup> answer = dao.getMatchedEntryGroups(matchConfig.getId(), environment.getId());
        assertEquals(entriesNum, answer.get(groupIndex).getEntries().size());
    }

    private void checkGroupNum(final int number) {
        List<LogEntryGroup> answer = dao.getMatchedEntryGroups(matchConfig.getId(), environment.getId());
        assertEquals(number, answer.size());
    }

    private LogEntry createError(String message) {
        LogEntry entry = new LogEntry();
        entry.setLevel(Level.ERROR.toString());
        entry.setDate(new LocalDateTime());
        entry.setMessage(message);
        entry.setEnvironment(environment);
        entry.setMatchConfig(matchConfig);
        return entry;
    }

    @Test
    public void test_misc_error() throws Exception {
        service.addEntries(getJobErrorsEntries("unexpected-error.log"));
        service.addEntries(getJobErrorsEntries("error_line_89.log"));
    }

    @Test
    public void test_similar_start() throws Exception {
        List<LogEntryGroup> groups = addEntriesAndVerifyGroups("similar_start.log");
        assertEquals("Expect single group", 1, groups.size());
        assertTrue(groups.get(0).getMessagePattern().endsWith("(.*)"));

    }

    private List<LogEntryGroup> addEntriesAndVerifyGroups(String fileName) throws IOException {
        final List<LogEntry> entries = getJobErrorsEntries(fileName);
        service.addEntries(entries);
        return dao.getMatchedEntryGroups(matchConfig.getId(), environment.getId());
    }

    @Test
    public void test_dvf_settings() throws Exception {
        List<LogEntryGroup> groups = addEntriesAndVerifyGroups("dvf-settings.log");
        assertEquals("Expect single group", 1, groups.size());
        assertEquals(2, groups.get(0).getEntries().size());
    }

    @Test
    public void test_similar_end() throws Exception {
        List<LogEntryGroup> groups = addEntriesAndVerifyGroups("similar_end.log");
        assertEquals("Expect single group", 1, groups.size());
        assertTrue(groups.get(0).getMessagePattern().substring(0, 10), groups.get(0).getMessagePattern().startsWith("(.*)Caused by"));
    }

    @Test
    public void test_general_error_grouped__full_text() throws Exception {
        String fileName = "same_errores.log";
        final List<LogEntry> entries = getJobErrorsEntries(fileName);
        for (int i = 1; i < entries.size(); i++) {
            assertEquals(entries.get(0).getMessage(), entries.get(i).getMessage());
        }
        List<LogEntryGroup> groups = addEntriesAndVerifyGroups(fileName);
        assertEntriesFound(entries, groups);
    }

    @Test
    public void test_general_error_grouped__middle_match() throws Exception {
        String fileName = "different_internal_errores.log";
        final List<LogEntry> entries = getJobErrorsEntries(fileName);
        List<LogEntryGroup> groups = addEntriesAndVerifyGroups(fileName);
        containsMessages(groups.get(0).getEntries(), "123456", "124356", "7776WWW", "3333", "666");
        assertEntriesFound(entries, groups);

    }

    private void containsMessages(List<LogEntry> entries, String... messages) {
        for (LogEntry entry : entries) {
            assertHasOneOfMessage(entry, messages);
        }
    }

    private void assertHasOneOfMessage(LogEntry entry, String[] messages) {
        for (String message : messages) {
            if (message.equals(entry.getMessage())) {
                return;
            }
        }
        fail("Message " + entry.getMessage() + " not found in " + Arrays.toString(messages));
    }

    private void assertEntriesFound(List<LogEntry> entries, List<LogEntryGroup> groups) {
        assertEquals("Expect one group found", 1, groups.size());
        for (LogEntry entry : entries) {
            assertTrue(entriesContains(groups.get(0), entry));
        }
        assertEquals("Expect 5 entries found", 5, groups.get(0).getEntries().size());
    }

    private boolean entriesContains(LogEntryGroup group, LogEntry entry) {
        for (LogEntry otherEntry : group.getEntries()) {
            if (nullOrEquals(otherEntry.getCaller(), entry.getCaller()) &&
                    nullOrEquals(otherEntry.getEnvironment(), entry.getEnvironment()) &&
                    nullOrEquals(otherEntry.getCategory(), entry.getCategory()) &&
                    nullOrEquals(otherEntry.getFoundNumber(), entry.getFoundNumber()) &&
                    nullOrEquals(otherEntry.getLevel(), entry.getLevel()) &&
                    nullOrEquals(otherEntry.getMatchConfig(), entry.getMatchConfig()) &&
                    nullOrEquals(PatternUtils.restoreMessage(otherEntry, group.getMessagePattern()), entry.getMessage()) &&
                    nullOrEquals(otherEntry.getDate(), entry.getDate())) {
                return true;
            }
        }
        return false;
    }

    private boolean nullOrEquals(Object something, Object other) {
        if (something == null) {
            return other == null;
        }
        return something.equals(other);
    }


    public List<LogEntry> getJobErrorsEntries(String fileName) throws IOException {
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
        environment.setLastUpdate(new LocalDateTime(1980, 1, 1, 1, 1));
        return new LogFileReader(path, LogFileReaderTest.DEF_PATTERN, environment).getEntries();
    }


}
