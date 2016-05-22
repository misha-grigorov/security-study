package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static com.dataart.security.utils.Utils.readRequestBody;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

public class FileUploadHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final String BOUNDARY_KEY = "boundary=";
    private static final String FILENAME_KEY = "filename=\"";

    @Override
    protected List<String> getAllowedMethods() {
        return ALLOWED_METHODS;
    }

    @Override
    protected void chainHandle(HttpExchange httpExchange) throws IOException {
        String contentType = httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE);

        if (contentType == null || !contentType.startsWith("multipart/form-data;") || !contentType.contains(BOUNDARY_KEY)) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        String contentDelimiter = contentType.substring(contentType.indexOf(BOUNDARY_KEY) + BOUNDARY_KEY.length());
        InputStream requestBody = httpExchange.getRequestBody();
        OutputStream responseBody = httpExchange.getResponseBody();
        String fileContent = readRequestBody(requestBody);

        String fileName = getFileName(fileContent);

        if (fileName == null) {
            badRequest(HTTP_BAD_REQUEST, httpExchange);
            return;
        }

        String response = "boundary is [" + contentDelimiter + "] filename is [" + fileName + "]";

        httpExchange.getResponseHeaders().add(CONTENT_TYPE, "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(HTTP_OK, response.length());

        sendResponse(response, responseBody);
    }

    protected String getFileName(String fileContent) {
        int fileNameKeyIndex = fileContent.indexOf(FILENAME_KEY);

        if (fileNameKeyIndex == -1) {
            return null;
        }

        int startIndex = fileNameKeyIndex + FILENAME_KEY.length();

        return fileContent.substring(startIndex, fileContent.indexOf("\"", startIndex));
    }
}
