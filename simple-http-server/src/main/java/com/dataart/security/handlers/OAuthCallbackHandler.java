package com.dataart.security.handlers;

import com.dataart.security.utils.GoogleOAuthAccessTokenResponse;
import com.dataart.security.utils.GoogleOAuthUtils;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OAuthCallbackHandler extends AbstractHttpHandler {
    public static final List<String> ALLOWED_METHODS = Arrays.asList("GET");

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        String rawQuery = httpExchange.getRequestURI().getRawQuery();
        Map<String, String> queryMap = Utils.parseQuery(rawQuery);

        if (queryMap.containsKey("error")) {
            String error = queryMap.get("error");

            Logger.info("Response from Google contains error {}", error);

            redirect("/oauth-page?error=" + error, httpExchange);

            return;
        }

        if (!checkQueryParams(queryMap)) {
            Logger.info("Bad response from Google - missing required params. Rejecting!");

            redirect("/oauth-page?error=invalid_response", httpExchange);

            return;
        }

        GoogleOAuthAccessTokenResponse response = GoogleOAuthUtils.getAccessToken(queryMap.get("code"));
        String labelsJson = GoogleOAuthUtils.getGmailLabelsRaw(response);

        GoogleOAuthUtils.putLabels(response.getAccessToken(), labelsJson);

        redirect("/oauth-page?labels=true", httpExchange);
    }

    private boolean checkQueryParams(Map<String, String> queryMap) {
        // TODO: check state is valid
        return queryMap.containsKey("state") && queryMap.containsKey("code") && queryMap.get("code") != null;
    }
}
