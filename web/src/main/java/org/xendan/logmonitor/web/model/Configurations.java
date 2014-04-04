package org.xendan.logmonitor.web.model;

import org.xendan.logmonitor.model.Configuration;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author xendan
 * @since  4/4/2014.
 */
@XmlRootElement
public class Configurations {
    private List<Configuration> configurations;

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }
}
