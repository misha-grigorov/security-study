package com.dataart.security.handlers.oauth;

import com.dataart.security.handlers.AbstractHttpHandler;
import com.dataart.security.oauth.OAuthErrorType;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.TEXT_PLAIN;

public abstract class AbstractBaseOAuthHandler extends AbstractHttpHandler {

    protected void errorResponse(HttpExchange httpExchange, OAuthErrorType errorType) throws IOException {
        String response = errorType.toString();

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, TEXT_PLAIN);
        httpExchange.sendResponseHeaders(errorType.getResponseCode(), response.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        sendResponse(response, httpExchange.getResponseBody());
    }
}
