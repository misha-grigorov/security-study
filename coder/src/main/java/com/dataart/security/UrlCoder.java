package com.dataart.security;

import org.pmw.tinylog.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UrlCoder implements ICoder {

    private static class UrlCoderHolder {
        private static final UrlCoder HOLDER_INSTANCE = new UrlCoder();
    }

    public static UrlCoder getInstance() {
        return UrlCoderHolder.HOLDER_INSTANCE;
    }

    private UrlCoder() {
    }


    @Override
    public String decode(String input) {
        if (input == null) {
            return null;
        }

        String result = null;

        try {
            result = URLDecoder.decode(input, UTF8.name());
        } catch (UnsupportedEncodingException e) {
            Logger.warn(e.getMessage());
        }

        return result;
    }

    @Override
    public String encode(String input) {
        if (input == null) {
            return null;
        }

        String result = null;

        try {
            result = URLEncoder.encode(input, UTF8.name());
        } catch (UnsupportedEncodingException e) {
            Logger.warn(e.getMessage());
        }

        return result;
    }
}
