package org.xendan.logmonitor.idea;

import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.LogSettings;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: id967161
 * Date: 16/09/13
 */
public class LogMonitorSettingsConfigurableTest {

    private LogMonitorSettingsConfigurable configurable;

    @Test
    public void test_selected_config() throws Exception {
        configurable.refresh();
        assertEquals("Expect existing and new project", 2, configurable.projectComboBox.getModel().getSize());
        assertNull(((LogMonitorConfiguration) configurable.projectComboBox.getSelectedItem()).getId());
    }

    @Test
    public void test_add_new() throws Exception {
        configurable.refresh();
        assertEquals("Expect add new and localhost", 2, configurable.serverComboBox.getModel().getSize());

        configurable.logSettingsModel.newButton.doClick();
        assertNotNull(((LogSettings) configurable.logSettingsList.getModel().getElementAt(0)).getServer());
        assertEquals(1, configurable.logSettingsList.getModel().getSize());
        assertEquals(1, configurable.configAdapter.getBean().getLogSettings().size());

        //add valid data
        configurable.logSettingsNametextField.setText("Log Settings 1");
        configurable.pathTextField.setText("somethere");

        configurable.serverHostTextField.setText("new host");
        configurable.logSettingsNametextField.setText("name 1");
        configurable.pathTextField.setText("aaa");

        //commit
        configurable.logSettingsModel.newButton.doClick();
        assertEquals("Expect new server added", 3, configurable.serverComboBox.getModel().getSize());
    }

    @Test
    public void test_not_added_empty() throws Exception {
        configurable.logSettingsModel.newButton.doClick();
        configurable.logSettingsModel.newButton.doClick();
        assertEquals(1, configurable.logSettingsList.getModel().getSize());
    }

    @Before
    public void setUp() {
        Project project = mock(Project.class);
        when(project.getName()).thenReturn("Test project");
        LogMonitorSettingsDao logMonitorSettingsDao = mock(LogMonitorSettingsDao.class);
        LogMonitorConfiguration logMonitorConfiguration = new LogMonitorConfiguration();
        logMonitorConfiguration.setProjectName("AAA");
        when(logMonitorSettingsDao.getConfigs()).thenReturn(new ArrayList<LogMonitorConfiguration>(Arrays.asList(logMonitorConfiguration)));
        ReaderScheduler readerScheduler = mock(ReaderScheduler.class);
        configurable = new LogMonitorSettingsConfigurable(project, logMonitorSettingsDao, new Serializer(new HomeResolver()), readerScheduler);
    }
}
