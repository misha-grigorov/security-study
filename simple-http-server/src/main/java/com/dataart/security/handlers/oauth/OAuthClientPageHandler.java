package com.dataart.security.handlers.oauth;

import com.dataart.security.handlers.AbstractHttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OAuthClientPageHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {

    }
}
