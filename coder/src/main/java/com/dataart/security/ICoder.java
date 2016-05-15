package com.dataart.security;

import java.nio.charset.Charset;

public interface ICoder {
    Charset UTF8 = Charset.forName("UTF-8");

    String decode(String input);
    String encode(String input);
}
