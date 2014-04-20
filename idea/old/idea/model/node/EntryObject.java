package org.xendan.logmonitor.idea.model.node;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.model.LogEntry;

/**
* User: id967161
* Date: 27/11/13
*/
public class EntryObject extends EntityObject<LogEntry> implements ConsoleDisplayable {


    public EntryObject(LogEntry entry) {
        super(entry);
    }

    @Override
    public String toString() {
        return entity.getLevel() + ":" + LogMonitorPanelModel.SHORT_DATE.print(entity.getDate()) + " " + StringUtils.abbreviate(getMessage(), LogMonitorPanelModel.MSG_WIDTH);
    }

    @Override
    public String toConsoleString() {
        return entity.getLevel() + ":" + entity.getDate() + "\n" + getMessage();
    }

    protected String getMessage() {
        return entity.getMessage();
    }

    public boolean isError() {
        return Level.toLevel(entity.getLevel()).isGreaterOrEqual(Level.ERROR);
    }
}
