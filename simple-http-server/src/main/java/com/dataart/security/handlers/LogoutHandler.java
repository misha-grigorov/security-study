package com.dataart.security.handlers;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.session.SessionManager;
import com.dataart.security.users.User;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.COOKIE_KEY;
import static com.dataart.security.utils.Utils.SERVER_SESSION_KEY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;

public class LogoutHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final SessionManager SESSION_MANAGER = SessionManager.getInstance();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        String userLogin = httpExchange.getPrincipal() != null ? httpExchange.getPrincipal().getUsername() : null;

        User user = DATA_BASE.getUserByLogin(userLogin);

        if (user == null) {
            Logger.warn("Some issues with auth userLogin={}", userLogin);

            badRequest(HTTP_BAD_REQUEST, httpExchange);
        }

        String sessionToken = SESSION_MANAGER.getSessionTokenFromCookie(httpExchange.getRequestHeaders().getFirst(COOKIE_KEY));

        SESSION_MANAGER.removeSession(sessionToken);

        httpExchange.getResponseHeaders().set("Set-Cookie", SERVER_SESSION_KEY + "" +
                "; path=/; domain=127.0.0.1; httponly");
        httpExchange.getResponseHeaders().set("Location", "/login-page");
        httpExchange.sendResponseHeaders(HTTP_MOVED_TEMP, -1);

        closeRequestBodyStream(httpExchange.getRequestBody());
        closeResponseBodyStream(httpExchange.getResponseBody());
    }
}
