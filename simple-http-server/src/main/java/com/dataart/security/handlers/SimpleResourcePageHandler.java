package com.dataart.security.handlers;

import com.dataart.security.authorization.AuthorizationManager;
import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.permissions.SimpleResourcePermission;
import com.dataart.security.users.User;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;
import org.rythmengine.Rythm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.TEXT_HTML_CHARSET_UTF_8;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;

public class SimpleResourcePageHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");
    private static final String TEMPLATE = "simple_resource_page.html";
    private static final AuthorizationManager AUTHORIZATION_MANAGER = AuthorizationManager.getInstance();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        OutputStream responseBody = httpExchange.getResponseBody();
        InputStream requestBody = httpExchange.getRequestBody();

        String userLogin = httpExchange.getPrincipal() != null ? httpExchange.getPrincipal().getUsername() : null;
        User user = DATA_BASE.getUserByLogin(userLogin);

        if (user == null) {
            Logger.warn("Some issues with auth userLogin={}", userLogin);

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        boolean canRead = AUTHORIZATION_MANAGER.isPermitted(user, SimpleResourcePermission.READ);

        if (!canRead) {
            Logger.warn("User without required permissions tried to access simple resource. userLogin={}", userLogin);

            badRequest(HTTP_FORBIDDEN, httpExchange);

            return;
        }

        boolean canCreate = AUTHORIZATION_MANAGER.isPermitted(user, SimpleResourcePermission.CREATE);
        boolean canUpdate = AUTHORIZATION_MANAGER.isPermitted(user, SimpleResourcePermission.UPDATE);
        boolean canDelete = AUTHORIZATION_MANAGER.isPermitted(user, SimpleResourcePermission.DELETE);

        String response = Rythm.render(TEMPLATE, canRead, canCreate, canUpdate, canDelete);

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, TEXT_HTML_CHARSET_UTF_8);
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        closeRequestBodyStream(requestBody);

        sendResponse(response, responseBody);
    }
}
