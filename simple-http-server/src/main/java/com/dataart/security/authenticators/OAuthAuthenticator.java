package com.dataart.security.authenticators;

import com.dataart.security.services.OAuthService;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class OAuthAuthenticator extends Authenticator {
    private static final OAuthService OAUTH_SERVICE = OAuthService.getInstance();

    private String realm;

    public OAuthAuthenticator(String realm) {
        this.realm = realm;
    }

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        String authorization = httpExchange.getRequestHeaders().getFirst("Authorization");

        if (!authorization.startsWith("Bearer ")) {
            return new Retry(HTTP_UNAUTHORIZED);
        }

        String bearerToken = authorization.substring("Bearer ".length());

        if (!OAUTH_SERVICE.checkJwtToken(bearerToken)) {
            return new Retry(HTTP_UNAUTHORIZED);
        }

        return new Success(new HttpPrincipal(bearerToken, realm));
    }
}
