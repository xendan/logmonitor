package org.xendan.logmonitor.web.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.service.EnvironmentMonitor;
import org.xendan.logmonitor.web.service.LogService;
import org.xendan.logmonitor.web.service.LogServiceImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * @author mullomuk
 * @since 5/8/2014.
 */
public class LogServiceProvider implements Provider<LogService> {

    private final InvocationHandler logServiceHandler;

    @Inject
    public LogServiceProvider(EnvironmentMonitor monitor, ConfigurationDao dao) {
        logServiceHandler = new LogServiceHandler(new LogServiceImpl(dao), dao, monitor);
    }

    @Override
    public LogService get() {
        return (LogService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{LogService.class}, logServiceHandler);
    }

    private static class LogServiceHandler implements InvocationHandler {
        private final LogServiceImpl logService;
        private final ConfigurationDao dao;
        private EnvironmentMonitor monitor;

        public LogServiceHandler(LogServiceImpl logService, ConfigurationDao dao, EnvironmentMonitor monitor) {
            this.logService = logService;
            this.dao = dao;
            this.monitor = monitor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method similar = findSimilar(method, LogServiceImpl.class);
            if (similar != null) {
                return similar.invoke(logService, args);
            }
            similar = findSimilar(method, EnvironmentMonitor.class);
            if (similar != null) {
                return similar.invoke(monitor, args);
            }
            return method.invoke(dao, args);
        }

        private Method findSimilar(Method method, Class<?> someClass) {

            for (Method serviceMethod : someClass.getMethods()) {
                if (serviceMethod.getName().equals(method.getName()) &&
                        Arrays.equals(serviceMethod.getParameterTypes(), method.getParameterTypes())) {
                    return serviceMethod;
                }
            }
            return null;
        }
    }
}
