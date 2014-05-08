package org.xendan.logmonitor.web.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
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
    public LogServiceProvider(HomeResolver resolver, ConfigurationDao dao) {
        logServiceHandler = new LogServiceHandler(new LogServiceImpl(resolver, dao), dao);
    }

    @Override
    public LogService get() {
        return (LogService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{LogService.class}, logServiceHandler);
    }

    private static class LogServiceHandler implements InvocationHandler {
        private final LogServiceImpl logService;
        private final ConfigurationDao dao;

        public LogServiceHandler(LogServiceImpl logService, ConfigurationDao dao) {
            this.logService = logService;
            this.dao = dao;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method similarOnService = findSimilar(method);
            if (similarOnService != null) {
                return similarOnService.invoke(logService, args);
            }
            return method.invoke(dao, args);
        }

        private Method findSimilar(Method method) {
            for (Method serviceMethod : LogServiceImpl.class.getMethods()) {
                if (serviceMethod.getName().equals(method.getName()) &&
                        Arrays.equals(serviceMethod.getParameterTypes(), method.getParameterTypes())) {
                    return serviceMethod;
                }
            }
            return null;
        }
    }
}
