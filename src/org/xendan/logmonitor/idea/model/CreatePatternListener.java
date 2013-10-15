package org.xendan.logmonitor.idea.model;

import org.xendan.logmonitor.model.MatchConfig;

/**
 * User: id967161
 * Date: 10/09/13
 */
public interface CreatePatternListener {
    void onMathConfigAdded(MatchConfig matcher);
}
