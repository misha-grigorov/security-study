package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.boon.json.JsonException;
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

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

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
        if (!"application/json".equals(httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        InputStream requestBody = httpExchange.getRequestBody();
        OutputStream responseBody = httpExchange.getResponseBody();
        Object jsonObject = parseJson(requestBody, Object.class);
        String response = jsonObject == null ? "JSON is invalid" : "JSON is valid";

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        sendResponse(response, responseBody);
    }

    protected <T> T parseJson(InputStream requestBody, Class<T> tClass) {
        T result = null;

        try (InputStream stream = requestBody) {
            result = JSON_MAPPER.readValue(stream, tClass);
        } catch (JsonException | IOException e) {
            Logger.warn(e.getMessage());
        }

        return result;
    }
}
