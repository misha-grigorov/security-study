package com.dataart.security.utils;

import com.dataart.security.users.User;
import org.pmw.tinylog.Logger;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
    public static final String COOKIE_KEY = "Cookie";
    public static final String COOKIE_DELIMITER = ";";
    public static final String SERVER_SESSION_KEY = "id=";
    public static final String USER_AGENT = "User-Agent";
    public static final String FORMS_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";

    private Utils() {
    }

    public static String readRequestBody(InputStream requestBody) {
        return readRequestBody(requestBody, true);
    }

    public static String readRequestBody(InputStream requestBody, boolean closeStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            int buffer;
            InputStreamReader streamReader = new InputStreamReader(requestBody, UTF_8);

            while ((buffer = streamReader.read()) != -1) {
                stringBuilder.append((char) buffer);
            }
        } catch (IOException e) {
            Logger.warn(e.getMessage());
        }

        if (closeStream) {
            try {
                requestBody.close();
            } catch (IOException e) {
                Logger.warn(e.getMessage());
            }
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> parseQuery(String source) {
        Map<String, String> result = new HashMap<>();

        if (source == null) {
            return result;
        }

        String[] pairs = source.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");

            String key = null;
            try {
                key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), UTF_8.name()) : pair;
            } catch (UnsupportedEncodingException e) {
                Logger.warn(e.getMessage());
            }

            String value = null;
            try {
                value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), UTF_8.name()) : null;
            } catch (UnsupportedEncodingException e) {
                Logger.warn(e.getMessage());
            }

            if (!result.containsKey(key)) {
                result.put(key, value);
            }
        }

        return result;
    }

    public static String generateSecureRandom() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            return new BigInteger(130, secureRandom).toString(32);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashPassword(final char[] password, final byte[] salt) {
        return new String(hashPassword(password, salt, 10000, 256), UTF_8);
    }

    public static byte[] hashPassword(final char[] password, final byte[] salt, final int iterations, final int keyLength) {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkPassword(String password, User user) {
        String newHashedPassword = hashPassword(password.toCharArray(), user.getSalt().getBytes(UTF_8));

        return user.getPassword().equals(newHashedPassword);
    }
}
