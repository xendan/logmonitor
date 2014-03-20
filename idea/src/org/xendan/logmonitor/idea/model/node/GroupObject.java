package org.xendan.logmonitor.idea.model.node;

import org.apache.commons.lang.StringUtils;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.model.LogEntryGroup;
import org.xendan.logmonitor.parser.PatternUtils;

/**
* User: id967161
* Date: 27/11/13
*/
public class GroupObject extends EntityObject<LogEntryGroup> implements ConsoleDisplayable {

    public GroupObject(LogEntryGroup group) {
        super(group);
    }

    @Override
    public String toString() {
        return getBaseMessage() + ":" + StringUtils.abbreviate(getMessage(), LogMonitorPanelModel.MSG_WIDTH);
    }

    private String getBaseMessage() {
        return entity.getEntries().size() + " similar entries";
    }

    @Override
    public String toConsoleString() {
        return getBaseMessage() + " matched by \n" + getMessage();
    }

    private String getMessage() {
        return PatternUtils.regexToSimple(entity.getMessagePattern());
    }
}
