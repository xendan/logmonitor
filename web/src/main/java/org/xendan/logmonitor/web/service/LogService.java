package org.xendan.logmonitor.web.service;

import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.web.dao.ConfigurationDao;

import java.util.List;

/**
 * @author mullomuk
 * @since 5/8/2014.
 */
public interface LogService extends ConfigurationDao, LogServicePartial {

}
