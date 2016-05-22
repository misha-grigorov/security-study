package com.dataart.security.handlers;

import com.dataart.security.AuthMetricManager;
import com.dataart.security.services.NotificationService;
import com.dataart.security.services.RegistrationService;
import com.dataart.security.services.RegistrationToken;
import com.dataart.security.session.Session;
import com.dataart.security.session.SessionManager;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.validator.routines.EmailValidator;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;
import static com.dataart.security.utils.Utils.USER_AGENT;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RegistrationHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST");
    private static final SessionManager SESSION_MANAGER = SessionManager.getInstance();
    private static final AuthMetricManager AUTH_METRIC_MANAGER = AuthMetricManager.getInstance();
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance(false, true);

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        Session session = SESSION_MANAGER.getSessionIfAuthenticated(httpExchange);

        if (session != null) {
            Logger.info("Authenticated user tried to register. user={}", session.getUser().getLogin());

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        String requestMethod = httpExchange.getRequestMethod();

        if (requestMethod.equals("POST")) {
            handlePostData(httpExchange);
        } else if (requestMethod.equals("GET")) {
            handleRegistrationTokenLink(httpExchange);
        }
    }

    private void handleRegistrationTokenLink(HttpExchange httpExchange) throws IOException {
        Map<String, String> queryParams = Utils.parseQuery(httpExchange.getRequestURI().getQuery());
        String tokenId = queryParams.get("token");

        User newUser = RegistrationService.checkRegistrationToken(tokenId);

        if (newUser == null) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        newUser.setStatus(UserStatus.ACTIVE);

        Session newSession = new Session(newUser, httpExchange.getRemoteAddress().getHostString(),
                httpExchange.getRequestHeaders().getFirst(USER_AGENT));

        SESSION_MANAGER.newSession(newSession);
        AUTH_METRIC_MANAGER.loginSuccess(newUser);

        httpExchange.getResponseHeaders().set("Location", "/change-password-page");
        httpExchange.sendResponseHeaders(HTTP_MOVED_TEMP, -1);

        closeRequestBodyStream(httpExchange.getRequestBody());
        closeResponseBodyStream(httpExchange.getResponseBody());
    }

    private void handlePostData(HttpExchange httpExchange) throws IOException {
        if (!FORMS_URL_ENCODED.equals(httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        String request = Utils.readRequestBody(httpExchange.getRequestBody());
        Map<String, String> params = Utils.parseQuery(request);
        String email = params.get("email");

        if (!EMAIL_VALIDATOR.isValid(email)) {
            Logger.info("Invalid email address was used for registration. email={}", email);

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        RegistrationToken registrationToken = RegistrationService.registerNewUser(email);
        String registrationLink = "http://127.0.0.1:55555/register?token=" +
                URLEncoder.encode(registrationToken.getTokenId(), UTF_8.name());

        NotificationService.sendEmail(email, "Registration information: user login=" +
                registrationToken.getUser().getLogin() + "\nregistration link=" + registrationLink);

        httpExchange.sendResponseHeaders(HTTP_CREATED, -1);
        closeResponseBodyStream(httpExchange.getResponseBody());
    }
}
