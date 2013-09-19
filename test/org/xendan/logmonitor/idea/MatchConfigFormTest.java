package org.xendan.logmonitor.idea;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import org.junit.Test;
import org.xendan.logmonitor.model.LogSettings;

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

        ArrayList<LogSettings> settings = new ArrayList<LogSettings>();
        settings.add(new LogSettings());
        ValueModel settingsValue = new ValueHolder(settings);
        form.setLogSettingsList(settingsValue);

        assertFalse("Expect for single settings apply not visible", form.applyForLabel.isVisible());
        settings = new ArrayList<LogSettings>();
        settings.add(new LogSettings());
        settings.add(new LogSettings());

        settingsValue.setValue(settings);
        assertTrue("Expect for two settings apply visible", form.applyForLabel.isVisible());
    }
}
