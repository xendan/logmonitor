package org.xendan.logmonitor.idea.model.task;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.idea.BaseDialog;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.idea.OnOkAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
* User: id967161
* Date: 16/12/13
*/
public class ShowLogAroundAction extends AbstractAction {
    private final Environment settings;
    private final HomeResolver homeResolver;
    private final String date;
    private final String pattern;
    private final Project project;

    public ShowLogAroundAction(Environment settings, HomeResolver homeResolver, String date, String pattern, Project project) {
        super("Show log fragment");
        this.settings = settings;
        this.homeResolver = homeResolver;
        this.date = date;
        this.pattern = pattern;
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ConsoleViewImpl console = new ConsoleViewImpl(project, false);
        JScrollPane scrollPane = new JBScrollPane();
        scrollPane.setViewportView(console.getComponent());
        BaseDialog dialog = new BaseDialog(OnOkAction.DO_NOTHING, scrollPane);
        new LoadRoundLogTask(date, pattern, console, settings, homeResolver, project.getName()).start();
        console.print("Loading...", ConsoleViewContentType.NORMAL_OUTPUT);
        dialog.setTitleAndShow("Log around " + date);
    }
}
