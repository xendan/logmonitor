package org.xendan.logmonitor.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 05/09/13
 */
@XmlRootElement
public class Matchers implements Serializable {
    private List<EntryMatcher> matchers = new ArrayList<EntryMatcher>();

    public List<EntryMatcher> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<EntryMatcher> matchers) {
        this.matchers = matchers;
    }
}
