package com.dataart.security;

public class HexCoder implements ICoder {

    private static class HexCoderHolder {
        private static final HexCoder HOLDER_INSTANCE = new HexCoder();
    }

    public static HexCoder getInstance() {
        return HexCoderHolder.HOLDER_INSTANCE;
    }

    private HexCoder() {
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
