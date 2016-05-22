package com.dataart.security.handlers;

import com.dataart.security.utils.Utils;

import java.io.InputStream;

public class LoginPageHandler extends SingleHtmlPageHandler {
    private static final String HTML_RESPONSE;

    static {
        InputStream resourceAsStream = LoginPageHandler.class.getClassLoader().getResourceAsStream("forms_auth.html");

        HTML_RESPONSE = Utils.readRequestBody(resourceAsStream);
    }

    @Override
    protected String getResponse() {
        return HTML_RESPONSE;
    }
}
