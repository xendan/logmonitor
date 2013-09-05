package org.xendan.logmonitor.model;

import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * User: id967161
 * Date: 03/09/13
 */
@Entity
public class ServerSettings implements Serializable {
    private Long id;
    private String name;
    private String host;
    private String login;
    private String password;
    private String path;
    private DateTime lastReadDate;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return name;
    }

    public DateTime getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(DateTime lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerSettings that = (ServerSettings) o;

        return !(host != null ? !host.equals(that.host) : that.host != null) && !(login != null ? !login.equals(that.login) : that.login != null) && !(name != null ? !name.equals(that.name) : that.name != null) && !(password != null ? !password.equals(that.password) : that.password != null) && !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (login != null ? login.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

}
