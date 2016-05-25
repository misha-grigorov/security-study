package com.dataart.security.handlers;

import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.rythmengine.Rythm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_OK;

public class RootHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");
    private static final String INDEX_TEMPLATE = "index.html";

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        String response = Rythm.render(INDEX_TEMPLATE, httpExchange.getPrincipal().getUsername());

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, Utils.TEXT_HTML_CHARSET_UTF_8);
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        sendResponse(response, httpExchange.getResponseBody());
    }
}
