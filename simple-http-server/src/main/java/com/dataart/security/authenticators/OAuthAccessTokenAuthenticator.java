package com.dataart.security.authenticators;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.oauth.OAuthClientInfo;
import com.sun.net.httpserver.BasicAuthenticator;

public class OAuthAccessTokenAuthenticator extends BasicAuthenticator {
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

    public OAuthAccessTokenAuthenticator(String s) {
        super(s);
    }

    @Override
    public boolean checkCredentials(String clientId, String clientSecret) {
        if (clientId == null || clientSecret == null) {
            return false;
        }

        OAuthClientInfo clientInfo = DATA_BASE.getClientInfoById(clientId);

        return clientInfo != null && clientInfo.getSecret().equals(clientSecret);
    }
}
