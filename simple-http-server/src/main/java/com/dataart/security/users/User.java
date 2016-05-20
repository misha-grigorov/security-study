package com.dataart.security.users;

import org.mindrot.jbcrypt.BCrypt;

public class User {
    private String login;
    private String email;
    private String password;
    private String ipAddress;
    private String userAgent;
    private String session;
    private String salt;
    private long lastSeen;

    public User() {
        this.salt = BCrypt.gensalt();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, salt);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String generateSession() {
        this.session = BCrypt.hashpw(ipAddress + userAgent + login + lastSeen, salt);

        return this.session;
    }
}
