package com.dataart.security.handlers.oauth;

import com.dataart.security.authorization.AuthorizationManager;
import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.oauth.OAuthClientInfo;
import com.dataart.security.oauth.OAuthErrorType;
import com.dataart.security.permissions.SimpleResourcePermission;
import com.dataart.security.users.User;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.TEXT_PLAIN;
import static java.net.HttpURLConnection.HTTP_OK;

public class OAuthSimpleResourceHandler extends AbstractBaseOAuthHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");
    private static final SimpleResourcePermission REQUIRED_PERMISSION = SimpleResourcePermission.READ;
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();
    private static final AuthorizationManager AUTHORIZATION_MANAGER = AuthorizationManager.getInstance();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        Jws<Claims> claimsJws = Utils.parseJwtToken(httpExchange.getPrincipal().getName());

        if (claimsJws == null) {
            Logger.info("Invalid jwt token");

            errorResponse(httpExchange, OAuthErrorType.INVALID_REQUEST);

            return;
        }

        Claims claims = claimsJws.getBody();

        if (claims.getExpiration().getTime() < System.currentTimeMillis()) {
            Logger.info("Token expired");

            errorResponse(httpExchange, OAuthErrorType.INVALID_REQUEST);

            return;
        }

        String scope = String.valueOf(claims.get("scope"));
        User user = DATA_BASE.getUserByLogin(String.valueOf(claims.get("uid")));
        OAuthClientInfo clientInfo = DATA_BASE.getClientInfoById(claims.getAudience());

        List<SimpleResourcePermission> requestScopeList = Utils.parseScope(scope);

        if (!requestScopeList.contains(REQUIRED_PERMISSION) && !AUTHORIZATION_MANAGER.isPermitted(user, REQUIRED_PERMISSION)) {
            Logger.info("Invalid scope was requested");

            errorResponse(httpExchange, OAuthErrorType.INVALID_SCOPE);

            return;
        }

        String response = "This is simple resource and you have access to it";

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, TEXT_PLAIN);
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        closeRequestBodyStream(httpExchange.getRequestBody());

        sendResponse(response, httpExchange.getResponseBody());
    }
}
