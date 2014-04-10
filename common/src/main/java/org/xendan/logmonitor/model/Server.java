package org.xendan.logmonitor.model;

import javax.persistence.Entity;

/**
 * User: id967161
 * Date: 11/09/13
 */
@Entity
public class Server extends BaseObject {
    public static final String LOCALHOST = "localhost";
    private String host;
    private String login;
    private String password;
    private String keyPath;
    private String passPhrase;

    public Server() {

    }

    /*
    public Server(String host, int id) {
        this.host = host;
        setId((long) id);
    }*/

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

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    @Override
    public String toString() {
        return host;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Server server = (Server) o;

        if (host != null ? !host.equals(server.host) : server.host != null) return false;
        if (keyPath != null ? !keyPath.equals(server.keyPath) : server.keyPath != null) return false;
        if (login != null ? !login.equals(server.login) : server.login != null) return false;
        if (passPhrase != null ? !passPhrase.equals(server.passPhrase) : server.passPhrase != null) return false;
        if (password != null ? !password.equals(server.password) : server.password != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (login != null ? login.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (keyPath != null ? keyPath.hashCode() : 0);
        result = 31 * result + (passPhrase != null ? passPhrase.hashCode() : 0);
        return result;
    }
}
