package com.dataart.security.handlers;

import com.dataart.security.services.ImageService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class ImagesHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET");
    private static final ImageService IMAGE_SERVICE = ImageService.getInstance();

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        // TODO: check if user has required permission to read requested image

        byte[] response = IMAGE_SERVICE.readImage(httpExchange);

        if (response == null) {
            badRequest(HTTP_NOT_FOUND, httpExchange);

            return;
        }

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, "image/jpeg");
        httpExchange.sendResponseHeaders(HTTP_OK, response.length);
        closeRequestBodyStream(httpExchange.getRequestBody());

        OutputStream responseBody = httpExchange.getResponseBody();

        responseBody.write(response);
        closeResponseBodyStream(responseBody);
    }
}
