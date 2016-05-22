package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_OK;

public abstract class SingleHtmlPageHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");

    protected abstract String getResponse();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        OutputStream responseBody = httpExchange.getResponseBody();
        InputStream requestBody = httpExchange.getRequestBody();

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, "text/html; charset=utf-8");
        httpExchange.sendResponseHeaders(HTTP_OK, getResponse().length());

        closeRequestBodyStream(requestBody);

        sendResponse(getResponse(), responseBody);
    }
}
