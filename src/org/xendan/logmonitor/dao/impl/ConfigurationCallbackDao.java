package org.xendan.logmonitor.dao.impl;

import org.xendan.logmonitor.dao.Callback;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.*;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * User: id967161
 * Date: 22/11/13
 */
public class ConfigurationCallbackDao {

    protected final ConfigurationDao wrapped;
    private final Executor executor =  Executors.newFixedThreadPool(5);

    public ConfigurationCallbackDao(ConfigurationDao wrapped) {
        this.wrapped = wrapped;
    }

    public void getConfigs(Callback<List<Configuration>> callback) {
        schedule(new Callable<List<Configuration>>(){
            @Override
            public List<Configuration> call() throws Exception {

                return wrapped.getConfigs();
            }
        }, callback);
    }

    private <T> void schedule(final Callable<T> callable, final Callback<T> callback) {
        executor.execute(new RunDaoTask<T>(callback, callable));
    }

    public void getNotGroupedMatchedEntries(final MatchConfig matchConfig, final Environment environment, Callback<List<LogEntry>> callback) {
        schedule(new Callable<List<LogEntry>>(){
            @Override
            public List<LogEntry> call() throws Exception {
                return wrapped.getNotGroupedMatchedEntries(matchConfig, environment);
            }
        }, callback);
    }

    public void getMatchedEntryGroups(final MatchConfig matchConfig, final Environment environment, Callback<List<LogEntryGroup>> callback) {
        schedule(new Callable<List<LogEntryGroup>>(){
            @Override
            public List<LogEntryGroup> call() throws Exception {
                return wrapped.getMatchedEntryGroups(matchConfig, environment);
            }
        }, callback);
    }

    public void addMatchConfig(final Environment environment, final MatchConfig config, Callback<Void> callback) {
        schedule(new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                wrapped.addMatchConfig(environment, config);
                return null;
            }
        }, callback);
    }

    public void remove(final BaseObject object) {
        executor.execute(new NoCallbackTask(new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                wrapped.remove(object);
                return null;
            }
        }));
    }

    public void removeAllEntries(final Environment environment) {
        executor.execute(new NoCallbackTask(new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                wrapped.remove(environment);
                return null;
            }
        }));
    }

    public void save(final List<Configuration> configs, Callback<Void> callback) {
        schedule(new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                wrapped.save(configs);
                return null;
            }
        }, callback);
    }

    public void clearAll(final boolean createTestTmp, Callback<Void> callback) {
        schedule(new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                wrapped.clearAll(createTestTmp);
                return null;
            }
        }, callback);
    }

    public void removeMatchConfig(final MatchConfig match, final Environment environment) {
        executor.execute(new NoCallbackTask(new Callable<Void>(){
            @Override
            public Void call() throws Exception {
                wrapped.removeMatchConfig(environment, match);
                return null;
            }
        }));
    }

    private static class RunDaoTask<T> implements Runnable {
        private final Callback<T> callback;
        private final Callable<T> callable;

        public RunDaoTask(Callback<T> callback, Callable<T> callable) {
            this.callback = callback;
            this.callable = callable;
        }

        @Override
        public void run() {
            T answer = null;
            Exception ex = null;
            try {
                answer = callable.call();
            } catch (Exception e) {
                ex = e;
            }
            try {
                SwingUtilities.invokeAndWait(new AnswerInSwing(answer, ex));
            } catch (Exception e) {
                throw new IllegalStateException("Error waiting swing execution", e);
            }

        }

        private class AnswerInSwing implements Runnable {
            private final T answer;
            private final Exception ex;

            public AnswerInSwing(T answer, Exception ex) {
                this.answer = answer;
                this.ex = ex;
            }

            @Override
            public void run() {
                if (ex != null) {
                    callback.onFail(ex);
                } else {
                    callback.onAnswer(answer);
                }
            }
        }
    }

    private static class NoCallbackTask extends RunDaoTask<Void> {

        public NoCallbackTask(Callable<Void> callable) {
            super(DefaultCallBack.DO_NOTHING, callable);
        }
    }
}
