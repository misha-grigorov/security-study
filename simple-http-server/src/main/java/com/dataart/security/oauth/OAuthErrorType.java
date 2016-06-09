package com.dataart.security.oauth;

public enum OAuthErrorType {
    INVALID_REQUEST("invalid_request", 400),
    UNAUTHORIZED_CLIENT("unauthorized_client", 401),
    ACCESS_DENIED("access_denied", 403),
    UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type", 400),
    INVALID_SCOPE("invalid_scope", 400),
    SERVER_ERROR("server_error", 500),
    TEMPORARILY_UNAVAILABLE("temporarily_unavailable", 503),
    NONE("none", -1);

    private final String name;
    private final int responseCode;

    OAuthErrorType(String name, int responseCode) {
        this.name = name;
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String toString() {
        return name;
    }
}
