package org.xendan.logmonitor.idea;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import org.junit.Test;
import org.xendan.logmonitor.model.Environment;

import java.util.ArrayList;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * User: id967161
 * Date: 18/09/13
 */
public class MatchConfigFormTest {
    @Test
    public void test_apply() throws Exception {
        MatchConfigForm form = new MatchConfigForm();

        ArrayList<Environment> settings = new ArrayList<Environment>();
        settings.add(new Environment());
        ValueModel settingsValue = new ValueHolder(settings);
        form.setLogSettingsList(settingsValue);

        assertFalse("Expect for single settings apply not visible", form.applyForLabel.isVisible());
        settings = new ArrayList<Environment>();
        settings.add(new Environment());
        settings.add(new Environment());

        settingsValue.setValue(settings);
        assertTrue("Expect for two settings apply visible", form.applyForLabel.isVisible());
    }
}
