package com.dataart.security.handlers;

import com.dataart.security.Utils;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class LoginPageHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");
    private static final String HTML_RESPONSE;

    static {
        InputStream resourceAsStream = LoginPageHandler.class.getClassLoader().getResourceAsStream("forms_auth.html");

        HTML_RESPONSE = Utils.readRequestBody(resourceAsStream);
    }

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        OutputStream responseBody = httpExchange.getResponseBody();
        InputStream requestBody = httpExchange.getRequestBody();

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, "text/html; charset=utf-8");
        httpExchange.sendResponseHeaders(HTTP_OK, HTML_RESPONSE.length());

        closeRequestBodyStream(requestBody);

        sendResponse(HTML_RESPONSE, responseBody);
    }
}
