package com.dataart.security;

import com.dataart.security.authenticators.FormsAuthenticator;
import com.dataart.security.authenticators.OAuthAccessTokenAuthenticator;
import com.dataart.security.authenticators.OAuthAuthenticator;
import com.dataart.security.authenticators.SimpleBasicAuthenticator;
import com.dataart.security.handlers.AuthHandler;
import com.dataart.security.handlers.ChangePasswordHandler;
import com.dataart.security.handlers.ChangePasswordPageHandler;
import com.dataart.security.handlers.CorsApiHandler;
import com.dataart.security.handlers.FileUploadHandler;
import com.dataart.security.handlers.FormHandler;
import com.dataart.security.handlers.ImageUploadHandler;
import com.dataart.security.handlers.ImagesHandler;
import com.dataart.security.handlers.JsonHandler;
import com.dataart.security.handlers.LoginPageHandler;
import com.dataart.security.handlers.LogoutHandler;
import com.dataart.security.handlers.OAuthCallbackHandler;
import com.dataart.security.handlers.OAuthPageHandler;
import com.dataart.security.handlers.ProfilePageHandler;
import com.dataart.security.handlers.RecoverPageHandler;
import com.dataart.security.handlers.RegisterPageHandler;
import com.dataart.security.handlers.RegistrationHandler;
import com.dataart.security.handlers.RootHandler;
import com.dataart.security.handlers.SimpleResourceHandler;
import com.dataart.security.handlers.SimpleResourcePageHandler;
import com.dataart.security.handlers.oauth.OAuthAuthorizationHandler;
import com.dataart.security.handlers.oauth.OAuthClientPageHandler;
import com.dataart.security.handlers.oauth.OAuthRedirectHandler;
import com.dataart.security.handlers.oauth.OAuthAccessTokenHandler;
import com.dataart.security.handlers.oauth.OAuthSimpleResourceHandler;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpServer;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

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
        Authenticator basicAuthenticator = new SimpleBasicAuthenticator("Some Realm");
        Authenticator formsAuthenticator = new FormsAuthenticator();
        Authenticator oauthAuthenticator = new OAuthAuthenticator("OAuth Bearer");
        Authenticator oAuthAccessTokenAuthenticator = new OAuthAccessTokenAuthenticator("OAuth Basic");

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
        server.createContext("/logout", new LogoutHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/register", new RegistrationHandler());
        server.createContext("/login-page", new LoginPageHandler());
        server.createContext("/register-page", new RegisterPageHandler());
        server.createContext("/recovery-page", new RecoverPageHandler());

        server.createContext("/oauth/client", new OAuthClientPageHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/oauth/token", new OAuthAccessTokenHandler()).setAuthenticator(oAuthAccessTokenAuthenticator);
        server.createContext("/oauth/auth", new OAuthAuthorizationHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/oauth/redirect", new OAuthRedirectHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/oauth/resource", new OAuthSimpleResourceHandler()).setAuthenticator(oauthAuthenticator);

        server.createContext("/simple-resource-page", new SimpleResourcePageHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/simple-resource", new SimpleResourceHandler()).setAuthenticator(formsAuthenticator);

        server.createContext("/upload/image", new ImageUploadHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/profile-page", new ProfilePageHandler()).setAuthenticator(formsAuthenticator);
        server.createContext("/images", new ImagesHandler()).setAuthenticator(formsAuthenticator);

        server.createContext("/json", new JsonHandler()).setAuthenticator(basicAuthenticator);
        server.createContext("/form", new FormHandler()).setAuthenticator(basicAuthenticator);
        server.createContext("/upload", new FileUploadHandler()).setAuthenticator(basicAuthenticator);

        server.createContext("/cors-api", new CorsApiHandler());

        server.createContext("/oauth-page", new OAuthPageHandler());
        server.createContext("/oauth2callback", new OAuthCallbackHandler());
    }
}
