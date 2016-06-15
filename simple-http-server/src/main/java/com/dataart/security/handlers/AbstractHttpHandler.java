package com.dataart.security.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static com.dataart.security.utils.Utils.SERVER_SESSION_KEY;
import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class AbstractHttpHandler implements HttpHandler {
    protected abstract List<String> getAllowedMethods();
    protected abstract void chainHandle(HttpExchange httpExchange) throws IOException;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InetSocketAddress remoteAddress = httpExchange.getRemoteAddress();
        String requestMethod = httpExchange.getRequestMethod();

        Logger.info("Incoming request {}:{}, {} {}", remoteAddress.getHostString(), remoteAddress.getPort(),
                requestMethod, httpExchange.getRequestURI());

        if (!getAllowedMethods().contains(requestMethod)) {
            badRequest(HTTP_BAD_METHOD, httpExchange);

            return;
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

    protected void badRequest(int responseStatus, HttpExchange httpExchange, String response) throws IOException {
        int responseLength = response == null ? -1 : response.length();

        OutputStream responseBody = httpExchange.getResponseBody();

        if (responseLength != -1) {
            httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");

            responseBody.write(response.getBytes(UTF_8));
        }

        httpExchange.sendResponseHeaders(responseStatus, responseLength);
        closeRequestBodyStream(httpExchange.getRequestBody());
        closeResponseBodyStream(responseBody);
    }

    protected void badRequest(int responseStatus, HttpExchange httpExchange) throws IOException {
        badRequest(responseStatus, httpExchange, null);
    }

    protected void sendResponse(String response, OutputStream responseBody) throws IOException {
        responseBody.write(response.getBytes(UTF_8));

        closeResponseBodyStream(responseBody);
    }

    protected void redirect(String location, String sessionId, HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().set("Set-Cookie", SERVER_SESSION_KEY + sessionId +
                "; path=/; domain=127.0.0.1; httponly");

        redirect(location, httpExchange);
    }

    protected void redirect(String location, HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().set("Location", location);
        httpExchange.sendResponseHeaders(HTTP_MOVED_TEMP, -1);

        closeRequestBodyStream(httpExchange.getRequestBody());
        closeResponseBodyStream(httpExchange.getResponseBody());
    }
}
