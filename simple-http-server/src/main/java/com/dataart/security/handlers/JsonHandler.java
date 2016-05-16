package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.boon.json.JsonException;
import org.boon.json.JsonFactory;
import org.boon.json.JsonParserFactory;
import org.boon.json.JsonSerializerFactory;
import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

public class JsonHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final ObjectMapper JSON_MAPPER;

    static {
        JsonParserFactory jsonParserFactory = new JsonParserFactory().strict();

        JSON_MAPPER = new ObjectMapperImpl(jsonParserFactory, new JsonSerializerFactory());
    }

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        InputStream requestBody = httpExchange.getRequestBody();
        OutputStream responseBody = httpExchange.getResponseBody();

        httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");

        Object jsonObject = null;

        try {
            jsonObject = JSON_MAPPER.readValue(requestBody, Object.class);
        } catch (JsonException e) {
            Logger.warn(e.getMessage());
        }

        String response = jsonObject == null ? "JSON is invalid" : "JSON is valid";

        closeRequestBodyStream(requestBody);

        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        responseBody.write(response.getBytes(UTF_8));

        closeResponseBodyStream(responseBody);
    }
}
