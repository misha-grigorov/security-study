package com.dataart.security.handlers;

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
import org.rythmengine.Rythm;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;
import static com.dataart.security.utils.Utils.TEXT_HTML_CHARSET_UTF_8;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RegistrationHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST");
    private static final SessionManager SESSION_MANAGER = SessionManager.getInstance();
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance(false, true);
    private static final String REGISTER_ACTION = "register";
    private static final String RECOVER_ACTION = "recover";

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        Session session = SESSION_MANAGER.getSessionIfAuthenticated(httpExchange);

        if (session != null && session.getUser().getStatus() == UserStatus.ACTIVE) {
            Logger.info("Authenticated user tried to register/recover. user={}", session.getUser().getLogin());

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

        User newUser = RegistrationService.checkRegistrationToken(tokenId, false);

        if (newUser == null) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        if (newUser.getStatus() != UserStatus.ACTIVE) {
            newUser.setStatus(UserStatus.RESET_PASSWORD);
        }

        String response = Rythm.render("reset_password.html", tokenId);

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, TEXT_HTML_CHARSET_UTF_8);
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        sendResponse(response, httpExchange.getResponseBody());
    }

    private void handlePostData(HttpExchange httpExchange) throws IOException {
        if (!FORMS_URL_ENCODED.equals(httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        String request = Utils.readFromRequestBody(httpExchange.getRequestBody());
        Map<String, String> params = Utils.parseQuery(request);
        String email = params.get("email");
        String action = params.get("action");

        if (!isValidAction(action) || !EMAIL_VALIDATOR.isValid(email)) {
            Logger.info("Invalid input was used for registration/recovery. email={}, action={}", email, action);

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        RegistrationToken registrationToken = null;

        if (action.equals(REGISTER_ACTION)) {
            registrationToken = RegistrationService.registerNewUser(email);
        }

        if (action.equals(RECOVER_ACTION)) {
            registrationToken = RegistrationService.recoverUser(email);
        }

        if (registrationToken == null) {
            Logger.info("invalid registration token. action={}, email={}", action, email);

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        String registrationLink = "http://127.0.0.1:55555/register?token=" +
                URLEncoder.encode(registrationToken.getTokenId(), UTF_8.name());

        NotificationService.sendEmail(email, "Registration information: user login=" +
                registrationToken.getUser().getLogin() + "\nregistration link=" + registrationLink);

        httpExchange.sendResponseHeaders(HTTP_OK, -1);
        closeResponseBodyStream(httpExchange.getResponseBody());
    }

    private boolean isValidAction(String action) {
        return action != null && (action.equals(REGISTER_ACTION) || action.equals(RECOVER_ACTION));
    }
}
