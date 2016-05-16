package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;

public abstract class AbstractHttpHandler implements HttpHandler {

    protected abstract List<String> getAllowedMethods();
    protected abstract void chainHandle(HttpExchange httpExchange) throws IOException;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InetSocketAddress remoteAddress = httpExchange.getRemoteAddress();
        String requestMethod = httpExchange.getRequestMethod();
        InputStream requestBody = httpExchange.getRequestBody();
        OutputStream responseBody = httpExchange.getResponseBody();

        Logger.info("Incoming request {}:{}, {} {}", remoteAddress.getHostString(), remoteAddress.getPort(),
                requestMethod, httpExchange.getRequestURI());

        if (!getAllowedMethods().contains(requestMethod)) {
            httpExchange.sendResponseHeaders(HTTP_BAD_METHOD, -1);
            closeRequestBodyStream(requestBody);
            closeResponseBodyStream(responseBody);
        }

        chainHandle(httpExchange);
    }

    protected void closeRequestBodyStream(InputStream requestBody) throws IOException {
        requestBody.close();
    }

    protected void closeResponseBodyStream(OutputStream responseBody) throws IOException {
        responseBody.flush();
        responseBody.close();
    }
}
