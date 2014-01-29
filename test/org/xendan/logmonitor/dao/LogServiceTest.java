package org.xendan.logmonitor.dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.LogFileReader;
import org.xendan.logmonitor.read.LogFileParserTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;

import static junit.framework.TestCase.*;

/**
 * User: id967161
 * Date: 15/10/13
 */
public class LogServiceTest {

    public static final String TEST_PATH = "test_db";
    public static final String TEST_FILES = "test_files";
    private LogService service;
    private Environment environment;
    private MatchConfig matchConfig;
    private HomeResolver homeResolver;
    private Throwable failure;

    @Before
    public void setUp() {
        failure = null;
        homeResolver = new HomeResolver();
        service = new WaitingService(homeResolver);
        service.clearAll(false, new TestCallback<Void>() {
            @Override
            public void onAnswer(Void answer) {
                File directory = new File(homeResolver.getPath(TEST_FILES));
                if (directory.exists()) {
                    try {
                        FileUtils.deleteDirectory(directory);
                    } catch (IOException e) {
                        throw new IllegalStateException("Can't delete directory", e);
                    }
                }
            }
        });
        environment = new Environment();
        matchConfig = new MatchConfig();
        matchConfig.setGeneral(true);
        matchConfig.setLevel(Level.ERROR.toString());
        Configuration config = new Configuration();
        config.getEnvironments().add(environment);
        environment.getMatchConfigs().add(matchConfig);
        service.save(Arrays.asList(config), new NoExceptionCallback());
    }

    @Test
    public void test_empty_messages() throws Exception {
        List<LogEntry> entries = getJobErrorsEntries("empty_messages.log");
        assertAllHaveMessages(entries);
        service.addEntries(entries, new TestCallback<Void>(){
            @Override
            public void onAnswer(Void answer) {
                for (MatchConfig config : environment.getMatchConfigs()) {
                    service.getNotGroupedMatchedEntries(config, environment, new TestCallback<List<LogEntry>>() {
                        @Override
                        public void onAnswer(List<LogEntry> answer) {
                            assertAllHaveMessages(answer);
                        }
                    });
                }
            }
        });

    }

    private void assertAllHaveMessages(List<LogEntry> entries) {
        for (LogEntry entry : entries) {
            assertFalse("Entry " + entry + " has empty message", StringUtils.isEmpty(entry.getMessage()));
        }
    }

    @Test
    public void testAddConfig() throws Throwable {
        List<LogEntry> entries = new ArrayList<LogEntry>();
        String error1 = "this. is .a long [error]";
        for (int i = 0; i < 3; i++) {
            entries.add(createError(error1));
        }
        entries.add(createError("and[] it. is a short [error]#"));
        service.addEntries(entries, new TestCallback<Void>() {
            @Override
            public void onAnswer(Void answer) {
                checkGroupNum(1);
                checkEntriesInGroup(0, 3);
                checkNotGrouped(1);

                final MatchConfig textError = new MatchConfig();
                textError.setLevel(Level.ERROR.toString());
                textError.setMessage("\\[Error\\]");

                service.addMatchConfig(environment, textError, new TestCallback<Void>() {
                    @Override
                    public void onAnswer(Void answer) {
                        environment.getMatchConfigs().add(textError);
                        checkGroupNum(0);
                        checkNotGrouped(0);
                        service.getNotGroupedMatchedEntries(textError, environment, new TestCallback<List<LogEntry>>() {
                            @Override
                            public void onAnswer(List<LogEntry> answer) {
                                assertEquals("All errors are now for new config",
                                        4, answer.size());
                            }
                        });

                    }
                });
            }
        });
    }

    @After
    public void tearDown() throws Throwable {
        if (failure != null) {
            throw failure;
        }
    }

    private void checkNotGrouped(final int entriesNum) {
        service.getNotGroupedMatchedEntries(matchConfig, environment, new TestCallback<List<LogEntry>>() {
            @Override
            public void onAnswer(List<LogEntry> answer) {
                assertEquals(entriesNum, answer.size());
            }
        });
    }

    private void checkEntriesInGroup(final int groupIndex, final int entriesNum) {
        service.getMatchedEntryGroups(matchConfig, environment, new TestCallback<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> answer) {
                assertEquals(entriesNum, answer.get(groupIndex).getEntries().size());
            }
        });
    }

    private void checkGroupNum(final int number) {
        service.getMatchedEntryGroups(matchConfig, environment, new TestCallback<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> answer) {
                assertEquals(number, answer.size());
            }
        });
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
        service.addEntries(getJobErrorsEntries("unexpected-error.log"), new NoExceptionCallback());
        service.addEntries(getJobErrorsEntries("error_line_89.log"), new NoExceptionCallback());
    }

    @Test
    public void test_similar_start() throws Exception {
        addEntriesAndVerifyGroups("similar_start.log", new TestCallback<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> groups) {
                assertEquals("Expect single group", 1, groups.size());
                assertTrue(groups.get(0).getMessagePattern().endsWith("(.*)"));
            }
        });

    }

    private void addEntriesAndVerifyGroups(String fileName, final TestCallback<List<LogEntryGroup>> verifyGroups) throws IOException {
        List<LogEntry> entries = getJobErrorsEntries(fileName);
        service.addEntries(entries, new TestCallback<Void>() {
            @Override
            public void onAnswer(Void answer) {
                service.getMatchedEntryGroups(matchConfig, environment, verifyGroups);
            }
        });
    }

    @Test
    public void test_dvf_settings() throws Exception {
        addEntriesAndVerifyGroups("dvf-settings.log", new TestCallback<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> groups) {
                assertEquals("Expect single group", 1, groups.size());
                assertEquals(2, groups.get(0).getEntries().size());
            }
        });
    }

    @Test
    public void test_similar_end() throws Exception {
        addEntriesAndVerifyGroups("similar_end.log", new TestCallback<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> groups) {
                assertEquals("Expect single group", 1, groups.size());
                assertTrue(groups.get(0).getMessagePattern().substring(0, 10), groups.get(0).getMessagePattern().startsWith("(.*)Caused by"));
            }
        });
    }

    @Test
    public void test_general_error_grouped__full_text() throws Exception {
        String fileName = "same_errores.log";
        final List<LogEntry> entries = getJobErrorsEntries(fileName);
        for (int i = 1; i < entries.size(); i++) {
            assertEquals(entries.get(0).getMessage(), entries.get(i).getMessage());
        }
        addEntriesAndVerifyGroups(fileName, new TestCallback<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> groups) {
                assertEntriesFound(entries, groups);
            }
        });
    }

    @Test
    public void test_general_error_grouped__middle_match() throws Exception {
        String fileName = "different_internal_errores.log";
        final List<LogEntry> entries = getJobErrorsEntries(fileName);
        addEntriesAndVerifyGroups(fileName, new TestCallback<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> groups) {
                List<LogEntry> groupedEntries = groups.get(0).getEntries();
                assertEquals("123456", groupedEntries.get(0).getMessage());
                assertEquals("124356", groupedEntries.get(1).getMessage());
                assertEquals("7776WWW", groupedEntries.get(2).getMessage());
                assertEquals("3333", groupedEntries.get(3).getMessage());
                assertEquals("666", groupedEntries.get(4).getMessage());
                assertEntriesFound(entries, groups);
            }
        });

    }

    private void assertEntriesFound(List<LogEntry> entries, List<LogEntryGroup> groups) {
        assertEquals("Expect one group found", 1, groups.size());
        for (LogEntry entry : entries) {
            assertTrue(groups.get(0).getEntries().contains(entry));
        }
        assertEquals("Expect 5 entries found", 5, groups.get(0).getEntries().size());
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
        return new LogFileReader(new LocalDateTime(1980, 1, 1, 1, 1), path, LogFileParserTest.DEF_PATTERN, environment).getEntries();
    }

    private abstract class TestCallback<T> implements Callback<T> {
        @Override
        public void onFail(Throwable error) {
            failure = error;
        }
    }

    private class NoExceptionCallback extends TestCallback<Void> {
        @Override
        public void onAnswer(Void answer) {
        }
    }

    private class WaitingService extends LogService {
        public WaitingService(HomeResolver resolver) {
            super(resolver);
        }

        @Override
        protected void execute(Runnable command) {
            FutureTask<Void> task = new FutureTask<Void>(command, null);
            super.execute(task);
            try {
                task.get();
            } catch (Exception e) {
                throw new IllegalStateException("Error waiting command termination");
            }
        }

        @Override
        protected ConfigurationDaoImpl createDao() {
            return new ConfigurationDaoImpl(homeResolver, TEST_PATH);
        }
    }
}
