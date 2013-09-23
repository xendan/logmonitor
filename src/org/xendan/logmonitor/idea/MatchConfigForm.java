package org.xendan.logmonitor.idea;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import org.apache.log4j.Level;
import org.xendan.logmonitor.idea.model.BeanChangeListener;
import org.xendan.logmonitor.idea.model.ListModelUpdater;
import org.xendan.logmonitor.idea.model.VerboseBeanAdapter;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
    public static final String ENVIRONMENT = "environment";
    private JTextField nameTextField;
    private JComboBox levelComboBox;
    private JTextArea messageTextArea;
    private JList ignorePatterns;
    private JCheckBox isGeneralCheckBox;
    private JLabel ignoreLabel;
    JLabel applyForLabel;
    JPanel applyFor;
    public JPanel contentPanel;
    private JCheckBox showMessageCheckBox;
    private JButton removeButton;
    private JButton addButton;
    private JScrollPane ignorePatternsScrollPane;
    private ValueModel environmentsModel;
    private VerboseBeanAdapter<MatchConfig> beanAdapter;

    @SuppressWarnings("unchecked")
    public void setBeanAdapters(VerboseBeanAdapter<MatchConfig> beanAdapter) {
        this.beanAdapter = beanAdapter;
        Bindings.bind(nameTextField, beanAdapter.getPropertyModel("name"));
        Bindings.bind(levelComboBox, new SelectionInList<String>(getLevels(), beanAdapter.getPropertyModel("level")));
        Bindings.bind(messageTextArea, beanAdapter.getPropertyModel("message"));
        Bindings.bind(isGeneralCheckBox, beanAdapter.getPropertyModel("useArchive"));
        Bindings.bind(showMessageCheckBox, beanAdapter.getPropertyModel("showNotification"));
        ValueModel exceptions = beanAdapter.getPropertyModel("exceptions");
        ArrayListModel<MatchConfig> listModel = new ArrayListModel<MatchConfig>((Collection<? extends MatchConfig>) exceptions.getValue());
        ListModelUpdater<MatchConfig> listModelUpdater = new ListModelUpdater<MatchConfig>(listModel);
        exceptions.addValueChangeListener(listModelUpdater);
        ignorePatterns.setModel(listModel);
        beanAdapter.addBeanChangeListener(new ApplyEnvironmentAdapter());
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

    public void setEnvironments(ValueModel environmentsModel) {
        this.environmentsModel = environmentsModel;
        updateSettings();
        environmentsModel.addValueChangeListener(new EnvironmentListener());
    }

    @SuppressWarnings("unchecked")
    private void updateSettings() {
        for (Component component : applyFor.getComponents()) {
            applyFor.remove(component);
        }
        List<Environment> environments = (List<Environment>) environmentsModel.getValue();
        if (environments == null) {
            environments = new ArrayList<Environment>();
        }
        boolean visible = environments.size() > 1;
        applyFor.setVisible(visible);
        applyForLabel.setVisible(visible);
        for (Environment environment : environments) {
            JCheckBox checkbox = new JCheckBox(environment.getName());
            checkbox.putClientProperty(ENVIRONMENT, environment);
            if (beanAdapter != null) {
                checkbox.setSelected(environment.getMatchConfigs().contains(beanAdapter.getBean()));
            }
            checkbox.addItemListener(new EnvironmentEnableListener());
            applyFor.add(checkbox);
        }
    }


    private class EnvironmentListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateSettings();
        }
    }

    private class ApplyEnvironmentAdapter implements BeanChangeListener<MatchConfig> {
        @Override
        public void onBeanSet(MatchConfig newBean) {
            for (Component component : applyFor.getComponents()) {
                JCheckBox checkBox = (JCheckBox) component;
                Environment environment = (Environment) checkBox.getClientProperty(ENVIRONMENT);
                checkBox.setSelected(environment.getMatchConfigs().contains(newBean));
            }
        }
    }

    private class EnvironmentEnableListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            Environment environment = (Environment) checkBox.getClientProperty(ENVIRONMENT);
            if (checkBox.isSelected()) {
                environment.getMatchConfigs().add(beanAdapter.getBean());
            } else {
                environment.getMatchConfigs().remove(beanAdapter.getBean());
            }
        }
    }
}
