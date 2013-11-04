package org.xendan.logmonitor.idea.model;

/**
 * User: id967161
 * Date: 04/11/13
 */
public interface OnOkAction {

    /**
     * @return true if action was completed successfully and dialog can be closed.
     */
    boolean doAction();
}
