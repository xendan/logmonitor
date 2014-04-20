package org.xendan.logmonitor.idea.components;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author xendan
 * @since 4/20/14.
 */
public class LogMonitorSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll{
    private JTextField textField1;
    private JLabel webArjPath;
    private JPanel contentPanel;
    private JPanel buildInPanel;
    private JPanel externalServerSettins;
    private JButton copyPathButton;
    private JLabel configLink;
    private JTextField textField2;
    private JRadioButton useBuiltInServerRadioButton;
    private JRadioButton useExternalServerRadioButton;
    private Settings settings;

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
        settings = readSettings();
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

    private void setPanelEnabled(JPanel panel, boolean enabled) {
        for (Component component : panel.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof JPanel) {
                setPanelEnabled((JPanel) component, enabled);
            }
        }
    }
    
    private static class Settings {
        
    }
}
