package org.xendan.logmonitor.idea.model.task;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import org.apache.commons.io.FileUtils;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.read.command.RoundLogDownloader;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * User: id967161
 * Date: 16/12/13
 */
public class LoadRoundLogTask extends Thread {
    private final String date;
    private final String pattern;
    private final ConsoleViewImpl console;
    private final Environment settings;
    private final HomeResolver homeResolver;
    private final String project;

    public LoadRoundLogTask(String date, String pattern, ConsoleViewImpl console, Environment settings, HomeResolver homeResolver, String project) {
        this.date = date;
        this.pattern = pattern;
        this.console = console;
        this.settings = settings;
        this.homeResolver = homeResolver;
        this.project = project;
    }

    @Override
    public void run() {
        try {
            String localFile = new RoundLogDownloader(settings, homeResolver, project, pattern).downloadToLocal(date);
            //TODO highlight errors
            printMessage(FileUtils.readFileToString(new File(localFile)), ConsoleViewContentType.NORMAL_OUTPUT);
        } catch (Exception e) {
            printMessage("Error getting log:\n" + getExceptionMessage(e), ConsoleViewContentType.ERROR_OUTPUT);
        }
    }

    private String getExceptionMessage(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter( writer );
        e.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }

    private void printMessage(final String log, final ConsoleViewContentType output) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                console.clear();
                console.print(log, output);
            }
        });
    }
}
