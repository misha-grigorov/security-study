package com.dataart.security;

public class HtmlCoder implements ICoder {

    private static class HtmlCoderHolder {
        private static final HtmlCoder HOLDER_INSTANCE = new HtmlCoder();
    }

    public static HtmlCoder getInstance() {
        return HtmlCoderHolder.HOLDER_INSTANCE;
    }

    private HtmlCoder() {
    }

    @Override
    public String decode(String input) {
        return null;
    }

    @Override
    public String encode(String input) {
        return null;
    }
}
