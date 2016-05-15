package com.dataart.security;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;

import static com.dataart.security.CoderEnum.BASE64;
import static com.dataart.security.CoderEnum.HEX;
import static com.dataart.security.CoderEnum.HTML;
import static com.dataart.security.CoderEnum.URL;
import static com.dataart.security.CoderModeEnum.DECODE;
import static com.dataart.security.CoderModeEnum.ENCODE;

public class AppArgumentsParser extends DefaultParser {
    private static final String INPUT_SOURCE_OPTION_LABEL = "i";
    private static final String CODER_MODE_OPTION_LABEL = "m";
    private static final String CODER_OPTION_LABEL = "u";
    private static final String[] OPTION_LABELS;
    private static final Options OPTIONS = new Options();
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();

    private String inputSource;
    private CoderModeEnum coderModeEnum;
    private CoderEnum coderEnum;

    static {
        OPTION_LABELS = new String[]{INPUT_SOURCE_OPTION_LABEL, CODER_MODE_OPTION_LABEL, CODER_OPTION_LABEL};

        OPTIONS.addOption(INPUT_SOURCE_OPTION_LABEL, true, "input source");
        OPTIONS.addOption(CODER_MODE_OPTION_LABEL, true, "coderEnum mode: " +
                String.join(", ", ENCODE.name(), DECODE.name()));
        OPTIONS.addOption(CODER_OPTION_LABEL, true, "use required coderEnum: " +
                String.join(", ", BASE64.name(), HEX.name(), HTML.name(), URL.name()));
    }

    public static void printHelp(String appName) {
        HELP_FORMATTER.printHelp(appName, OPTIONS);
    }

    public AppArgumentsParser(String[] arguments) throws ParseException {
        init(arguments);
    }

    private void init(String[] arguments) throws ParseException {
        if (arguments == null || arguments.length != OPTION_LABELS.length * 2) { // every option has an input
            throw new ParseException("Invalid arguments! arguments=" + Arrays.toString(arguments));
        }

        CommandLine commandLine = parse(OPTIONS, arguments);

        for (String option : OPTION_LABELS) {
            if (!commandLine.hasOption(option)) {
                throw new ParseException("Missing required argument: " + option);
            }
        }

        initInputSource(commandLine);
        initCoderMode(commandLine);
        initCoder(commandLine);
    }

    private void initInputSource(CommandLine commandLine) {
        inputSource = commandLine.getOptionValue(INPUT_SOURCE_OPTION_LABEL);
    }

    private void initCoderMode(CommandLine commandLine) throws ParseException {
        try {
            coderModeEnum = CoderModeEnum.valueOf(commandLine.getOptionValue(CODER_MODE_OPTION_LABEL).toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ParseException(iae.getMessage());
        }
    }

    private void initCoder(CommandLine commandLine) throws ParseException {
        try {
            coderEnum = CoderEnum.valueOf(commandLine.getOptionValue(CODER_OPTION_LABEL).toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ParseException(iae.getMessage());
        }
    }

    public String getInputSource() {
        return inputSource;
    }

    public CoderModeEnum getCoderModeEnum() {
        return coderModeEnum;
    }

    public CoderEnum getCoderEnum() {
        return coderEnum;
    }
}
