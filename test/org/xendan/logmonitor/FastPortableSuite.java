package org.xendan.logmonitor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.xendan.logmonitor.parser.LogParserTest;
import org.xendan.logmonitor.read.LogReaderTest;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LogParserTest.class,
    LogReaderTest.class
})
public class FastPortableSuite {
}
