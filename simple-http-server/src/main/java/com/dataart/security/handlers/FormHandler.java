package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FormHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        if (!"application/x-www-form-urlencoded".equals(httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        InputStream requestBody = httpExchange.getRequestBody();
        OutputStream responseBody = httpExchange.getResponseBody();

        String request = URLDecoder.decode(readRequestBody(requestBody), UTF_8.name());

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(HTTP_OK, request.length());

        sendResponse(request, responseBody);
    }

    protected String readRequestBody(InputStream requestBody) {
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream stream = requestBody) {
            String line = null;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, UTF_8));

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            Logger.warn(e.getMessage());
        }

        return stringBuilder.toString();
    }
}
