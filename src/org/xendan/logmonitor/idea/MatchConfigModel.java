package org.xendan.logmonitor.idea;

import org.apache.log4j.Level;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.*;

/**
 * User: id967161
 * Date: 10/09/13
 */
public class MatchConfigModel {
    private final JTextField mathcerName;
    private final JComboBox levelComboBox;
    private final JTextArea matchMessageTextArea;
    private final JCheckBox useArchive;

    public MatchConfigModel(JTextField mathcerName, JComboBox levelComboBox, JTextArea matchMessageTextArea, JCheckBox useArchive) {
        this.mathcerName = mathcerName;
        this.levelComboBox = levelComboBox;
        this.matchMessageTextArea = matchMessageTextArea;
        this.useArchive = useArchive;
        levelComboBox.setModel(new DefaultComboBoxModel(new Level[]{Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE}));
    }

    public MatchConfig createMatcher() {
        MatchConfig matcher = new MatchConfig();
        matcher.setName(mathcerName.getText());
        matcher.setLevel(levelComboBox.getSelectedItem().toString());
        matcher.setMessage(matchMessageTextArea.getText());
        matcher.setUseArchive(useArchive.isSelected());
        return matcher;
    }


    public void setMatcher(MatchConfig config) {
        mathcerName.setText(config.getName());
        levelComboBox.setSelectedItem(Level.toLevel(config.getLevel()));
        matchMessageTextArea.setText(config.getMessage());
        useArchive.setSelected(config.isUseArchive());
    }
}