package com.dataart.security.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class JsonHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        JSON_MAPPER.registerModule(new JsonOrgModule());
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

        JSONObject jsonObject = null;

        try {
            jsonObject = JSON_MAPPER.readValue(requestBody, JSONObject.class);
        } catch (IOException e) {
            Logger.warn(e.getMessage());
        }

        String response = jsonObject == null ? "JSON is invalid" : "JSON is valid";

        closeRequestBodyStream(requestBody);

        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        responseBody.write(response.getBytes(UTF8));

        closeResponseBodyStream(responseBody);
    }
}
