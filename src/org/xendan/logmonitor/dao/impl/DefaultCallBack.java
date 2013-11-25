package org.xendan.logmonitor.dao.impl;

import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import org.xendan.logmonitor.dao.Callback;

/**
 * User: id967161
 * Date: 22/11/13
 */
public abstract class DefaultCallBack<T> implements Callback<T> {

    public static final Callback<Void> DO_NOTHING = new DefaultCallBack<Void>() {
        @Override
        public void onAnswer(Void answer) {
        }
    };
    private static final Logger logger = LoggerFactory.getInstance().getLoggerInstance(DefaultCallBack.class.getCanonicalName());

    @Override
    public void onFail(Throwable error) {
        logger.error(error);
    }
}
