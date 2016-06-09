package com.dataart.security.handlers;

import com.dataart.security.auth.AuthMetricManager;
import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.services.NotificationService;
import com.dataart.security.session.SessionManager;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class ChangePasswordHandler extends AbstractHttpHandler {
    private static final String SPECIAL_CHARACTERS = " /*!@#$%^&*()\"{}_[]|\\?/<>,.";
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();
    private static final SessionManager SESSION_MANAGER = SessionManager.getInstance();
    private static final AuthMetricManager AUTH_METRIC_MANAGER = AuthMetricManager.getInstance();

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

            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) httpExchange.getAttribute("change-password-params");

        if (params == null) {
            params = Utils.parseQuery(Utils.readRequestBody(httpExchange.getRequestBody(), false));
        } else {
            httpExchange.setAttribute("change-password-params", null);
        }

        if (params == null) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        closeRequestBodyStream(httpExchange.getRequestBody());

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
                break;
        }
    }

    private void handleReset(User user, Map<String, String> params, HttpExchange httpExchange) throws IOException {
        doHandle(user, params, httpExchange, false);
    }

    private void handleChange(User user, Map<String, String> params, HttpExchange httpExchange) throws IOException {
        doHandle(user, params, httpExchange, true);
    }

    private void doHandle(User user, Map<String, String> params, HttpExchange httpExchange, boolean checkOldPassword) throws IOException {
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

        if (checkOldPassword && !checkOldPassword(params, user, newPassword)) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            UserStatus userStatus = AUTH_METRIC_MANAGER.passwordChangeFail(user);

            if (userStatus == UserStatus.SUSPENDED) {
                SESSION_MANAGER.clearUserSessions(user);

                redirect("/login-page", "", httpExchange);

                return;
            }

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        user.setPassword(newPassword);
        user.setStatus(UserStatus.ACTIVE);
        AUTH_METRIC_MANAGER.passwordChangeSuccess(user);
        SESSION_MANAGER.clearUserSessions(user);

        NotificationService.sendEmail(user.getEmail(), "Your password was changed. Please, recover your password or " +
                "contact the administrator, if you think that it was a mistake.");

        redirect("/login-page", "", httpExchange);
    }

    private boolean checkOldPassword(Map<String, String> params, User user, String newPassword) {
        String oldPassword = params.get("old-password");

        if (oldPassword == null || newPassword.equals(oldPassword) ||
                !Utils.checkPassword(oldPassword, user)) {
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
