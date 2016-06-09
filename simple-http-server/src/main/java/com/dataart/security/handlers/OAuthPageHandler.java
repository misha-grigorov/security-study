package com.dataart.security.handlers;

import com.dataart.security.utils.GoogleOAuthUtils;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;
import org.rythmengine.Rythm;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OAuthPageHandler extends SingleHtmlPageHandler {
    private static final String TEMPLATE_HTML = "oauth_page.html";

    @Override
    protected String getResponse(HttpExchange httpExchange) {
        Map<String, String> queryMap = Utils.parseQuery(httpExchange.getRequestURI().getRawQuery());
        String error = queryMap.getOrDefault("error", null);
        boolean isLabels = Boolean.valueOf(queryMap.getOrDefault("labels", "false"));
        Map<String, Object> params = new HashMap<>(3);

        if (isLabels) {
            Collection<String> labels = GoogleOAuthUtils.getAvailableLabels();

            if (labels.isEmpty()) {
                return null;
            }

            params.put("error", null);
            params.put("authUrl", null);
            params.put("labels", labels);

            return Rythm.render(TEMPLATE_HTML, params);
        }

        try {
            String state = URLEncoder.encode(Utils.generateSecureRandom(), UTF_8.name());

            params.put("error", error);
            params.put("authUrl", GoogleOAuthUtils.formAuthUrlForClient(state));
            params.put("labels", null);

            return Rythm.render(TEMPLATE_HTML, params);
        } catch (UnsupportedEncodingException e) {
            Logger.warn(e.getMessage());

            return null;
        }
    }
}
