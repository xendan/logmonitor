package org.xendan.logmonitor.read;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import javax.swing.*;
import java.awt.*;

/**
* User: kcyxa
* Date: 1/26/13
*/
public class DummyUserInfo implements UserInfo {
    private final String password;

    public DummyUserInfo(String password) {
        this.password = password;
    }

    public boolean promptYesNo(String str) {
        return true;
    }

    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public boolean promptPassphrase(String message) {
        return true;
    }

    public boolean promptPassword(String message) {
//        Object[] ob = {passwordField};
//        int result =
//                JOptionPane.showConfirmDialog(null, ob, message,
//                        JOptionPane.OK_CANCEL_OPTION);
//        if (result == JOptionPane.OK_OPTION) {
//            passwd = passwordField.getText();
//            return true;
//        } else {
//            return false;
//        }
        return true;
    }

    public void showMessage(String message) {
//        JOptionPane.showMessageDialog(null, message);
    }

//    final GridBagConstraints gbc =
//            new GridBagConstraints(0, 0, 1, 1, 1, 1,
//                    GridBagConstraints.NORTHWEST,
//                    GridBagConstraints.NONE,
//                    new Insets(0, 0, 0, 0), 0, 0);
//    private Container panel;

}
