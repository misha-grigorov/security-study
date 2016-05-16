package com.dataart.security;

import org.apache.commons.lang3.StringEscapeUtils;

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
        return StringEscapeUtils.unescapeHtml4(input);
    }

    @Override
    public String encode(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
}
