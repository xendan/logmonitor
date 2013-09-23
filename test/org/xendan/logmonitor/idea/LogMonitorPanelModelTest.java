package org.xendan.logmonitor.idea;

import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.service.LogService;
import static org.mockito.Mockito.mock;

/**
 * User: id967161
 * Date: 20/09/13
 */
public class LogMonitorPanelModelTest {

    private LogMonitorPanelModel model;

    @Test
    public void test_no_config() throws Exception {

    }

    @Before
    public void setUp() {
        LogService service = mock(LogService.class);
        model = new LogMonitorPanelModel(service);
    }
}
