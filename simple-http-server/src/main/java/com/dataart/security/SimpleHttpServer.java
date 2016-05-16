package com.dataart.security;

import com.dataart.security.handlers.JsonHandler;
import com.dataart.security.handlers.RootHandler;
import com.sun.net.httpserver.HttpServer;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    private static final int USE_SYSTEM_DEFAULT_BACKLOG = 0;

    private int port;
    private String host;
    private HttpServer server;

    public SimpleHttpServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(host, port);
            server = HttpServer.create(socketAddress, USE_SYSTEM_DEFAULT_BACKLOG);

            Logger.info("Server started at {}:{}", socketAddress.getHostString(), socketAddress.getPort());

            initContext();

            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            Logger.warn(e.getMessage());
        }
    }

    public void stop() {
        server.stop(0);

        Logger.info("Server was stopped");
    }

    protected void initContext() {
        server.createContext("/", new RootHandler());
        server.createContext("/json", new JsonHandler());
    }
}
