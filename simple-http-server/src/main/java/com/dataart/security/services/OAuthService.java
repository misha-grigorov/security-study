package com.dataart.security.services;

import com.dataart.security.authorization.AuthorizationManager;
import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.oauth.OAuthAuthorizationClientCode;
import com.dataart.security.oauth.OAuthClientAuthorizationRequest;
import com.dataart.security.oauth.OAuthClientInfo;
import com.dataart.security.oauth.OAuthErrorType;
import com.dataart.security.oauth.OAuthJwtAccessToken;
import com.dataart.security.permissions.SimpleResourcePermission;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import com.dataart.security.utils.Utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.pmw.tinylog.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OAuthService {
    public static final long OAUTH_CLIENT_REQUEST_TIMEOUT = 300000; // 5 min
    public static final long OAUTH_CLIENT_CODE_TIMEOUT = 600000; // 10 min
    public static final int JWT_TIMEOUT = 3_600_000; // 60 min

    private static final Map<String, OAuthClientAuthorizationRequest> CLIENT_REQUEST_STATE = new HashMap<>();
    private static final Map<String, OAuthAuthorizationClientCode> CLIENT_CODES = new HashMap<>();
    private static final AuthorizationManager AUTHORIZATION_MANAGER = AuthorizationManager.getInstance();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

    private static class OAuthServiceHolder {
        private static final OAuthService HOLDER_INSTANCE = new OAuthService();
    }

    public static OAuthService getInstance() {
        return OAuthServiceHolder.HOLDER_INSTANCE;
    }

    public synchronized OAuthClientAuthorizationRequest newClientRequest (String rawQuery, User user) {
        OAuthClientAuthorizationRequest result = new OAuthClientAuthorizationRequest();
        Map<String, String> queryMap = Utils.parseQuery(rawQuery);

        String responseType = queryMap.get("response_type");
        String clientId = queryMap.get("client_id");
        String redirectUrl = queryMap.get("redirect_uri");
        String scope = queryMap.get("scope");
        String state = queryMap.get("state");

        if (responseType == null || clientId == null || redirectUrl == null || scope == null || state == null) {
            result.setErrorType(OAuthErrorType.INVALID_REQUEST);

            return result;
        }

        if (!"code".equals(responseType)) {
            result.setErrorType(OAuthErrorType.UNSUPPORTED_RESPONSE_TYPE);

            return result;
        }

        OAuthClientInfo clientInfo = DATA_BASE.getClientInfoById(clientId);

        if (clientInfo == null) {
            result.setErrorType(OAuthErrorType.UNAUTHORIZED_CLIENT);

            return result;
        }

        if (!clientInfo.getRedirectUri().equals(redirectUrl)) {
            result.setErrorType(OAuthErrorType.INVALID_REQUEST);

            return result;
        }

        List<SimpleResourcePermission> scopePermissions = Utils.parseScope(scope)
                .stream()
                .filter(resourcePermission -> AUTHORIZATION_MANAGER.isPermitted(user, resourcePermission))
                .collect(Collectors.toList());

        if (scopePermissions.isEmpty()) {
            result.setErrorType(OAuthErrorType.INVALID_SCOPE);

            return result;
        }

        String requestState = Utils.generateSecureRandom();

        CLIENT_REQUEST_STATE.put(requestState, result);

        result.setClientInfo(clientInfo);
        result.setPermissions(scopePermissions);
        result.setRequestQueryParams(queryMap);
        result.setUser(user);
        result.setState(requestState);
        result.setValidUntil(System.currentTimeMillis() + OAUTH_CLIENT_REQUEST_TIMEOUT);

        return result;
    }

    public synchronized OAuthClientAuthorizationRequest restoreClientRequest(String state) {
        OAuthClientAuthorizationRequest result = null;

        if (state != null && CLIENT_REQUEST_STATE.containsKey(state)) {
            result = CLIENT_REQUEST_STATE.get(state);

            if (result.getValidUntil() <= System.currentTimeMillis()) {
                Logger.info("Client request expired. clientId={}, userLogin={}", result.getClientInfo().getId(), result.getUser().getLogin());

                result.setErrorType(OAuthErrorType.INVALID_REQUEST);

                CLIENT_REQUEST_STATE.remove(state);
            }
        } else {
            result = new OAuthClientAuthorizationRequest(OAuthErrorType.INVALID_REQUEST);
        }

        return result;
    }

    public synchronized OAuthAuthorizationClientCode handleUserAction(Map<String, String> params, boolean isAllowed) throws UnsupportedEncodingException {
        OAuthClientAuthorizationRequest clientRequest = restoreClientRequest(params.get("state"));
        OAuthAuthorizationClientCode result = new OAuthAuthorizationClientCode();
        OAuthErrorType errorType = clientRequest.getErrorType();

        if (errorType != OAuthErrorType.NONE) {
            result.setErrorType(errorType);

            return result;
        }

        StringBuilder redirectUri = new StringBuilder(clientRequest.getClientInfo().getRedirectUri());

        if (!isAllowed) {
            redirectUri
                    .append("?error=")
                    .append(OAuthErrorType.ACCESS_DENIED.toString());
        } else {
            result.setCode(Utils.generateSecureRandom());
            result.setValidUntil(System.currentTimeMillis() + OAUTH_CLIENT_CODE_TIMEOUT);

            String clientCode = result.getCode();
            redirectUri
                    .append("?code=")
                    .append(URLEncoder.encode(clientCode, UTF_8.name()))
                    .append("&scope=")
                    .append(URLEncoder.encode(StringUtils.join(clientRequest.getPermissions().toArray(), " "), UTF_8.name()));

            CLIENT_CODES.put(clientCode, result);
        }

        redirectUri
                .append("&state=")
                .append(URLEncoder.encode(clientRequest.getRequestQueryParams().get("state"), UTF_8.name()));

        result.setUserActionBasedRedirectUri(redirectUri.toString());
        result.setClientInfo(clientRequest.getClientInfo());
        result.setUser(clientRequest.getUser());
        result.setPermissions(clientRequest.getPermissions());

        CLIENT_REQUEST_STATE.remove(clientRequest.getState());

        return result;
    }

    public synchronized OAuthJwtAccessToken newAccessTokenRequest(Map<String, String> queryMap) {
        OAuthJwtAccessToken accessToken = new OAuthJwtAccessToken();

        String code = queryMap.get("code");
        String grantType = queryMap.get("grant_type");
        String redirectUrl = queryMap.get("redirect_uri");
        String clientId = queryMap.get("client_id");

        if (grantType == null || clientId == null || redirectUrl == null || code == null) {
            accessToken.setErrorType(OAuthErrorType.INVALID_REQUEST);

            return accessToken;
        }

        if (!"authorization_code".equals(grantType)) {
            accessToken.setErrorType(OAuthErrorType.UNSUPPORTED_RESPONSE_TYPE);

            return accessToken;
        }

        OAuthAuthorizationClientCode clientAuthorizationRequest = CLIENT_CODES.get(code);

        if (clientAuthorizationRequest == null) {
            accessToken.setErrorType(OAuthErrorType.UNAUTHORIZED_CLIENT);

            return accessToken;
        }

        long now = System.currentTimeMillis();

        if (clientAuthorizationRequest.getValidUntil() <= now) {
            accessToken.setErrorType(OAuthErrorType.INVALID_REQUEST);

            return accessToken;
        }

        if (!clientId.equals(clientAuthorizationRequest.getClientInfo().getId())) {
            accessToken.setErrorType(OAuthErrorType.UNAUTHORIZED_CLIENT);

            return accessToken;
        }

        byte[] key = Base64.getEncoder().encode(clientAuthorizationRequest.getClientInfo().getSecret().getBytes());

        String jwtToken = Jwts.builder()
                .setId(Utils.generateSecureRandom())
                .setAudience(clientId)
                .setIssuer("Simple Http Server")
                .setExpiration(new Date(now + JWT_TIMEOUT))
                .setIssuedAt(new Date(now))
                .claim("scope", StringUtils.join(clientAuthorizationRequest.getPermissions().toArray(), " "))
                .claim("uid", clientAuthorizationRequest.getUser().getLogin())
                .signWith(SignatureAlgorithm.HS256, key).compact();

        accessToken.setJwtToken(jwtToken);
        CLIENT_CODES.remove(code);

        return accessToken;
    }

    public boolean checkJwtToken(String bearerToken) {
        if (!Jwts.parser().isSigned(bearerToken)) {
            return false;
        }

        Jws<Claims> claimsJws = Utils.parseJwtToken(bearerToken);

        if (claimsJws == null) {
            return false;
        }

        Claims claims = claimsJws.getBody();

        if (claims.getId() == null) {
            return false;
        }

        if (claims.getIssuer() == null || !claims.getIssuer().equals("Simple Http Server")) {
            return false;
        }

        if (claims.getExpiration() == null || claims.getExpiration().getTime() < System.currentTimeMillis()) {
            return false;
        }

        if (claims.get("scope") == null) {
            return false;
        }

        if (claims.get("uid") == null || DATA_BASE.getUserByLogin(String.valueOf(claims.get("uid"))) == null ||
                DATA_BASE.getUserByLogin(String.valueOf(claims.get("uid"))).getStatus() != UserStatus.ACTIVE) {
            return false;
        }

        return true;
    }
}
