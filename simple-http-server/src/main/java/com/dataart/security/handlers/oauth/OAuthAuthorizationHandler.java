package com.dataart.security.handlers.oauth;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.oauth.OAuthClientAuthorizationRequest;
import com.dataart.security.oauth.OAuthErrorType;
import com.dataart.security.services.OAuthService;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;
import org.rythmengine.Rythm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.TEXT_HTML_CHARSET_UTF_8;
import static java.net.HttpURLConnection.HTTP_OK;

public class OAuthAuthorizationHandler extends AbstractBaseOAuthHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");
    private static final OAuthService OAUTH_SERVICE = OAuthService.getInstance();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        String rawQuery = httpExchange.getRequestURI().getRawQuery();

        User user = DATA_BASE.getUserByLogin(httpExchange.getPrincipal().getUsername());

        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            Logger.warn("Some issues with authenticator. user=null");

            errorResponse(httpExchange, OAuthErrorType.INVALID_REQUEST);

            return;
        }

        OAuthClientAuthorizationRequest oAuthClientAuthorizationRequest = OAUTH_SERVICE.newClientRequest(rawQuery, user);
        OAuthErrorType errorType = oAuthClientAuthorizationRequest.getErrorType();

        if (errorType != OAuthErrorType.NONE) {
            errorResponse(httpExchange, errorType);

            return;
        }

        String response = Rythm.render("oauth/oauth_auth_page.html", oAuthClientAuthorizationRequest.getClientInfo(),
                oAuthClientAuthorizationRequest.getPermissions(), oAuthClientAuthorizationRequest.getState());

        Headers responseHeaders = httpExchange.getResponseHeaders();

        responseHeaders.add(CONTENT_TYPE, TEXT_HTML_CHARSET_UTF_8);
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        sendResponse(response, httpExchange.getResponseBody());
    }
}
