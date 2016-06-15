package com.dataart.security.handlers;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.services.ImageService;
import com.dataart.security.users.User;
import com.sun.net.httpserver.HttpExchange;
import org.rythmengine.Rythm;

import java.io.IOException;

public class ProfilePageHandler extends SingleHtmlPageHandler {
    private static final String TEMPLATE_HTML = "profile.html";
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();
    private static final ImageService IMAGE_SERVICE = ImageService.getInstance();

    @Override
    protected String getResponse(HttpExchange httpExchange) throws IOException {
        String userLogin = httpExchange.getPrincipal() != null ? httpExchange.getPrincipal().getUsername() : null;
        User user = DATA_BASE.getUserByLogin(userLogin);
        String imageRelativePath = IMAGE_SERVICE.getProfileImageRelativePath(user);

        return Rythm.render(TEMPLATE_HTML, imageRelativePath);
    }
}
