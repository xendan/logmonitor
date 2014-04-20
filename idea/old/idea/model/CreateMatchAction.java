package org.xendan.logmonitor.idea.model;

import com.jgoodies.binding.value.ValueHolder;
import org.xendan.logmonitor.dao.LogService;
import org.xendan.logmonitor.dao.DefaultCallBack;
import org.xendan.logmonitor.idea.BaseDialog;
import org.xendan.logmonitor.idea.MatchConfigForm;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.idea.OnOkAction;
import org.xendan.logmonitor.parser.EntryStatusListener;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
* User: id967161
* Date: 27/11/13
*/
class CreateMatchAction extends AbstractAction {

    private final Serializer serializer;
    private final EntryStatusListener listener;
    private final LogService dao;
    private final Configuration configuration;
    private final String level;
    private final String message;

    public CreateMatchAction(Serializer serializer, EntryStatusListener listener, LogService dao, Configuration configuration, String name, String level, String message) {
        super(name);
        this.serializer = serializer;
        this.listener = listener;
        this.dao = dao;
        this.configuration = configuration;
        this.level = level;
        this.message = message;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MatchConfigForm matchConfigForm = new MatchConfigForm();
        final MatchConfig match = new MatchConfig();
        match.setMessage(message);
        match.setLevel(level);
        VerboseBeanAdapter<MatchConfig> beanAdapter = new VerboseBeanAdapter<MatchConfig>(match);
        matchConfigForm.setBeanAdapters(beanAdapter);
        final List<Environment> copy = serializer.doCopy(configuration.getEnvironments());
        int maxWeight = 0;
        for (Environment environment : copy) {
            for (MatchConfig matchConfig : environment.getMatchConfigs()) {
                if (matchConfig.getWeight() != null && matchConfig.getWeight() > maxWeight) {
                    maxWeight = matchConfig.getWeight();
                }
            }
            environment.getMatchConfigs().add(match);
        }
        match.setWeight(maxWeight + 1);
        matchConfigForm.setEnvironments(new ValueHolder(copy));
        matchConfigForm.setIsSpecific();
        BaseDialog dialog = new BaseDialog(new OnMatchConfigOkAction(match, copy), matchConfigForm.getContentPanel());
        dialog.setTitleAndShow("Add new match config");
    }

    private class OnMatchConfigOkAction extends Thread implements OnOkAction {
        private final MatchConfig config;
        private final List<Environment> copy;

        private OnMatchConfigOkAction(MatchConfig config, List<Environment> copy) {
            this.config = config;
            this.copy = copy;
        }

        @Override
        public boolean canClose() {
            start();
            return true;
        }

        @Override
        public void run() {
            List<Environment> originals = configuration.getEnvironments();
            for (int i = 0; i < copy.size(); i++) {
                if (copy.get(i).getMatchConfigs().contains(config)) {
                    final Environment original = originals.get(i);
                    dao.addMatchConfig(originals.get(i), config, new DefaultCallBack<Void>() {
                        @Override
                        public void onAnswer(Void answer) {
                            listener.onEntriesAdded(original);
                        }
                    });
                }
            }
        }
    }
}
