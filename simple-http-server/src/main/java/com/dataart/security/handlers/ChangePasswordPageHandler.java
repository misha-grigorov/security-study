package com.dataart.security.handlers;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.IOException;

public class ChangePasswordPageHandler extends SingleHtmlPageHandler {
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();
    private static final String CHANGE_PASSWORD_RESPONSE;
    private static final String RESET_PASSWORD_RESPONSE;

    static {
        CHANGE_PASSWORD_RESPONSE = Utils.readFromRequestBody(ChangePasswordPageHandler.class.getClassLoader().getResourceAsStream("change_password.html"));

        RESET_PASSWORD_RESPONSE = Utils.readFromRequestBody(ChangePasswordPageHandler.class.getClassLoader().getResourceAsStream("reset_password.html"));
    }

    @Override
    protected String getResponse(HttpExchange httpExchange) throws IOException {
        String userLogin = httpExchange.getPrincipal() != null ? httpExchange.getPrincipal().getUsername() : null;
        User user = DATA_BASE.getUserByLogin(userLogin);

        if (user == null) {
            Logger.warn("Some issues with auth userLogin={}", userLogin);

            return null;
        }

        if (user.getStatus() == UserStatus.RESET_PASSWORD) {
            return RESET_PASSWORD_RESPONSE;
        }

        return CHANGE_PASSWORD_RESPONSE;
    }
}
