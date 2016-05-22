package com.dataart.security.services;

import com.dataart.security.users.User;

public class RegistrationToken {
    private String tokenId;
    private User user;
    private long validUntil;

    public RegistrationToken(String tokenId, User user, long validUntil) {
        this.tokenId = tokenId;
        this.user = user;
        this.validUntil = validUntil;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
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
}
