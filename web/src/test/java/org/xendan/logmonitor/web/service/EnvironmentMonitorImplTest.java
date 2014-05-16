package org.xendan.logmonitor.web.service;

import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.model.Server;

import static org.junit.Assert.assertTrue;

public class EnvironmentMonitorImplTest {

    private Environment environment;

    @Before
    public void setUp() throws Exception {
        environment = new Environment();
        environment.setId(5L);
        environment.setUpdateInterval(3);
    }

    @Test
    public void testNextRegularUpdateOnLocal() throws Exception {
        EnvironmentMonitorImpl monitor = new EnvironmentMonitorImpl();
        monitor.setEnvironmentStatus(environment, EnvironmentMessage.PARSING);
        monitor.setEnvironmentStatus(environment, EnvironmentMessage.WAITING);
        EnvironmentStatus status = monitor.getStatus(environment.getId());
        assertUpdateSoon(status);
    }

    @Test
    public void testNextRegularUpdateOnServer() throws Exception {
        environment.setServer(new Server());
        EnvironmentMonitorImpl monitor = new EnvironmentMonitorImpl();
        monitor.setEnvironmentStatus(environment, EnvironmentMessage.DOWNLOADING);
        monitor.setEnvironmentStatus(environment, EnvironmentMessage.WAITING);
        EnvironmentStatus status = monitor.getStatus(environment.getId());
        assertUpdateSoon(status);
    }

    @Test
    public void testNextRegularUpdateOnError() throws Exception {
        environment.setServer(new Server());
        EnvironmentMonitorImpl monitor = new EnvironmentMonitorImpl();
        monitor.setEnvironmentStatus(environment, EnvironmentMessage.DOWNLOADING);
        monitor.setEnvironmentStatus(environment, EnvironmentMessage.ERROR_DOWNLOADING);
        EnvironmentStatus status = monitor.getStatus(environment.getId());
        assertUpdateSoon(status);
    }

    private void assertUpdateSoon(EnvironmentStatus status) {
        long delta = environment.getUpdateInterval() * 60 * 1000 - status.getUpdateInterval();
        assertTrue("Next update after parsing delta -" + delta, delta < 100);
    }

}
