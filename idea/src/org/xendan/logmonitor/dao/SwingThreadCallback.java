package org.xendan.logmonitor.dao;

import javax.swing.*;

/**
 * User: id967161
 * Date: 19/03/14
 */
public abstract class SwingThreadCallback<T> extends DefaultCallBack<T> {

    @Override
    public final void onAnswer(final T answer) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                doInSwingThread(answer);
            }
        });
    }

    protected abstract void doInSwingThread(T answer);
}
