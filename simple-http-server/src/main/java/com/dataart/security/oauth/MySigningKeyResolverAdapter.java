package com.dataart.security.oauth;

import com.dataart.security.db.InMemoryUserDataBase;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.SigningKeyResolverAdapter;

import java.util.Base64;

public class MySigningKeyResolverAdapter extends SigningKeyResolverAdapter {
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

    @Override
    public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
        if (!header.getAlgorithm().equals(SignatureAlgorithm.HS256.getValue())) {
            throw new SignatureException("Algorithm is not supported, invalid token");
        }

        String audience = claims.getAudience();

        if (audience == null) {
            throw new SignatureException("Audience claim is required, invalid token");
        }

        OAuthClientInfo clientInfo = DATA_BASE.getClientInfoById(audience);

        return Base64.getEncoder().encode(clientInfo.getSecret().getBytes());
    }
}
