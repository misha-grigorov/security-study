package com.dataart.security;

import javax.xml.bind.DatatypeConverter;

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
        if (input == null) {
            return null;
        }

        return new String(DatatypeConverter.parseHexBinary(input));
    }

    @Override
    public String encode(String input) {
        if (input == null) {
            return null;
        }

        return DatatypeConverter.printHexBinary(input.getBytes(UTF8));
    }
}
