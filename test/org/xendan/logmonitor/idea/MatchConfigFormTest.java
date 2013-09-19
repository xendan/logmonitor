package org.xendan.logmonitor.idea;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import org.junit.Test;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.*;
import java.util.ArrayList;

import static junit.framework.TestCase.*;

/**
 * User: id967161
 * Date: 18/09/13
 */
public class MatchConfigFormTest {

    @Test
    public void test_added_to_environemnts() throws Exception {
        MatchConfig config = new MatchConfig();
        ArrayList<Environment> environments = new ArrayList<Environment>();
        Environment env1 = new Environment();
        env1.getMatchConfigs().add(config);
        Environment env2 = new Environment();
        environments.add(env1);
        environments.add(env2);

        MatchConfigForm form = new MatchConfigForm();
        form.setBeanAdapters(new VerboseBeanAdapter<MatchConfig>(config));
        form.setEnvironments(new ValueHolder(environments));

        //initial
        assertEquals("Expect 2 checkbox for 2 environments", 2, form.applyFor.getComponents().length);
        JCheckBox checkbox1 = (JCheckBox) form.applyFor.getComponent(0);
        assertTrue("Config is present for first env", checkbox1.isSelected());
        JCheckBox checkbox2 = (JCheckBox) form.applyFor.getComponent(1);
        assertFalse("Config is not present for second env", checkbox2.isSelected());

        //add
        checkbox2.setSelected(true);
        assertEquals(config, env2.getMatchConfigs().get(0));
        //remove
        checkbox1.setSelected(false);
        assertTrue(env1.getMatchConfigs().isEmpty());
    }

    @Test
    public void test_apply_visibility() throws Exception {
        MatchConfigForm form = new MatchConfigForm();

        ArrayList<Environment> settings = new ArrayList<Environment>();
        settings.add(new Environment());
        ValueModel settingsValue = new ValueHolder(settings);
        form.setEnvironments(settingsValue);

        assertFalse("Expect for single settings apply not visible", form.applyForLabel.isVisible());
        settings = new ArrayList<Environment>();
        settings.add(new Environment());
        settings.add(new Environment());

        settingsValue.setValue(settings);
        assertTrue("Expect for two settings apply visible", form.applyForLabel.isVisible());
    }
}
