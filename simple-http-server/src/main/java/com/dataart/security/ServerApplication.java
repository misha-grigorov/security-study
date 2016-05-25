package com.dataart.security;

import org.rythmengine.Rythm;

import java.util.HashMap;
import java.util.Map;

public class ServerApplication {
    public static final String LOCAL_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 55555;

    public static void main(String[] args) {
        // use Map to store the configuration
        Map<String, Object> rythmConfig = new HashMap<>();

        // tell rythm where to find the template files
        rythmConfig.put("home.template", "templates");

        // init Rythm with our predefined configuration
        Rythm.init(rythmConfig);

        SimpleHttpServer httpsServer = new SimpleHttpServer(LOCAL_HOST, DEFAULT_PORT);

        httpsServer.start();
    }
}
