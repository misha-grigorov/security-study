package com.dataart.security;

public class NullCoder implements ICoder {
    @Override
    public String decode(String input) {
        return null;
    }

    @Override
    public String encode(String input) {
        return null;
    }
}
