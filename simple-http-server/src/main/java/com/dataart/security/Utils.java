package com.dataart.security;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
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
}
