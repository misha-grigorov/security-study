package com.dataart.security.authenticators;

import com.dataart.security.AuthMetricManager;
import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.Utils;
import com.dataart.security.session.Session;
import com.dataart.security.session.SessionManager;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.mindrot.jbcrypt.BCrypt;
import org.pmw.tinylog.Logger;

import java.util.Map;

import static com.dataart.security.handlers.AbstractHttpHandler.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class FormsAuthenticator extends Authenticator {
    private static final String USER_AGENT = "User-Agent";
    private static final String REALM = "Forms-realm";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SERVER_SESSION_KEY = "id=";
    private static final SessionManager SESSION_MANAGER = SessionManager.getInstance();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();
    private static final AuthMetricManager AUTH_METRIC_MANAGER = AuthMetricManager.getInstance();

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        String requestMethod = httpExchange.getRequestMethod();
        Headers responseHeaders = httpExchange.getResponseHeaders();

        Session session = getUserSessionIfAuthenticated(httpExchange);

        if (session != null) {
            SESSION_MANAGER.updateSession(session, System.currentTimeMillis());

            return successAuth(responseHeaders, session.getToken(), session.getUser().getLogin());
        }

        if (requestMethod.equals("POST") && httpExchange.getRequestURI().getPath().equals("/auth")) {
            return tryToAuthenticate(httpExchange);
        }

        return redirectToLoginPage(responseHeaders);
    }

    private Session getUserSessionIfAuthenticated(HttpExchange httpExchange) {
        Headers requestHeaders = httpExchange.getRequestHeaders();

        if (requestHeaders.containsKey(COOKIE_KEY)) {
            String cookie = requestHeaders.getFirst(COOKIE_KEY);

            if (cookie.contains(SERVER_SESSION_KEY)) {
                int startIndex = cookie.indexOf(SERVER_SESSION_KEY) + SERVER_SESSION_KEY.length();
                int endIndex = cookie.contains(";") ? cookie.indexOf(";", startIndex) : cookie.length();
                String sessionToken = cookie.substring(startIndex, endIndex);

                Session session = SESSION_MANAGER.getSessionByToken(sessionToken);

                if (session == null) {
                    return null;
                }

                if (!session.getIpAddress().equals(httpExchange.getRemoteAddress().getHostString())) {
                    return null;
                }

                if (!session.getUserAgent().equals(requestHeaders.getFirst(USER_AGENT))) {
                    return null;
                }

                Logger.info("user={} session found", session.getUser());

                return session;
            }
        }

        Logger.info("session was not found for specified sessionId");

        return null;
    }

    private Result tryToAuthenticate(HttpExchange httpExchange) {
        Headers requestHeaders = httpExchange.getRequestHeaders();

        if (!"application/x-www-form-urlencoded".equals(requestHeaders.getFirst(CONTENT_TYPE))) {
            return new Failure(HTTP_BAD_REQUEST);
        }

        // do not close requestBody stream
        String request = Utils.readRequestBody(httpExchange.getRequestBody(), false);
        Map<String, String> params = Utils.parseQuery(request);
        String login = params.get("user");
        String password = params.get("pw");
        Headers responseHeaders = httpExchange.getResponseHeaders();

        if (login == null || password == null) {
            return retryAuth(responseHeaders);
        }

        User user = DATA_BASE.getUserByLogin(login);

        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            Logger.info("Log in failed: invalid username or non-active user");

            return retryAuth(responseHeaders);
        }

        if (BCrypt.checkpw(password, user.getPassword())) {
            Session newSession = new Session(user, httpExchange.getRemoteAddress().getHostString(),
                    requestHeaders.getFirst(USER_AGENT));

            SESSION_MANAGER.newSession(newSession);
            AUTH_METRIC_MANAGER.loginSuccess(user);

            return successAuth(responseHeaders, newSession.getToken(), login);
        } else {
            UserStatus userStatus = AUTH_METRIC_MANAGER.loginFail(user);

            if (userStatus == UserStatus.BLOCKED) {
                SESSION_MANAGER.clearUserSessions(user);
            }

            return retryAuth(responseHeaders);
        }
    }

    private Result retryAuth(Headers responseHeaders) {
        responseHeaders.set("WWW-Authenticate", "Forms");

        return new Retry(HTTP_UNAUTHORIZED);
    }

    private Result successAuth(Headers responseHeaders, String token, String userLogin) {
        responseHeaders.set("Set-Cookie", SERVER_SESSION_KEY + token +
                "; path=/; domain=127.0.0.1; httponly");

        return new Success(new HttpPrincipal(userLogin, REALM));
    }

    private Result redirectToLoginPage(Headers responseHeaders) {
        responseHeaders.set("Location", "/login-page");

        return new Retry(HTTP_MOVED_TEMP);
    }
}
