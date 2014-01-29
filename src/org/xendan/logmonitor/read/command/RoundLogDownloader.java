package org.xendan.logmonitor.read.command;

import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;

import java.util.regex.Matcher;

/**
 * User: id967161
 * Date: 16/12/13
 */
public class RoundLogDownloader extends LogDownloader {

    private static final int N_OF_LINES = 11;

    private final String pattern;

    public RoundLogDownloader(Environment settings, HomeResolver homeResolver, String project, String pattern) {
        super(settings, homeResolver, CommandFileLoader.createRound(homeResolver), project);
        this.pattern = pattern;
        //TODO: do something for local files!!
    }

    @Override
    protected String replaceParameters(String datePattern, String shCommand) {
        return super.replaceParameters(datePattern, shCommand)
                .replaceAll("\\$n_of_lines", String.valueOf(N_OF_LINES))
                .replaceAll("\\$pattern", Matcher.quoteReplacement(pattern));
    }
}
