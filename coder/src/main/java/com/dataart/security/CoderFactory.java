package com.dataart.security;

import org.pmw.tinylog.Logger;

public class CoderFactory {
    public static ICoder getCoder(CoderEnum coderEnum) {
        switch (coderEnum) {
            case BASE64:
                return Base64Coder.getInstance();
            case HEX:
                return HexCoder.getInstance();
            case HTML:
                return HtmlCoder.getInstance();
            case URL:
                return UrlCoder.getInstance();
            default:
                Logger.error("Invalid code {}", coderEnum);

                return new NullCoder();
        }
    }
}
