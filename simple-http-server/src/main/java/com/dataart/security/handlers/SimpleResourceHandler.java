package com.dataart.security.handlers;

import com.dataart.security.authorization.AuthorizationManager;
import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.permissions.SimpleResourcePermission;
import com.dataart.security.users.User;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;

public class SimpleResourceHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final AuthorizationManager AUTHORIZATION_MANAGER = AuthorizationManager.getInstance();
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

            return;
        }

        boolean canUpdate = AUTHORIZATION_MANAGER.isPermitted(user, SimpleResourcePermission.UPDATE);

        if (!canUpdate) {
            Logger.warn("User without required permissions tried to access simple resource. userLogin={}", userLogin);

            badRequest(HTTP_FORBIDDEN, httpExchange);

            return;
        }

        httpExchange.sendResponseHeaders(HTTP_OK, -1);

        closeRequestBodyStream(httpExchange.getRequestBody());
        closeResponseBodyStream(httpExchange.getResponseBody());
    }
}
