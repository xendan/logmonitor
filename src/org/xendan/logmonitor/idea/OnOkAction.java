package org.xendan.logmonitor.idea;

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
