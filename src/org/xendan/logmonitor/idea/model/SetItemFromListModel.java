package org.xendan.logmonitor.idea.model;

import com.intellij.ui.JBColor;
import com.intellij.ui.ListCellRendererWrapper;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.value.ValueModel;
import org.apache.commons.lang.StringUtils;
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
    protected JPanel itemPanel;
    JList itemsList;
    private final ValueModel listValueModel;
    private final String toStringProperty;
    final VerboseBeanAdapter<T> beanAdapter;
    private ListModelUpdater listModelUpdater;
    private JComponent firstFocusComponent;

    public SetItemFromListModel(JButton newButton, JButton removeButton, JPanel itemPanel, JList itemsList, ValueModel listValueModel, String toStringProperty) {
        this.newButton = newButton;
        this.removeButton = removeButton;
        this.itemPanel = itemPanel;
        this.itemsList = itemsList;
        this.listValueModel = listValueModel;
        this.toStringProperty = toStringProperty;
        beanAdapter = new VerboseBeanAdapter<T>(newBeanInstance());
        init();
    }

    protected void setFirstFocusComponent(JComponent firstFocusComponent) {
        this.firstFocusComponent = firstFocusComponent;
    }

    protected void init() {
        newButton.addActionListener(new NewButtonListener());
        removeButton.setEnabled(false);
        removeButton.addActionListener(new RemoveButtonActionListener());
        itemsList.addListSelectionListener(new ItemListSelectionListener());
        itemsList.setCellRenderer(new NewRenderer());
        disableItemPanel();
        ArrayListModel<T> listModel = new ArrayListModel<T>((Collection) listValueModel.getValue());
        listModelUpdater = new ListModelUpdater(listModel);
        listValueModel.addValueChangeListener(listModelUpdater);
        itemsList.setModel(listModel);
        beanAdapter.getPropertyModel(toStringProperty).addValueChangeListener(new ListRefresher(listModel));
        bind(beanAdapter);
    }

    public void disableItemPanel() {
        setPanelEnabled(itemPanel, false);
    }

    protected void setPanelEnabled(JPanel itemPanel, boolean enabled) {
        for (Component component : itemPanel.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof JPanel) {
                setPanelEnabled((JPanel) component, enabled);
            }
        }
    }

    protected abstract void bind(VerboseBeanAdapter<T> beanAdapter);

    protected final T newBeanInstance() {
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

    public ValueModel getBeanModel(String propertyName) {
        return beanAdapter.getPropertyModel(propertyName);
    }

    protected void onNewClicked() {
        onItemCommit();
        T newBean = initBean(newBeanInstance());
        List<T> items = getItemsList();
        items.add(newBean);
        listValueModel.setValue(items);
        listModelUpdater.setIgnoreItemSelection(true);
        itemsList.setSelectedValue(newBean, true);
        applyItemSet();
        listModelUpdater.setIgnoreItemSelection(false);
        setPanelEnabled(itemPanel, true);
        firstFocusComponent.requestFocus();
    }

    protected T initBean(T bean) {
        return bean;
    }

    public void onItemCommit() {
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
            if (!listModelUpdater.isIgnoreItemSelection()) {
                onItemCommit();
                applyItemSet();
            }
        }
    }

    private void applyItemSet() {
        removeButton.setEnabled(true);
        setPanelEnabled(itemPanel, true);
        onItemSet();
        beanAdapter.setBean((T) itemsList.getSelectedValue());
    }

    protected void onItemSet() {

    }

    private class RemoveButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            removeItem(getSelected());
            disableItemPanel();
        }
    }

    private void removeItem(T item) {
        List<T> items = getItemsList();
        items.remove(item);
        listValueModel.setValue(items);
        if (itemsList.getSelectedIndex() == -1) {
            removeButton.setEnabled(false);
        }
    }

    protected void onNamePropertyUpdated() {

    }

    private class ListRefresher implements PropertyChangeListener {
        private final ArrayListModel<T> listModel;

        public ListRefresher(ArrayListModel<T> listModel) {
            this.listModel = listModel;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            listModel.fireContentsChanged(itemsList.getSelectedIndex());
            onNamePropertyUpdated();
        }
    }

    private class NewRenderer extends ListCellRendererWrapper<T> {

        @Override
        public void customize(JList jList, T t, int i, boolean b, boolean b2) {
            if (StringUtils.isEmpty(t.toString())) {
                setText("New...");
                setForeground(JBColor.GRAY);
            } else {
                setText(t.toString());
            }
        }
    }
}
