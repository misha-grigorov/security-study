package com.dataart.security.session;

import com.dataart.security.users.User;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static final int SESSION_EXPIRED_TIMEOUT_MILLIS = 300000; // 5 minutes
    private static final Map<String, Session> sessionMap = new HashMap<>();

    private static class SessionManagerHolder {
        private static final SessionManager HOLDER_INSTANCE = new SessionManager();
    }

    public static SessionManager getInstance() {
        return SessionManagerHolder.HOLDER_INSTANCE;
    }

    private SessionManager() {
    }

    public synchronized void newSession(Session newSession) {
        Logger.info("New session generated for user={}, ip={}, user-agent={}", newSession.getUser().getLogin(),
                newSession.getIpAddress(), newSession.getUserAgent());

        sessionMap.put(newSession.getToken(), newSession);
    }

    public synchronized boolean isValidSession(String token) {
        if (token == null) {
            return false;
        }

        Session session = sessionMap.get(token);

        if (session == null) {
            return false;
        }

        if (System.currentTimeMillis() - session.getLastSeen() > SESSION_EXPIRED_TIMEOUT_MILLIS) {
            Logger.info("Session expired user={}, ip={}, user-agent={}", session.getUser().getLogin(),
                    session.getIpAddress(), session.getUserAgent());
            sessionMap.remove(token);

            return false;
        }

        return true;
    }

    public synchronized Session getSessionByToken(String token) {
        if (isValidSession(token)) {
            return sessionMap.get(token);
        }

        return null;
    }

    public synchronized void updateSession(Session session, long lastSeen) {
        String previousToken = session.getToken();
        String newToken = session.updateToken(lastSeen);

        sessionMap.remove(previousToken);
        sessionMap.put(newToken, session);
    }

    public synchronized void clearUserSessions(User user) {
        sessionMap.values().removeIf(session -> session.getUser().equals(user));
    }
}
