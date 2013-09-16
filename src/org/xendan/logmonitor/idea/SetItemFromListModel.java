package org.xendan.logmonitor.idea;

import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import org.xendan.logmonitor.model.BaseObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * User: id967161
 * Date: 16/09/13
 */
public abstract class SetItemFromListModel<T extends BaseObject> {
    private JButton newButton;
    private JButton removeButton;
    private JPanel itemPanel;
    private JList itemsList;
    private BeanAdapter<T> beanAdapter;
    private ArrayListModel<T> listModel;

    public SetItemFromListModel(JButton newButton, JButton removeButton, JPanel itemPanel, JList itemsList) {
        this.newButton = newButton;
        this.removeButton = removeButton;
        this.itemPanel = itemPanel;
        this.itemsList = itemsList;
        init();
    }

    private void init() {
        newButton.addActionListener(new NewButtonListener());
        removeButton.setEnabled(false);
        removeButton.addActionListener(new RemoveButtonActionListener());
        itemsList.addListSelectionListener(new ItemListSelectionListener());
        setPanelEnabled(itemPanel, false);
    }

    private BeanAdapter<T> initBeanAdapter(T bean) {
        if (beanAdapter == null) {
            beanAdapter = new BeanAdapter<T>(bean);
            bind(beanAdapter);
        } else {
            beanAdapter.setBean(bean);
        }
        return beanAdapter;
    }

    private void setPanelEnabled(JPanel itemPanel, boolean enabled) {
        for (Component component : itemPanel.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof JPanel) {
                setPanelEnabled((JPanel) component, enabled);
            }
        }
    }

    protected abstract void bind(BeanAdapter<T> beanAdapter);

    @SuppressWarnings("unchecked")
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
        if (beanAdapter == null) {
            return null;
        }
        return beanAdapter.getBean();
    }

    public void setItemsModel(ArrayListModel<T> listModel) {
        this.listModel = listModel;
        itemsList.setModel(listModel);
    }

    private class NewButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            T newBean = newBeanInstance();
            listModel.add(newBean);
            initBeanAdapter(newBean);
            setPanelEnabled(itemPanel, false);
        }
    }

    @SuppressWarnings("unchecked")
    private class ItemListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            removeButton.setEnabled(true);
            setPanelEnabled(itemPanel, true);
            initBeanAdapter((T) itemsList.getSelectedValue());
        }
    }

    private class RemoveButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            listModel.remove(getSelected());
            setPanelEnabled(itemPanel, false);
        }
    }
}
