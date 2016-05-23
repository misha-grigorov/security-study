package com.dataart.security;

import com.dataart.security.authenticators.FormsAuthenticator;
import com.dataart.security.authenticators.SimpleBasicAuthenticator;
import com.dataart.security.handlers.AuthHandler;
import com.dataart.security.handlers.ChangePasswordHandler;
import com.dataart.security.handlers.ChangePasswordPageHandler;
import com.dataart.security.handlers.FileUploadHandler;
import com.dataart.security.handlers.FormHandler;
import com.dataart.security.handlers.JsonHandler;
import com.dataart.security.handlers.LoginPageHandler;
import com.dataart.security.handlers.RecoverPageHandler;
import com.dataart.security.handlers.RegisterPageHandler;
import com.dataart.security.handlers.RegistrationHandler;
import com.dataart.security.handlers.RootHandler;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.pmw.tinylog.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class SimpleHttpsServer {
    private static final int USE_SYSTEM_DEFAULT_BACKLOG = 0;

    private int port;
    private String host;
    private HttpsServer server;

    public SimpleHttpsServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(host, port);

            final SSLContext sslContext = createSslContext();

            server = HttpsServer.create(socketAddress, USE_SYSTEM_DEFAULT_BACKLOG);

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters httpsParameters) {
                    try {
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        httpsParameters.setNeedClientAuth(false);
                        httpsParameters.setCipherSuites(engine.getEnabledCipherSuites());
                        httpsParameters.setProtocols(engine.getEnabledProtocols());

                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        httpsParameters.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        Logger.error("Failed to create HTTPS server", ex);
                    }
                }
            });

            Logger.info("Server started at {}:{}", socketAddress.getHostString(), socketAddress.getPort());

            initContext();

            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            Logger.warn(e.getMessage());
        }
    }

    public void stop() {
        server.stop(0);

        Logger.info("Server was stopped");
    }

    protected void initContext() {
        Authenticator basicAuthenticator = new SimpleBasicAuthenticator("Some Realm");
        Authenticator formsAuthenticator = new FormsAuthenticator();

        server.createContext("/", new RootHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/favicon.ico", httpExchange -> {
            httpExchange.sendResponseHeaders(HTTP_NOT_FOUND, -1);
            httpExchange.getRequestBody().close();
            httpExchange.getResponseBody().flush();
            httpExchange.getResponseBody().close();
        });
        server.createContext("/auth", new AuthHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/change-password", new ChangePasswordHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/change-password-page", new ChangePasswordPageHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/register", new RegistrationHandler());
        server.createContext("/login-page", new LoginPageHandler());
        server.createContext("/register-page", new RegisterPageHandler());
        server.createContext("/recovery-page", new RecoverPageHandler());

        server.createContext("/json", new JsonHandler()).setAuthenticator(basicAuthenticator);
        server.createContext("/form", new FormHandler()).setAuthenticator(basicAuthenticator);
        server.createContext("/upload", new FileUploadHandler()).setAuthenticator(basicAuthenticator);
    }

    protected SSLContext createSslContext() throws Exception {
        SSLContext sslContext = null;

        KeyStore keyStore = KeyStore.getInstance("JKS");

        keyStore.load(SimpleHttpsServer.class.getClassLoader().getResourceAsStream("serverkeystore"),
                "$3cur!tY-SSZ2Q".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");

        keyManagerFactory.init(keyStore, "$3cur!tY-SSZ2Q".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");

        trustManagerFactory.init(keyStore);

        sslContext = SSLContext.getInstance("TLSv1.2");

        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }
}
