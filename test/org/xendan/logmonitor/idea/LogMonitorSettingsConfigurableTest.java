package org.xendan.logmonitor.idea;

import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.LogSettings;
import org.xendan.logmonitor.model.Server;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: id967161
 * Date: 16/09/13
 */
public class LogMonitorSettingsConfigurableTest {

    private LogMonitorSettingsConfigurable configurable;
    private LogMonitorConfiguration config;
    private Server server;
    private LogSettings local;
    private LogSettings notLocal;

    @Test
    public void test_selected_config() throws Exception {
        configurable.refresh();
        assertEquals("Expect existing and new project", 2, configurable.projectComboBox.getModel().getSize());
        assertNull(((LogMonitorConfiguration) configurable.projectComboBox.getSelectedItem()).getId());
    }

    @Test
    public void test_remove_button() throws Exception {
        setSomeLogSettings();
        configurable.refresh();
        configurable.projectComboBox.setSelectedItem(config);

        assertFalse(configurable.logSettingsModel.removeButton.isEnabled());
        configurable.logSettingsList.setSelectedIndex(1);

        assertTrue(configurable.logSettingsModel.removeButton.isEnabled());
    }

    @Test
    public void test_add_new() throws Exception {
        configurable.refresh();
        assertEquals("Expect add new and localhost", 2, configurable.serverComboBox.getModel().getSize());

        configurable.logSettingsModel.newButton.doClick();
        assertNotNull(((LogSettings) configurable.logSettingsList.getModel().getElementAt(0)).getServer());
        assertNull(((LogSettings) configurable.logSettingsList.getModel().getElementAt(0)).getServer().getId());
        assertEquals(1, configurable.logSettingsList.getModel().getSize());
        assertEquals(1, configurable.configAdapter.getBean().getLogSettings().size());
        assertTrue("Expect on new name is enabled", configurable.logSettingsNametextField.isEnabled());
        assertEquals(0, configurable.logSettingsList.getSelectedIndex());

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
    public void test_server_is_selected() throws Exception {
        setSomeLogSettings();
        configurable.refresh();
        configurable.projectComboBox.setSelectedItem(config);

        assertEquals("Expect two log setting ", 2, configurable.logSettingsModel.itemsList.getModel().getSize());

        configurable.logSettingsModel.itemsList.setSelectedValue(local, false);

        assertEquals("Expect local host selected", LogMonitorSettingsConfigurable.LOCALHOST, configurable.serverComboBox.getSelectedItem());
        assertFalse("For local host server panel is disabled", configurable.serverHostTextField.isEnabled());

        configurable.logSettingsModel.itemsList.setSelectedValue(notLocal, false);

        assertTrue("For server panel is enabled for not localhost", configurable.serverHostTextField.isEnabled());

        assertEquals("Expect sever host selected", server,  configurable.serverComboBox.getSelectedItem());
        assertEquals("Expect sever host selected", server.getHost(), configurable.serverHostTextField.getText());
    }

    private void setSomeLogSettings() {
        local = createValidSettigns();
        local.setName("LOCAL");
        notLocal = createValidSettigns();
        server = new Server();
        server.setHost("some host");
        notLocal.setServer(server);
        config.setLogSettings(Arrays.asList(local, notLocal));
    }

    private LogSettings createValidSettigns() {
        LogSettings settings = new LogSettings();
        settings.setName("some name");
        settings.setPath("some path");
        return settings;
    }

    @Test
    public void test_not_added_empty() throws Exception {
        configurable.logSettingsModel.newButton.doClick();
        configurable.logSettingsModel.newButton.doClick();
        assertEquals(1, configurable.logSettingsList.getModel().getSize());
    }

    @Test
    public void test_patterns_disabled() throws Exception {
        setSomeLogSettings();
        configurable.refresh();
        configurable.projectComboBox.setSelectedItem(config);

        assertFalse(configurable.paternsList.isEnabled());
        configurable.logSettingsModel.itemsList.setSelectedValue(local, false);
        assertTrue("Some log settings is selected", configurable.paternsList.isEnabled());
    }

    @Before
    public void setUp() {
        Project project = mock(Project.class);
        when(project.getName()).thenReturn("Test project");
        LogMonitorSettingsDao logMonitorSettingsDao = mock(LogMonitorSettingsDao.class);
        config = new LogMonitorConfiguration();
        config.setProjectName("AAA");
        when(logMonitorSettingsDao.getConfigs()).thenReturn(new ArrayList<LogMonitorConfiguration>(Arrays.asList(config)));
        ReaderScheduler readerScheduler = mock(ReaderScheduler.class);
        configurable = new LogMonitorSettingsConfigurable(project, logMonitorSettingsDao, new Serializer(new HomeResolver()), readerScheduler);
    }
}
