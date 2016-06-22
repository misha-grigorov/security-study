package com.dataart.security.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.APPLICATION_JSON;
import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_OK;

public class CorsApiHandler extends AbstractHttpHandler {
    private static final String DEFAULT_RESPONSE = "{ \"message\": \"hello\" }";
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        Headers responseHeaders = httpExchange.getResponseHeaders();

        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Cache-Control", "no-store");
        responseHeaders.add("Pragma", "no-cache");
        responseHeaders.add(CONTENT_TYPE, APPLICATION_JSON);
        httpExchange.sendResponseHeaders(HTTP_OK, DEFAULT_RESPONSE.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        sendResponse(DEFAULT_RESPONSE, httpExchange.getResponseBody());
    }
}
