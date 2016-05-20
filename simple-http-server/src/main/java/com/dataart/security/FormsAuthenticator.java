package com.dataart.security;

import com.dataart.security.users.User;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

import static com.dataart.security.handlers.AbstractHttpHandler.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;

public class FormsAuthenticator extends Authenticator {
    public static final int SESSION_EXPIRED_TIMEOUT_MILLIS = 5 * 60 * 1000; // 5 minutes
    public static final String REALM = "Forms-realm";
    public static final String COOKIE_KEY = "Cookie";
    public static final String SERVER_SESSION_KEY = "server-session=";

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        String requestMethod = httpExchange.getRequestMethod();

        User user = getUserIfAuthenticated(httpExchange);

        if (user != null) {
            if (System.currentTimeMillis() - user.getLastSeen() > SESSION_EXPIRED_TIMEOUT_MILLIS) {
                SessionManager.getInstance().sessionExpired(user.getSession());
            } else {
                return new Success(new HttpPrincipal(user.getLogin(), REALM));
            }
        }

        if (requestMethod.equals("POST") && httpExchange.getRequestURI().getPath().equals("/auth")) {
            return tryToAuthenticate(httpExchange);
        }

        httpExchange.getResponseHeaders().set("Location", "/login-page");

        return new Retry(HTTP_MOVED_TEMP);
    }

    private User getUserIfAuthenticated(HttpExchange httpExchange) {
        Headers requestHeaders = httpExchange.getRequestHeaders();

        if (requestHeaders.containsKey(COOKIE_KEY)) {
            String cookie = requestHeaders.getFirst(COOKIE_KEY);

            if (cookie.contains(SERVER_SESSION_KEY)) {
                int startIndex = cookie.indexOf(SERVER_SESSION_KEY) + SERVER_SESSION_KEY.length();
                int endIndex = cookie.contains(";") ? cookie.indexOf(";", startIndex) : cookie.length();
                String sessionToken = cookie.substring(startIndex, endIndex);
                SessionManager sessionManager = SessionManager.getInstance();

                if (sessionManager.isValidSession(sessionToken)) {
                    return sessionManager.getSessionByToken(sessionToken);
                }
            }
        }

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

        if (login == null || password == null) {
            return new Retry(HTTP_OK);
        }

        User user = InMemoryUserDataBase.getInstance().getUserByLogin(login);

        if (user == null || !user.getLogin().equals(login)) {
            return new Retry(HTTP_OK);
        }

        if (BCrypt.checkpw(password, user.getPassword())) {
            user.setIpAddress(httpExchange.getRemoteAddress().getHostString());
            user.setUserAgent(requestHeaders.getFirst("User-Agent"));
            user.setLastSeen(System.currentTimeMillis());

            String newSessionToken = user.generateSession();
            SessionManager.getInstance().newSession(newSessionToken, user);

            httpExchange.getResponseHeaders().set("Set-Cookie", SERVER_SESSION_KEY + newSessionToken + "; path=/; domain=127.0.0.1; httponly");

            return new Success(new HttpPrincipal(login, REALM));
        } else {
            return new Retry(HTTP_OK);
        }
    }
}
