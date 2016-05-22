package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class ChangePasswordHandler extends AbstractHttpHandler {
    @Override
    protected List<String> getAllowedMethods() {
        return null;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {

    }
}
