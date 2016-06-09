package com.dataart.security.oauth;

import org.boon.json.JsonParserFactory;
import org.boon.json.JsonSerializerFactory;
import org.boon.json.ObjectMapper;
import org.boon.json.annotations.JsonIgnore;
import org.boon.json.annotations.SerializedName;
import org.boon.json.implementation.ObjectMapperImpl;

import java.util.Objects;

public class OAuthJwtAccessToken {
    private static final ObjectMapper JSON_MAPPER;

    static {
        JsonParserFactory jsonParserFactory = new JsonParserFactory().strict();
        JsonSerializerFactory jsonSerializer = new JsonSerializerFactory().useAnnotations();

        JSON_MAPPER = new ObjectMapperImpl(jsonParserFactory, jsonSerializer);
    }

    @SerializedName("token_type")
    private final String tokenType = "Bearer";

    @JsonIgnore
    private OAuthErrorType errorType = OAuthErrorType.NONE;

    @SerializedName("access_token")
    private String jwtToken;

    public OAuthJwtAccessToken() {
    }

    public OAuthErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(OAuthErrorType errorType) {
        this.errorType = errorType;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthJwtAccessToken that = (OAuthJwtAccessToken) o;
        return errorType == that.errorType &&
                Objects.equals(jwtToken, that.jwtToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorType, jwtToken);
    }

    public String toJsonString() {
        return JSON_MAPPER.toJson(this);
    }
}
