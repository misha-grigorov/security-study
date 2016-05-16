package com.dataart.security;

import org.pmw.tinylog.Logger;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Base64Coder implements ICoder {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private static class Base64CoderHolder {
        private static final Base64Coder HOLDER_INSTANCE = new Base64Coder();
    }

    public static Base64Coder getInstance() {
        return Base64CoderHolder.HOLDER_INSTANCE;
    }

    private Base64Coder() {
    }

    @Override
    public String decode(String input) {
        if (input == null) {
            return null;
        }

        byte[] decodedBytes = null;

        try {
            decodedBytes = DECODER.decode(input);
        } catch (IllegalArgumentException iae) {
            Logger.warn("You input is invalid");
        }

        return decodedBytes == null ? null : new String(decodedBytes, UTF_8);
    }

    @Override
    public String encode(String input) {
        if (input == null) {
            return null;
        }

        return ENCODER.encodeToString(input.getBytes(UTF_8));
    }
}
