package com.dataart.security.handlers;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

public class ChangePasswordHandler extends AbstractHttpHandler {
    private static final String SPECIAL_CHARACTERS = "/*!@#$%^&*()\"{}_[]|\\?/<>,.";
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        if (!FORMS_URL_ENCODED.equals(httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        String userLogin = httpExchange.getPrincipal() != null ? httpExchange.getPrincipal().getUsername() : null;
        User user = DATA_BASE.getUserByLogin(userLogin);

        if (user == null) {
            Logger.warn("Some issues with auth userLogin={}", userLogin);

            badRequest(HTTP_BAD_REQUEST, httpExchange);
        }

        String request = Utils.readRequestBody(httpExchange.getRequestBody());
        Map<String, String> params = Utils.parseQuery(request);

        String action = params.get("action");

        switch (action) {
            case "reset":
                handleReset(user, params, httpExchange);
                break;
            case "change":
                handleChange(user, params, httpExchange);
                break;
            default:
                Logger.warn("Invalid action appeared, possible break in {}", action);

                badRequest(HTTP_BAD_REQUEST, httpExchange);
        }
    }

    private void handleReset(User user, Map<String, String> params, HttpExchange httpExchange) throws IOException {
        String newPassword = params.get("new-password");
        String repeatNewPassword = params.get("repeat-new-password");

        if (newPassword == null || repeatNewPassword == null || !newPassword.equals(repeatNewPassword)) {
            Logger.info("Invalid passwords for reset");

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        if (!checkPasswordPolicy(newPassword)) {
            Logger.info("Bad new password");

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        user.setPassword(newPassword);
        user.setStatus(UserStatus.ACTIVE);
        Logger.info("Password was changed for user {}", user.getLogin());

        httpExchange.sendResponseHeaders(HTTP_OK, -1);

        closeRequestBodyStream(httpExchange.getRequestBody());
        closeResponseBodyStream(httpExchange.getResponseBody());
    }

    private void handleChange(User user, Map<String, String> params, HttpExchange httpExchange) throws IOException {
        if (!checkOldPassword(params, user)) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        handleReset(user, params, httpExchange);
    }

    private boolean checkOldPassword(Map<String, String> params, User user) {
        String oldPassword = params.get("old-password");

        if (oldPassword == null || !BCrypt.checkpw(oldPassword, user.getPassword())) {
            return false;
        }

        return true;
    }

    private boolean checkPasswordPolicy(String password) {
        if (password.length() < 10 && password.length() > 128) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char ch : password.toCharArray()) {
            if (Character.isDigit(ch)) {
                hasDigit = true;
            }

            if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            }

            if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            }

            if (SPECIAL_CHARACTERS.indexOf(ch) != -1) {
                hasSpecial = true;
            }
        }

        return hasDigit && hasLowercase && hasUppercase && hasSpecial;
    }
}
