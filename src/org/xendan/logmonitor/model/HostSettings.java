package org.xendan.logmonitor.model;

import org.joda.time.DateTime;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class HostSettings {

    private DateTime lastReadDate;

    public DateTime getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(DateTime lastReadDate) {
        this.lastReadDate = lastReadDate;
    }
}
