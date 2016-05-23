package com.dataart.security;

public class ServerApplication {
    public static final String LOCAL_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 55555;

    public static void main(String[] args) {
        SimpleHttpsServer httpsServer = new SimpleHttpsServer(LOCAL_HOST, DEFAULT_PORT);

        httpsServer.start();
    }
}
