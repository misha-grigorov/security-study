package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.FORMS_URL_ENCODED;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class AuthHandler extends AbstractHttpHandler {
    public static final List<String> ALLOWED_METHODS = Arrays.asList("POST");

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        if (!FORMS_URL_ENCODED.equals(httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> authParams = (Map<String, String>) httpExchange.getAttribute("auth-params");
        String redirectLocation = null;

        if (authParams != null && authParams.get("continue") != null) {
            String continueRedirectValue = StringEscapeUtils.unescapeHtml4(authParams.get("continue"));

            // probably need to add more sophisticated check for new location
            if (continueRedirectValue != null && !continueRedirectValue.isEmpty()) {
                redirectLocation = continueRedirectValue;
            }

            httpExchange.setAttribute("auth-params", null);
        }

        redirect(redirectLocation != null ? redirectLocation : "/", httpExchange);
    }
}
