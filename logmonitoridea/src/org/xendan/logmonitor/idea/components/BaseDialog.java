package org.xendan.logmonitor.idea.components;

import org.xendan.logmonitor.idea.OnOkAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BaseDialog extends JDialog {

	public static final int WIDTH = 870;
	public static final int HEIGHT = 820;

	private final OnOkAction onOkAction;

	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JPanel infoPanel;

	public BaseDialog(OnOkAction onOkAction, JComponent content) {
		this.onOkAction = onOkAction;
		setContentPane(contentPane);
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
		infoPanel.add(content);
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
		if (onOkAction.canClose()) {
			dispose();
		}
	}

	private void onCancel() {
		dispose();
	}

	public void setTitleAndShow(String title) {
		setTitle(title);
		setSize(WIDTH, HEIGHT);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}

}
