package org.xendan.logmonitor.idea;

import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class MatchConfigListModel extends AbstractValueModel implements PropertyChangeListener {
    private final ValueModel environmentsModel;
    private List<MatchConfig> list = null;

    public MatchConfigListModel(ValueModel environmentsModel) {
        this.environmentsModel = environmentsModel;
        environmentsModel.addValueChangeListener(this);
    }

    @Override
    public List<MatchConfig> getValue() {
        if (list == null) {
            List<Environment> environments = (List<Environment>) environmentsModel.getValue();
            list = new ArrayList<MatchConfig>();
            //TODO check when
            if (environments != null) {
                for (Environment environment : environments) {
                    for (MatchConfig matchConfig : environment.getMatchConfigs()) {
                        if (!list.contains(matchConfig)) {
                            list.add(matchConfig);
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void setValue(Object newValue) {
        Object oldValue = list;
        list = (List<MatchConfig>) newValue;
        fireValueChange(oldValue, list);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object oldValue = list;
        list = null;
        fireValueChange(oldValue, getValue());
    }
}