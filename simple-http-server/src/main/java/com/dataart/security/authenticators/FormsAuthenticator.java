package com.dataart.security.authenticators;

import com.dataart.security.auth.AuthMetricManager;
import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.services.RegistrationService;
import com.dataart.security.utils.Utils;
import com.dataart.security.session.Session;
import com.dataart.security.session.SessionManager;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.pmw.tinylog.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;
import static com.dataart.security.utils.Utils.SERVER_SESSION_KEY;
import static com.dataart.security.utils.Utils.USER_AGENT;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class FormsAuthenticator extends Authenticator {
    private static final String REALM = "Forms-realm";
    private static final SessionManager SESSION_MANAGER = SessionManager.getInstance();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();
    private static final AuthMetricManager AUTH_METRIC_MANAGER = AuthMetricManager.getInstance();

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        String requestMethod = httpExchange.getRequestMethod();
        Headers responseHeaders = httpExchange.getResponseHeaders();

        Session session = SESSION_MANAGER.getSessionIfAuthenticated(httpExchange);

        if (session != null) {
            return successAuth(responseHeaders, session.getToken(), session.getUser().getLogin());
        }

        String path = httpExchange.getRequestURI().getPath();

        if (requestMethod.equals("POST")) {

            if (path.equals("/auth")) {
                return tryToAuthenticate(httpExchange);
            } else if (path.equals("/change-password")) {
                return checkRegistrationToken(httpExchange);
            }
        }

        return redirectToLoginPage(responseHeaders, path, httpExchange.getRequestURI().getQuery());
    }

    private Result checkRegistrationToken(HttpExchange httpExchange) {
        Headers requestHeaders = httpExchange.getRequestHeaders();

        if (!FORMS_URL_ENCODED.equals(requestHeaders.getFirst(CONTENT_TYPE))) {
            return new Failure(HTTP_BAD_REQUEST);
        }

        String request = Utils.readRequestBody(httpExchange.getRequestBody(), false);
        Map<String, String> params = Utils.parseQuery(request);
        String tokenId = params.get("tokenId");
        User user = RegistrationService.checkRegistrationToken(tokenId, true);

        if (user == null) {
            return new Failure(HTTP_BAD_REQUEST);
        }

        String referer = requestHeaders.getFirst("Referer");

        if (referer == null || !referer.contains(tokenId)) {
            return new Failure(HTTP_BAD_REQUEST);
        }

        httpExchange.setAttribute("change-password-params", params);

        return new Success(new HttpPrincipal(user.getLogin(), REALM));
    }

    private Result tryToAuthenticate(HttpExchange httpExchange) {
        Headers requestHeaders = httpExchange.getRequestHeaders();

        if (!FORMS_URL_ENCODED.equals(requestHeaders.getFirst(CONTENT_TYPE))) {
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

        if (!AUTH_METRIC_MANAGER.checkUserStatus(user)) {
            return retryAuth(responseHeaders);
        }

        if (Utils.checkPassword(password, user)) {
            Session newSession = new Session(user, requestHeaders.getFirst(USER_AGENT),
                    httpExchange.getRemoteAddress().getHostString());

            SESSION_MANAGER.newSession(newSession);
            AUTH_METRIC_MANAGER.loginSuccess(user);

            httpExchange.setAttribute("auth-params", params);

            return successAuth(responseHeaders, newSession.getToken(), login);
        } else {
            UserStatus userStatus = AUTH_METRIC_MANAGER.loginFail(user);

            if (userStatus == UserStatus.SUSPENDED) {
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

    private Result redirectToLoginPage(Headers responseHeaders, String path, String query) {
        String redirectLocation = "/login-page";

        if (query != null) {
            String encodedContinuePath = null;

            try {
                encodedContinuePath = URLEncoder.encode(path + "?" + query, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                Logger.info("Failed to url-encode redirect url", e.getMessage());
            }

            redirectLocation = encodedContinuePath != null ? redirectLocation + "?continue=" + encodedContinuePath : redirectLocation;
        }

        responseHeaders.set("Location", redirectLocation);

        return new Retry(HTTP_MOVED_TEMP);
    }
}
