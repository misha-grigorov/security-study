package com.dataart.security;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
    private Utils() {
    }

    public static String readRequestBody(InputStream requestBody) {
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream stream = requestBody) {
            int buffer;
            InputStreamReader streamReader = new InputStreamReader(stream, UTF_8);

            while ((buffer = streamReader.read()) != -1) {
                stringBuilder.append((char) buffer);
            }
        } catch (IOException e) {
            Logger.warn(e.getMessage());
        }

        return stringBuilder.toString();
    }
}
