package org.xendan.logmonitor.idea;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.LogSettings;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: id967161
 * Date: 16/09/13
 */
public class MatchConfigForm {
    private JTextField nameTextField;
    private JComboBox levelComboBox;
    private JLabel nameCombo;
    private JTextArea messageTextArea;
    private JList ignorePatterns;
    private JCheckBox isGeneralCheckBox;
    private JLabel ignoreLabel;
    JLabel applyForLabel;
    private JPanel applyFor;
    public JPanel contentPanel;
    private JCheckBox showMessageCheckBox;
    private JButton removeButton;
    private JButton addButton;
    private JScrollPane ignorePatternsScrollPane;
    private ValueModel settingsModel;

    @SuppressWarnings("unchecked")
    public void setBeanAdapter(VerboseBeanAdapter<MatchConfig> beanAdapter) {
        Bindings.bind(nameTextField, beanAdapter.getPropertyModel("name"));
        Bindings.bind(levelComboBox, new SelectionInList<String>(getLevels(), beanAdapter.getPropertyModel("level")));
        Bindings.bind(messageTextArea, beanAdapter.getPropertyModel("message"));
        Bindings.bind(isGeneralCheckBox, beanAdapter.getPropertyModel("useArchive"));
        ValueModel exceptions = beanAdapter.getPropertyModel("exceptions");
        ArrayListModel<MatchConfig> listModel = new ArrayListModel<MatchConfig>((Collection<? extends MatchConfig>) exceptions.getValue());
        ListModelUpdater<MatchConfig> listModelUpdater = new ListModelUpdater<MatchConfig>(listModel);
        exceptions.addValueChangeListener(listModelUpdater);
        ignorePatterns.setModel(listModel);
    }

    private List<String> getLevels() {
        List<String> levels = new ArrayList<String>();
        levels.add(Level.FATAL.toString());
        levels.add(Level.ERROR.toString());
        levels.add(Level.WARN.toString());
        levels.add(Level.INFO.toString());
        levels.add(Level.DEBUG.toString());
        levels.add(Level.TRACE.toString());
        return levels;
    }

    public void hideException() {
        ignoreLabel.setVisible(false);
        ignorePatterns.setVisible(false);
        ignorePatternsScrollPane.setVisible(false);
        removeButton.setVisible(false);
        addButton.setVisible(false);
    }

    public void setIsArchive(boolean value) {
        isGeneralCheckBox.setSelected(value);
    }

    public void setShowNotification(boolean value) {
        showMessageCheckBox.setSelected(value);
    }

    public void setLogSettingsList(ValueModel settingsModel) {
        this.settingsModel = settingsModel;
        updateSettings();
        settingsModel.addValueChangeListener(new SettingsListener());
    }

    @SuppressWarnings("unchecked")
    private void updateSettings() {
        for (Component component : applyFor.getComponents()) {
            applyFor.remove(component);
        }
        for (LogSettings config : (List<LogSettings>) settingsModel.getValue()) {
            JCheckBox checkbox = new JCheckBox(config.getName());
            applyFor.add(checkbox);
        }
    }


    private class SettingsListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateSettings();
        }
    }
}
