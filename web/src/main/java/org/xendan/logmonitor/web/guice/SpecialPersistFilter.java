package org.xendan.logmonitor.web.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import javax.servlet.*;
import java.io.IOException;

/**
 * Overrides Guice implementation to handle when the persistence service is already started
 * @see  http://stackoverflow.com/questions/17402081/how-to-start-jpa-in-a-guice-quartz-web-application
 */
@Singleton
public final class SpecialPersistFilter implements Filter {
    private final UnitOfWork unitOfWork;
    private final PersistService persistService;

    @Inject
    public SpecialPersistFilter(UnitOfWork unitOfWork, PersistService persistService) {
        this.unitOfWork = unitOfWork;
        this.persistService = persistService;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            persistService.start();
        }
        catch (IllegalStateException e) {
            // Ignore exception is the persist service was already started
        }
    }

    public void destroy() {
        persistService.stop();
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        unitOfWork.begin();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        finally {
            unitOfWork.end();
        }
    }
}