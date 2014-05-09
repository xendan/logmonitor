package org.xendan.logmonitor.web.service;

import org.apache.tools.ant.BuildException;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import java.util.List;

/**
 * @author mullomuk
 * @since 5/8/2014.
 */
public interface LogServicePartial {

    void addEntries(List<LogEntry> entries);

    void removeMatchConfig(final MatchConfig match, final Environment environment);

    void addMatchConfig(final Environment environment, final MatchConfig newConfig);

}
