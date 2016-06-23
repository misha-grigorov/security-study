package com.dataart.security.handlers.oauth;

import com.dataart.security.oauth.OAuthErrorType;
import com.dataart.security.oauth.OAuthJwtAccessToken;
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
import static java.net.HttpURLConnection.HTTP_OK;

public class OAuthAccessTokenHandler extends AbstractBaseOAuthHandler {
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

        String request = Utils.readFromRequestBody(httpExchange.getRequestBody());
        Map<String, String> params = Utils.parseQuery(request);

        OAuthJwtAccessToken accessToken = OAUTH_SERVICE.newAccessTokenRequest(params, httpExchange.getPrincipal());

        if (accessToken.getErrorType() != OAuthErrorType.NONE) {
            errorResponse(httpExchange, accessToken.getErrorType());

            return;
        }

        Headers responseHeaders = httpExchange.getResponseHeaders();

        responseHeaders.add(CONTENT_TYPE, Utils.APPLICATION_JSON);

        String response = accessToken.toJsonString();

        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        sendResponse(response, httpExchange.getResponseBody());
    }
}
