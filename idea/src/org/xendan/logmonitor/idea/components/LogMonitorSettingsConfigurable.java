package org.xendan.logmonitor.idea.components;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author xendan
 * @since 4/20/14.
 */
public class LogMonitorSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll{
    private JCheckBox useBuiltInServerCheckBox;
    private JTextField textField1;
    private JLabel url;
    private JLabel webArjPath;
    private JButton copyPathButton;
    private JTextField textField2;
    private JPanel contentPanel;

    @NotNull
    @Override
    public String getId() {
        return "logmonitor.settings";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Log monitor settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        //TODO check why..
        return "settings.logmonitor";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
