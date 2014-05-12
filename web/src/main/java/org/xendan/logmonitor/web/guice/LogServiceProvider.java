package org.xendan.logmonitor.web.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.xendan.logmonitor.web.service.LogService;
import org.xendan.logmonitor.web.service.LogServiceHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author mullomuk
 * @since 5/8/2014.
 */
public class LogServiceProvider implements Provider<LogService> {

    private final InvocationHandler logServiceHandler;

    @Inject
    public LogServiceProvider(LogServiceHandler logServiceHandler) {
        this.logServiceHandler = logServiceHandler;
    }

    @Override
    public LogService get() {
        return (LogService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{LogService.class}, logServiceHandler);
    }

}
