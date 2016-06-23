package com.dataart.security.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.TEXT_HTML_CHARSET_UTF_8;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public abstract class SingleHtmlPageHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");

    protected abstract String getResponse(HttpExchange httpExchange) throws IOException;

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        OutputStream responseBody = httpExchange.getResponseBody();
        InputStream requestBody = httpExchange.getRequestBody();
        String response = getResponse(httpExchange);

        if (response == null) {
            Logger.warn("Unexpected issue with html page handler");

            badRequest(HTTP_NOT_FOUND, httpExchange);

            return;
        }

        Headers responseHeaders = httpExchange.getResponseHeaders();

        responseHeaders.add(CONTENT_TYPE, TEXT_HTML_CHARSET_UTF_8);
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        closeRequestBodyStream(requestBody);

        sendResponse(response, responseBody);
    }
}
