package com.dataart.security;

import org.apache.commons.cli.ParseException;
import org.pmw.tinylog.Logger;

public class CoderApplication {
    public static final String APP_NAME = "coder";

    public static void main(String[] args) {
        AppArgumentsParser argumentsParser = null;

        try {
            argumentsParser = new AppArgumentsParser(args);
        } catch (ParseException e) {
            Logger.error(e.getMessage());
        }

        if (argumentsParser == null) {
            AppArgumentsParser.printHelp(APP_NAME);

            return;
        }

        String inputSource = argumentsParser.getInputSource();
        CoderModeEnum coderModeEnum = argumentsParser.getCoderModeEnum();
        CoderEnum coderEnum = argumentsParser.getCoderEnum();

        Logger.info("Original input: " + inputSource);

        String result = getCodingResult(inputSource, coderModeEnum, coderEnum);

        Logger.info("Coding result: mode={}, coder={}, output={}", coderModeEnum, coderEnum, result);
    }

    public static String getCodingResult(String inputSource, CoderModeEnum coderMode, CoderEnum coderEnum) {
        ICoder coder = CoderFactory.getCoder(coderEnum);

        switch (coderMode) {
            case ENCODE:
                return coder.encode(inputSource);
            case DECODE:
                return coder.decode(inputSource);
            default:
                Logger.error("Invalid coder mode {}", coderMode);

                return null;
        }
    }
}
