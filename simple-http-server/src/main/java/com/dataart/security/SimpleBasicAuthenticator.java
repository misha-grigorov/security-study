package com.dataart.security;

import com.sun.net.httpserver.BasicAuthenticator;

public class SimpleBasicAuthenticator extends BasicAuthenticator {
    public SimpleBasicAuthenticator(String s) {
        super(s);
    }

    @Override
    public boolean checkCredentials(String user, String password) {
        //unsafe
        return user.equals("admin") && password.equals("password");
    }
}
