package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class RootHandler extends AbstractHttpHandler {
    private static final String WELCOME_MESSAGE = "Welcome to our server";
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(HTTP_OK, WELCOME_MESSAGE.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        OutputStream responseBody = httpExchange.getResponseBody();

        responseBody.write(WELCOME_MESSAGE.getBytes(UTF8));

        closeResponseBodyStream(responseBody);
    }
}
