package org.xendan.logmonitor.idea.components;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xendan.logmonitor.idea.read.Serializer;
import org.xendan.logmonitor.idea.read.Settings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

/**
 * @author xendan
 * @since 4/20/14.
 */
public class LogMonitorSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll{
    private JLabel webArjPath;
    private JPanel contentPanel;
    private JPanel buildInPanel;
    private JPanel externalServerSettings;
    private JButton copyPathButton;
    private JLabel configLink;
    private JTextField textField2;
    private JRadioButton useBuiltInServerRadioButton;
    private JRadioButton useExternalServerRadioButton;
    private JFormattedTextField portTextField;
    private Settings settings;
    private Serializer serializer;
    private Settings.State initialState;

    public LogMonitorSettingsConfigurable(Settings settings, Serializer serializer) {
        this.settings = settings;
        this.serializer = serializer;
        init();
    }

    private void init() {
        ButtonGroup group = new ButtonGroup();
        group.add(useBuiltInServerRadioButton);
        group.add(useExternalServerRadioButton);
        setState(settings.getState());
    }

    private void setState(Settings.State state) {
        initState();
        boolean isBuiltIn = Boolean.TRUE.equals(state.getUseBuiltInServer());
        setPanelEnabled(buildInPanel, isBuiltIn);
        boolean isExternal = Boolean.FALSE.equals(state.getUseBuiltInServer());
        setPanelEnabled(externalServerSettings, isExternal);
        useBuiltInServerRadioButton.setSelected(isBuiltIn);
        useExternalServerRadioButton.setSelected(isExternal);
        ActionListener internalExternalListener = new InternalExternalListener();
        useBuiltInServerRadioButton.addActionListener(internalExternalListener);
        useExternalServerRadioButton.addActionListener(internalExternalListener);
        portTextField.setValue(state.getPort());
        portTextField.getDocument().addDocumentListener(new PortSynchroniser());
    }

    private void initState() {
        initialState = serializer.doCopy(settings.getState());
    }

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
        return !initialState.equals(settings.getState());
    }

    @Override
    public void apply() throws ConfigurationException {
        initState();
    }

    @Override
    public void reset() {
        settings.reset(initialState);
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

    private void createUIComponents() {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        portTextField = new JFormattedTextField(format);
    }

    private class PortSynchroniser extends DocumentAdapter {

        @Override
        protected void textChanged(DocumentEvent documentEvent) {
            settings.getState().setPort((Integer) portTextField.getValue());
        }
    }

    private class InternalExternalListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            boolean builtInServer = event.getSource() == useBuiltInServerRadioButton;
            settings.getState().setUseBuiltInServer(builtInServer);
            setPanelEnabled(buildInPanel, builtInServer);
            setPanelEnabled(externalServerSettings, !builtInServer);
        }
    }
}
