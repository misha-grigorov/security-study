package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.pmw.tinylog.Logger;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

public class AuthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Logger.info("Success");

        httpExchange.sendResponseHeaders(HTTP_OK, -1);

        httpExchange.getRequestBody().close();
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }
}
