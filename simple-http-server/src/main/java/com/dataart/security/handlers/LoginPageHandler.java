package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.pmw.tinylog.Logger;
import org.rythmengine.Rythm;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoginPageHandler extends SingleHtmlPageHandler {

    public static final String CONTINUE_REDIRECT_KEY = "continue=";

    @Override
    protected String getResponse(HttpExchange httpExchange) {
        String rawQuery = httpExchange.getRequestURI().getRawQuery();
        String continueRedirectValue = null;

        if (rawQuery != null && rawQuery.startsWith(CONTINUE_REDIRECT_KEY)) {
            String buffer = rawQuery.substring(rawQuery.indexOf(CONTINUE_REDIRECT_KEY) + CONTINUE_REDIRECT_KEY.length());

            try {
                continueRedirectValue = StringEscapeUtils.escapeHtml4(URLDecoder.decode(buffer, UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                Logger.info(e.getMessage());
            }
        }

        return Rythm.render("forms_auth.html", continueRedirectValue);
    }
}
