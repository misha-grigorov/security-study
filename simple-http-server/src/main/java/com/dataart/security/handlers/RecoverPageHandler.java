package com.dataart.security.handlers;

import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;

public class RecoverPageHandler extends SingleHtmlPageHandler {
    private static final String HTML_RESPONSE;

    static {
        InputStream resourceAsStream = RecoverPageHandler.class.getClassLoader().getResourceAsStream("recovery_page.html");

        HTML_RESPONSE = Utils.readRequestBody(resourceAsStream);
    }

    @Override
    protected String getResponse(HttpExchange httpExchange) throws IOException {
        return HTML_RESPONSE;
    }
}
