package com.dataart.security.session;

import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.dataart.security.utils.Utils.COOKIE_DELIMITER;
import static com.dataart.security.utils.Utils.COOKIE_KEY;
import static com.dataart.security.utils.Utils.SERVER_SESSION_KEY;
import static com.dataart.security.utils.Utils.USER_AGENT;

public class SessionManager {
    private static final int SESSION_EXPIRED_TIMEOUT_MILLIS = 900_000; // 15 minutes
    private static final Map<String, Session> SESSION_MAP = new HashMap<>();

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

        SESSION_MAP.put(newSession.getToken(), newSession);
    }

    public synchronized boolean isValidSession(String token) {
        if (token == null) {
            return false;
        }

        Session session = SESSION_MAP.get(token);

        if (session == null) {
            return false;
        }

        if (System.currentTimeMillis() - session.getLastSeen() > SESSION_EXPIRED_TIMEOUT_MILLIS) {
            Logger.info("Session expired user={}, ip={}, user-agent={}", session.getUser().getLogin(),
                    session.getIpAddress(), session.getUserAgent());
            SESSION_MAP.remove(token);

            return false;
        }

        return true;
    }

    public synchronized Session getSessionByToken(String token) {
        if (isValidSession(token)) {
            return SESSION_MAP.get(token);
        }

        return null;
    }

    public synchronized void updateSession(Session session, long lastSeen) {
        if (session == null) {
            return;
        }

        session.setLastSeen(lastSeen);

        String previousToken = session.getToken();
        String newToken = session.updateToken();

        SESSION_MAP.remove(previousToken);
        SESSION_MAP.put(newToken, session);
    }

    public synchronized boolean removeSession(String sessionToken) {
        if (sessionToken == null) {
            return false;
        }

        if (SESSION_MAP.containsKey(sessionToken)) {
            SESSION_MAP.remove(sessionToken);

            return true;
        }

        return false;
    }

    public synchronized void clearUserSessions(User user) {
        if (user == null) {
            return;
        }

        SESSION_MAP.values().removeIf(session -> session.getUser().equals(user));
    }

    public synchronized Session getSessionIfAuthenticated(HttpExchange httpExchange) {
        Headers requestHeaders = httpExchange.getRequestHeaders();

        if (requestHeaders.containsKey(COOKIE_KEY)) {
            String sessionToken = getSessionTokenFromCookie(requestHeaders.getFirst(COOKIE_KEY));
            Session session = getSessionByToken(sessionToken);

            if (session == null) {
                Logger.info("session was not found for specified sessionId");

                return null;
            }

            if (!session.getIpAddress().equals(httpExchange.getRemoteAddress().getHostString())) {
                Logger.info("session contains a different ip address");

                return null;
            }

            if (!session.getUserAgent().equals(requestHeaders.getFirst(USER_AGENT))) {
                Logger.info("session contains a different user-agent");

                return null;
            }

            if (session.getUser().getStatus() == UserStatus.BLOCKED) {
                Logger.info("session of the blocked user. userLogin={}", session.getUser().getLogin());

                return null;
            }

            Logger.info("userLogin={} session found", session.getUser().getLogin());

            return session;
        }

        Logger.info("session was not found for specified sessionId");

        return null;
    }

    public synchronized String getSessionTokenFromCookie(String cookie) {
        String[] cookieTokens = cookie.split(COOKIE_DELIMITER);
        String sessionToken = null;

        for (String token : cookieTokens) {
            if (token.contains(SERVER_SESSION_KEY)) {
                sessionToken = token;
                break;
            }
        }

        if (sessionToken == null) {
            return null;
        }

        int startIndex = sessionToken.indexOf(SERVER_SESSION_KEY) + SERVER_SESSION_KEY.length();
        int endIndex = sessionToken.length();

        return sessionToken.substring(startIndex, endIndex);
    }

    public synchronized boolean checkCsrf(String csrf, HttpExchange httpExchange) {
        if (csrf == null || httpExchange == null) {
            return false;
        }

        Session session = getSessionIfAuthenticated(httpExchange);

        if (session == null || session.getCsrf() == null || !csrf.equals(session.getCsrf())) {
            return false;
        }

        return true;
    }
}
