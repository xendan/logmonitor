package org.xendan.logmonitor.web.service;

import com.google.inject.Inject;
import com.google.inject.persist.UnitOfWork;
import org.xendan.logmonitor.web.dao.ConfigurationDao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;


public class LogServiceHandler implements InvocationHandler {
    private final LogServiceImpl logService;
    private final ConfigurationDao dao;
    private EnvironmentMonitor monitor;

    @Inject
    public LogServiceHandler(LogServiceImpl logService, ConfigurationDao dao, EnvironmentMonitor monitor) {
        this.logService = logService;
        this.dao = dao;
        this.monitor = monitor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        dao.sanitizeCheck();
        Method similar = findSimilar(method, EnvironmentMonitor.class);
        if (similar != null) {
            return similar.invoke(monitor, args);
        }
        similar = findSimilar(method, LogServiceImpl.class);
        if (similar != null) {
            return similar.invoke(logService, args);
        }
        Object result = method.invoke(dao, args);
        dao.sanitizeCheck();
        return result;
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
