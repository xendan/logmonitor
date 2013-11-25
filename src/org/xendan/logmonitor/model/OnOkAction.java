package org.xendan.logmonitor.model;

/**
 * User: id967161
 * Date: 22/11/13
 */
public interface OnOkAction {
    OnOkAction DO_NOTHING = new OnOkAction() {
        @Override
        public boolean canClose() {
            return true;
        }
    };

    boolean canClose();

}
