package org.xendan.logmonitor.dao;

import org.apache.log4j.Logger;
import org.xendan.logmonitor.idea.LogMonitorPanel;

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
//    private static final Logger logger = LoggerFactory.getInstance().getLoggerInstance(DefaultCallBack.class.getCanonicalName());
    private static final Logger logger = Logger.getLogger(LogMonitorPanel.class);
    @Override
    public void onFail(Throwable error) {
        logger.error(error);
    }
}
