package org.xendan.logmonitor.idea;

import com.jgoodies.binding.list.ArrayListModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
* User: id967161
* Date: 18/09/13
*/
class ListModelUpdater<T> implements PropertyChangeListener {
    private final ArrayListModel<T> listModel;
    private boolean ignoreItemSelection;

    public ListModelUpdater(ArrayListModel<T> listModel) {
        this.listModel = listModel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        ignoreItemSelection = true;
        listModel.clear();
        listModel.addAll((Collection<? extends T>) evt.getNewValue());
        ignoreItemSelection = false;
    }

    public boolean isIgnoreItemSelection() {
        return ignoreItemSelection;
    }

    public void setIgnoreItemSelection(boolean ignoreItemSelection) {
        this.ignoreItemSelection = ignoreItemSelection;
    }
}
