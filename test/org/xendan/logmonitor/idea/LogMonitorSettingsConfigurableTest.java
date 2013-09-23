package org.xendan.logmonitor.idea;

import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.model.Server;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;
import org.xendan.logmonitor.service.LogService;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: id967161
 * Date: 16/09/13
 */
public class LogMonitorSettingsConfigurableTest {

    private LogMonitorSettingsConfigurable configurable;
    private Configuration config;
    private Server server;
    private Environment local;
    private Environment notLocal;

    @Test
    public void test_projects() throws Exception {
        configurable.reset();
        //add
        assertEquals("New project", configurable.addProjectButton.getText());
        configurable.addProjectButton.doClick();

        assertEquals("for AAA and for current project", 2, configurable.projectComboBox.getModel().getSize());
        assertEquals(configurable.addProjectButton.getText(), "Add project");
        String newName = "This is new";
        configurable.projectNameTextField.setText(newName);
        configurable.addProjectButton.doClick();
        assertEquals("Expect new added", 3, configurable.projectComboBox.getModel().getSize());
        assertEquals("New project is selected", newName, ((Configuration)configurable.projectComboBox.getSelectedItem()).getProjectName());
        assertFalse("hidden again", configurable.projectNameTextField.isVisible());
        assertEquals(configurable.addProjectButton.getText(), "New project");
        //remove
        configurable.projectRemoveButton.doClick();
        assertEquals("for AAA and for current project", 2, configurable.projectComboBox.getModel().getSize());
    }

    @Test
    public void test_new_contains_error_pattern() throws Exception {
        setSomeEnvironments();
        local.setMatchConfigs(Arrays.asList(new MatchConfig()));
        configurable.reset();
        configurable.projectComboBox.setSelectedItem(config);

        configurable.addLogSettingsButton.doClick();
        Environment newEnvironment = (Environment) configurable.environmentsList.getSelectedValue();
        assertEquals("Expect contains from local and error", 2, newEnvironment.getMatchConfigs().size());
    }

    @Test
    public void test_selected_config() throws Exception {
        configurable.reset();
        assertEquals("Expect existing and new project", 2, configurable.projectComboBox.getModel().getSize());
        assertNull(((Configuration) configurable.projectComboBox.getSelectedItem()).getId());
    }

    @Test
    public void test_empty_and_localhost_to_null() throws Exception {
        setSomeEnvironments();
        configurable.reset();
        configurable.projectComboBox.setSelectedItem(config);
        assertLocalhost("localhost", 2);
        assertLocalhost("", 3);
        assertLocalhost("127.0.0.1", 4);
    }

    private void assertLocalhost(String localhost, int index) {
        configurable.addLogSettingsButton.doClick();
        configurable.serverHostTextField.setText(localhost);
        configurable.pathTextField.setText("aaa");
        configurable.logSettingsNametextField.setText("bb");
        configurable.environmentsList.setSelectedIndex(1);
        Server server1 = ((Configuration) configurable.projectComboBox.getSelectedItem()).getEnvironments().get(index).getServer();
        assertNull("Expect " + localhost + " is localhost found "  +((server1 == null) ? "" : (server1.getHost())) , server1);
    }

    @Test
    public void test_remove_button() throws Exception {
        setSomeEnvironments();
        configurable.reset();
        configurable.projectComboBox.setSelectedItem(config);

        assertFalse(configurable.removeLogSettingsButton.isEnabled());
        configurable.environmentsList.setSelectedIndex(1);

        assertTrue(configurable.removeLogSettingsButton.isEnabled());
    }

    @Test
    public void test_add_new() throws Exception {
        configurable.reset();
        assertEquals("Expect add new and localhost", 2, configurable.serverComboBox.getModel().getSize());

        configurable.addLogSettingsButton.doClick();
        assertNotNull(((Environment) configurable.environmentsList.getModel().getElementAt(0)).getServer());
        assertNull(((Environment) configurable.environmentsList.getModel().getElementAt(0)).getServer().getId());
        assertEquals(1, configurable.environmentsList.getModel().getSize());
        assertEquals(1, configurable.configAdapter.getBean().getEnvironments().size());
        assertTrue("Expect on new name is enabled", configurable.logSettingsNametextField.isEnabled());
        assertEquals(0, configurable.environmentsList.getSelectedIndex());

        //add valid data
        configurable.logSettingsNametextField.setText("Log Settings 1");
        configurable.pathTextField.setText("somethere");

        configurable.serverHostTextField.setText("new host");
        configurable.logSettingsNametextField.setText("name 1");
        configurable.pathTextField.setText("aaa");

        //commit
        configurable.addLogSettingsButton.doClick();
        assertEquals("Expect new server added", 3, configurable.serverComboBox.getModel().getSize());
    }

    @Test
    public void test_server_is_selected() throws Exception {
        setSomeEnvironments();
        configurable.reset();
        configurable.projectComboBox.setSelectedItem(config);

        assertEquals("Expect two log setting ", 2, configurable.environmentsList.getModel().getSize());

        configurable.environmentsList.setSelectedValue(local, false);

        assertEquals("Expect local host selected", LogMonitorSettingsConfigurable.LOCALHOST, configurable.serverComboBox.getSelectedItem());
        assertFalse("For local host server panel is disabled", configurable.serverHostTextField.isEnabled());

        configurable.environmentsList.setSelectedValue(notLocal, false);

        assertTrue("For server panel is enabled for not localhost", configurable.serverHostTextField.isEnabled());

        assertEquals("Expect sever host selected", server,  configurable.serverComboBox.getSelectedItem());
        assertEquals("Expect sever host selected", server.getHost(), configurable.serverHostTextField.getText());
    }

    private void setSomeEnvironments() {
        local = createValidSettigns();
        local.setName("LOCAL");
        notLocal = createValidSettigns();
        server = new Server();
        server.setHost("some host");
        notLocal.setServer(server);
        config.setEnvironments(Arrays.asList(local, notLocal));
    }

    private Environment createValidSettigns() {
        Environment settings = new Environment();
        settings.setName("some name");
        settings.setPath("some path");
        return settings;
    }

    @Test
    public void test_not_added_empty() throws Exception {
        configurable.addLogSettingsButton.doClick();
        configurable.addLogSettingsButton.doClick();
        assertEquals(1, configurable.environmentsList.getModel().getSize());
    }

    @Test
    public void test_patterns_disabled() throws Exception {
        setSomeEnvironments();
        configurable.reset();
        configurable.projectComboBox.setSelectedItem(config);

        assertFalse(configurable.paternsList.isEnabled());
        configurable.environmentsList.setSelectedValue(local, false);
        assertTrue("Some log settings is selected", configurable.paternsList.isEnabled());
    }

    @Test
    public void test_new_environment_has_all_patterns() throws Exception {
        setSomeEnvironments();
        local.getMatchConfigs().add(createMatch(1));
        local.getMatchConfigs().add(createMatch(2));
        notLocal.getMatchConfigs().add(createMatch(3));
        configurable.reset();
        configurable.projectComboBox.setSelectedItem(config);

        configurable.addLogSettingsButton.doClick();
        assertEquals("Expect all available matchcs + new default copied to new enviromnent",
                4, ((Environment) configurable.environmentsList.getSelectedValue()).getMatchConfigs().size());
    }

    private MatchConfig createMatch(int id) {
        MatchConfig match = new MatchConfig();
        match.setId((long) id);
        match.setName(String.valueOf(id));
        match.setMessage(String.valueOf(id));
        return match;
    }

    @Before
    public void setUp() {
        Project project = mock(Project.class);
        when(project.getName()).thenReturn("Test project");
        LogService service = mock(LogService.class);
        config = new Configuration();
        config.setProjectName("AAA");
        when(service.getConfigs()).thenReturn(new ArrayList<Configuration>(Arrays.asList(config)));
        ReaderScheduler readerScheduler = mock(ReaderScheduler.class);
        configurable = new LogMonitorSettingsConfigurable(project, service, new Serializer(new HomeResolver()), readerScheduler);
    }
}
