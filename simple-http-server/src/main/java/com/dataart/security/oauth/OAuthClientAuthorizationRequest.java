package com.dataart.security.oauth;

import com.dataart.security.permissions.SimpleResourcePermission;
import com.dataart.security.users.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OAuthClientAuthorizationRequest {
    private OAuthClientInfo clientInfo;
    private User user;
    private Map<String, String> requestQueryParams;
    private List<SimpleResourcePermission> permissions;
    private long validUntil;
    private String state;
    private OAuthErrorType errorType = OAuthErrorType.NONE;

    public OAuthClientAuthorizationRequest() {
    }

    public OAuthClientAuthorizationRequest(OAuthErrorType errorType) {
        this.errorType = errorType;
    }

    public OAuthClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(OAuthClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<String, String> getRequestQueryParams() {
        return requestQueryParams;
    }

    public void setRequestQueryParams(Map<String, String> requestQueryParams) {
        this.requestQueryParams = requestQueryParams;
    }

    public List<SimpleResourcePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<SimpleResourcePermission> permissions) {
        this.permissions = permissions;
    }

    public long getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(long validUntil) {
        this.validUntil = validUntil;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public OAuthErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(OAuthErrorType errorType) {
        this.errorType = errorType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthClientAuthorizationRequest that = (OAuthClientAuthorizationRequest) o;
        return Objects.equals(state, that.state) &&
                errorType == that.errorType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, errorType);
    }
}
