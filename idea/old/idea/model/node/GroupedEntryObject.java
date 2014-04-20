package org.xendan.logmonitor.idea.model.node;

import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogEntryGroup;
import org.xendan.logmonitor.parser.PatternUtils;

/**
* User: id967161
* Date: 27/11/13
*/
public class GroupedEntryObject extends EntryObject {

    private final LogEntryGroup group;

    public GroupedEntryObject(LogEntry entry, LogEntryGroup group) {
        super(entry);
        this.group = group;
    }

    @Override
    protected String getMessage() {
        return PatternUtils.restoreMessage(entity, group.getMessagePattern());
    }
}
