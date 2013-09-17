package org.xendan.logmonitor.idea;

import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.value.ValueModel;
import org.xendan.logmonitor.model.BaseObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: id967161
 * Date: 16/09/13
 */
@SuppressWarnings("unchecked")
public abstract class SetItemFromListModel<T extends BaseObject> {
    JButton newButton;
    JButton removeButton;
    JPanel itemPanel;
    JList itemsList;
    private final ValueModel listValueModel;
    final VerboseBeanAdapter<T> beanAdapter;
    private boolean modelIsClearing;

    public SetItemFromListModel(JButton newButton, JButton removeButton, JPanel itemPanel, JList itemsList, ValueModel listValueModel) {
        this.newButton = newButton;
        this.removeButton = removeButton;
        this.itemPanel = itemPanel;
        this.itemsList = itemsList;
        this.listValueModel = listValueModel;
        beanAdapter = new VerboseBeanAdapter<T>(newBeanInstance());
        init();
    }


    protected void init() {
        newButton.addActionListener(new NewButtonListener());
        removeButton.setEnabled(false);
        removeButton.addActionListener(new RemoveButtonActionListener());
        itemsList.addListSelectionListener(new ItemListSelectionListener());
        setPanelEnabled(itemPanel, false);
        ArrayListModel<T> listModel = new ArrayListModel<T>((Collection) listValueModel.getValue());
        listValueModel.addValueChangeListener(new ListModelUpdater(listModel));
        itemsList.setModel(listModel);
        bind(beanAdapter);
    }

    private void setPanelEnabled(JPanel itemPanel, boolean enabled) {
        for (Component component : itemPanel.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof JPanel) {
                setPanelEnabled((JPanel) component, enabled);
            }
        }
    }

    protected abstract void bind(VerboseBeanAdapter<T> beanAdapter);

    private T newBeanInstance() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        if (genericSuperClass instanceof ParameterizedType) {
            Type[] genericTypes = ((ParameterizedType) genericSuperClass).getActualTypeArguments();
            if (genericTypes.length == 1) {
                try {
                    return ((Class<T>) genericTypes[0]).newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("Can't create new bean", e);
                }
            }
        }
        throw new IllegalStateException("Can't create new bean");
    }

    public T getSelected() {
        return beanAdapter.getBean();
    }

    protected void onNewClicked() {
        clearInvalid();
        T newBean = newBeanInstance();
        List<T> items = getItemsList();
        items.add(newBean);
        listValueModel.setValue(items);
        beanAdapter.setBean(newBean);
        setPanelEnabled(itemPanel, false);
    }

    private void clearInvalid() {
        for (T item : getItemsList()) {
            if (isInvalid(item)) {
                removeItem(item);
            }
        }
    }

    protected abstract boolean isInvalid(T item);

    private List<T> getItemsList() {
        return new ArrayList<T>((Collection<T>) listValueModel.getValue());
    }

    private class NewButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            onNewClicked();
        }
    }

    @SuppressWarnings("unchecked")
    private class ItemListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!modelIsClearing) {
                clearInvalid();
                removeButton.setEnabled(true);
                setPanelEnabled(itemPanel, true);
                onItemSet();
                beanAdapter.setBean((T) itemsList.getSelectedValue());
            }
        }
    }

    protected void onItemSet() {

    }

    private class RemoveButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            removeItem(getSelected());
            setPanelEnabled(itemPanel, false);
        }
    }

    private void removeItem(T item) {
        List<T> items = getItemsList();
        items.remove(item);
        listValueModel.setValue(items);
    }

    private class ListModelUpdater implements PropertyChangeListener {
        private final ArrayListModel<T> listModel;

        public ListModelUpdater(ArrayListModel<T> listModel) {
            this.listModel = listModel;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            modelIsClearing = true;
            listModel.clear();
            listModel.addAll((Collection<? extends T>) evt.getNewValue());
            modelIsClearing = false;
        }
    }
}
