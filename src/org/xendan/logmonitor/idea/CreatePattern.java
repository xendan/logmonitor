package org.xendan.logmonitor.idea;

import org.xendan.logmonitor.parser.LogParser;

import javax.swing.*;
import java.awt.event.*;

public class CreatePattern extends JDialog {
    private final CreatePatternListener listener;
    private final MatchConfigModel model;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameTextField;
    private JComboBox levelCombobox;
    private JTextField patternTextField;
    private JCheckBox useArchive;
    private JTextArea messageTextArea;

    public CreatePattern(String pattern, CreatePatternListener listener) {
        this.listener = listener;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        useArchive.setSelected(true);
        model = new MatchConfigModel(nameTextField, levelCombobox, messageTextArea, useArchive);
        messageTextArea.setText(LogParser.replaceSpecial(pattern));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        listener.onMathConfigAdded(model.createMatcher());
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        CreatePattern dialog = new CreatePattern("AAA", null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
