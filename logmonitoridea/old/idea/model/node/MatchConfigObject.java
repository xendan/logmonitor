package org.xendan.logmonitor.idea.model.node;

import org.xendan.logmonitor.idea.model.node.ConsoleDisplayable;
import org.xendan.logmonitor.idea.model.node.EntityObject;
import org.xendan.logmonitor.model.MatchConfig;

/**
* User: id967161
* Date: 27/11/13
*/
public class MatchConfigObject extends EntityObject<MatchConfig> implements ConsoleDisplayable {
    private int childNum;

    public MatchConfigObject(MatchConfig matchConfig) {
        super(matchConfig);
    }
//        TODO:implement normal child count
//
//        public void setChildNum(int childNum) {
//            this.childNum = childNum;
//        }

    @Override
    public String toString() {
        return entity.toString() + "(" + childNum + ")";
    }

    @Override
    public String toConsoleString() {
        String patternStr = entity.getMessage() == null ? "" : "\nPattern:\n" + entity.getMessage();
        return entity.toString() + "\nLEVEL>=" + entity.getLevel() + patternStr;
    }
}
