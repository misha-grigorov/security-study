package com.dataart.security.utils;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.boon.json.JsonParserFactory;
import org.boon.json.JsonSerializerFactory;
import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GoogleOAuthUtils {
    public static final String CLIENT_ID = "988237544668-ruhh042c7a303r9qah2cduq3rh858f6m.apps.googleusercontent.com";
    public static final String CLIENT_SECRET = "9Xa--8uXILu48ly8UuKa64v3";
    public static final String REDIRECT_URL = "http://127.0.0.1:55555/oauth2callback";
    public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    public static final String TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";
    public static final String RESPONSE_TYPE = "code";
    public static final String SCOPE = "https://mail.google.com/ https://www.googleapis.com/auth/gmail.modify https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/gmail.labels";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String LABELS_REQUEST_URL = "https://www.googleapis.com/gmail/v1/users/me/labels";

    private static final ObjectMapper JSON_MAPPER;
    private static final Map<String, String> TOKEN_LABELS = new ConcurrentHashMap<>();

    static {
        JsonParserFactory jsonParserFactory = new JsonParserFactory().strict();

        JSON_MAPPER = new ObjectMapperImpl(jsonParserFactory, new JsonSerializerFactory());
    }

    private GoogleOAuthUtils() {
    }

    public static String formAuthUrlForClient(String state) throws UnsupportedEncodingException {
        return AUTH_URL + "?response_type=" + RESPONSE_TYPE + "&client_id=" + URLEncoder.encode(CLIENT_ID, UTF_8.name()) +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URL, UTF_8.name()) +
                "&scope=" + URLEncoder.encode(SCOPE, UTF_8.name()) + "&state=" + URLEncoder.encode(state, UTF_8.name());
    }

    public static String getAccessTokenRaw(String code) throws IOException {
        return Request.Post(TOKEN_URL).
                bodyForm(Form.form()
                        .add("code", code)
                        .add("client_id", CLIENT_ID)
                        .add("client_secret", CLIENT_SECRET)
                        .add("redirect_uri", REDIRECT_URL)
                        .add("grant_type", GRANT_TYPE)
                        .build())
                .execute().returnContent().asString(UTF_8);
    }

    public static GoogleOAuthAccessTokenResponse getAccessToken(String code) throws IOException {
        String jsonString = getAccessTokenRaw(code);

        return JSON_MAPPER.readValue(jsonString, GoogleOAuthAccessTokenResponse.class);
    }

    public static String getGmailLabelsRaw(GoogleOAuthAccessTokenResponse tokenResponse) throws IOException {
        return Request.Get(LABELS_REQUEST_URL)
                .addHeader("Authorization", tokenResponse.getTokenType() + " " + tokenResponse.getAccessToken())
                .execute().returnContent().asString(UTF_8);
    }

    public static List<GmailLabelInfo> getGmailLabels(String jsonString) {
        return JSON_MAPPER.readValue(jsonString, GmailLabelsMap.class).getLabels();
    }

    public static void putLabels(String accessToken, String jsonLabels) {
        TOKEN_LABELS.put(accessToken, jsonLabels);
    }

    public static Collection<String> getAvailableLabels() {
        return TOKEN_LABELS.values();
    }
}
