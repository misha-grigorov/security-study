package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;

public class AuthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().set("Location", "/");
        httpExchange.sendResponseHeaders(HTTP_MOVED_TEMP, -1);

        httpExchange.getRequestBody().close();
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }
}
