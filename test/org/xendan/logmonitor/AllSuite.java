package org.xendan.logmonitor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.xendan.logmonitor.parser.DateParserTest;
import org.xendan.logmonitor.parser.EntryMatcherTest;
import org.xendan.logmonitor.parser.LevelParserTest;
import org.xendan.logmonitor.parser.LogParserTest;
import org.xendan.logmonitor.read.LsCommandTest;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DateParserTest.class,
        EntryMatcherTest.class,
        LevelParserTest.class,
        LogParserTest.class,
        LsCommandTest.class
})
public class AllSuite {
}