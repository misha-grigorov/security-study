package com.dataart.security.oauth;

import com.dataart.security.permissions.SimpleResourcePermission;
import com.dataart.security.users.User;

import java.util.List;
import java.util.Objects;

public class OAuthAuthorizationClientCode {
    private String code;
    private OAuthClientInfo clientInfo;
    private User user;
    private long validUntil;
    private String userActionBasedRedirectUri;
    private OAuthErrorType errorType = OAuthErrorType.NONE;
    private List<SimpleResourcePermission> permissions;

    public OAuthAuthorizationClientCode() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public long getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(long validUntil) {
        this.validUntil = validUntil;
    }

    public String getUserActionBasedRedirectUri() {
        return userActionBasedRedirectUri;
    }

    public void setUserActionBasedRedirectUri(String userActionBasedRedirectUri) {
        this.userActionBasedRedirectUri = userActionBasedRedirectUri;
    }

    public OAuthErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(OAuthErrorType errorType) {
        this.errorType = errorType;
    }

    public List<SimpleResourcePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<SimpleResourcePermission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthAuthorizationClientCode that = (OAuthAuthorizationClientCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
