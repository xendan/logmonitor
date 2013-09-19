package org.xendan.logmonitor.idea;

import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: id967161
 * Date: 17/09/13
 */
public class VerboseBeanAdapter<T>  {
    private final BeanAdapter<T> adapter;
    private Map<String, VerboseModel> models = new HashMap<String, VerboseModel>();
    private List<BeanChangeListener<T>> listeners = new ArrayList<BeanChangeListener<T>>();

    public VerboseBeanAdapter(T bean) {
        adapter = new BeanAdapter<T>(bean);
    }

    public T getBean() {
        return adapter.getBean();
    }

    public void addBeanChangeListener(BeanChangeListener<T> listener) {
        listeners.add(listener);
    }

    public ValueModel getPropertyModel(String property) {
        if (!models.containsKey(property)) {
            VerboseModel model = new VerboseModel(adapter.getValueModel(property));
            models.put(property, model);
        }
        return models.get(property);
    }

    public void setBean(T newBean) {
        adapter.setBean(newBean);
        for (BeanChangeListener listener : listeners) {
            listener.onBeanSet(newBean);
        }
        for (VerboseModel model : models.values()) {
            model.fireChanged(model.getValue(), null);
        }
    }

    private class VerboseModel extends AbstractValueModel {
        private final ValueModel valueModel;

        public VerboseModel(ValueModel valueModel) {
            this.valueModel = valueModel;
        }

        @Override
        public Object getValue() {
            return valueModel.getValue();
        }

        @Override
        public void setValue(Object newValue) {
            Object oldValue = getValue();
            valueModel.setValue(newValue);
            fireChanged(newValue, oldValue);
        }

        private void fireChanged(Object newValue, Object oldValue) {
            firePropertyChange("value", oldValue, newValue);
        }
    }

}
