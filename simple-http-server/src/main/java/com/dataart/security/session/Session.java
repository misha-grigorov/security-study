package com.dataart.security.session;

import com.dataart.security.users.User;
import com.dataart.security.utils.Utils;

public class Session {
    private User user;
    private String userAgent;
    private String ipAddress;
    private String token;
    private long lastSeen;

    private Session() {
    }

    public Session(User user, String userAgent, String ipAddress) {
        this.user = user;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.lastSeen = System.currentTimeMillis();
        this.token = updateToken();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String updateToken() {
        String randomValue = Utils.generateSecureRandom();

        setToken(randomValue);

        return randomValue;
    }
}
