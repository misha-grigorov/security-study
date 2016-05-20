package com.dataart.security;

import com.dataart.security.users.User;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final ConcurrentHashMap<String, User> sessionMap = new ConcurrentHashMap<>();

    private static class SessionManagerHolder {
        private static final SessionManager HOLDER_INSTANCE = new SessionManager();
    }

    public static SessionManager getInstance() {
        return SessionManagerHolder.HOLDER_INSTANCE;
    }

    private SessionManager() {
    }

    public void newSession(String token, User user) {
        if (token == null) {
            return;
        }

        sessionMap.put(token, user);
    }

    public boolean isValidSession(String token) {
        return token != null && sessionMap.containsKey(token);

    }

    public User getSessionByToken(String token) {
        if (token == null) {
            return null;
        }

        return sessionMap.get(token);
    }

    public void sessionExpired(String token) {
        if (token == null) {
            return;
        }

        sessionMap.remove(token);
    }
}
