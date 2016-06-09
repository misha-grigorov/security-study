package com.dataart.security.handlers.oauth;

import com.dataart.security.oauth.OAuthAuthorizationClientCode;
import com.dataart.security.oauth.OAuthErrorType;
import com.dataart.security.services.OAuthService;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;

public class OAuthRedirectHandler extends AbstractBaseOAuthHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final OAuthService OAUTH_SERVICE = OAuthService.getInstance();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        if (!FORMS_URL_ENCODED.equals(httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
            errorResponse(httpExchange, OAuthErrorType.INVALID_REQUEST);

            return;
        }

        String request = Utils.readRequestBody(httpExchange.getRequestBody());
        Map<String, String> params = Utils.parseQuery(request);
        String action = params.get("action");

        if (!checkActionValid(action)) {
            errorResponse(httpExchange, OAuthErrorType.INVALID_REQUEST);

            return;
        }

        OAuthAuthorizationClientCode clientCode = OAUTH_SERVICE.handleUserAction(params, "Allow".equals(action));

        if (clientCode.getErrorType() != OAuthErrorType.NONE) {
            errorResponse(httpExchange, OAuthErrorType.INVALID_REQUEST);

            return;
        }

        Headers responseHeaders = httpExchange.getResponseHeaders();

        responseHeaders.add("Pragma", "no-cache");
        responseHeaders.add("Cache-Control", "no-store");

        redirect(clientCode.getUserActionBasedRedirectUri(), httpExchange);
    }

    private boolean checkActionValid(String action) {
        return "Allow".equals(action) || "Deny".equals(action);
    }
}
