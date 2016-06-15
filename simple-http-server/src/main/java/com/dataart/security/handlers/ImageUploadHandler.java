package com.dataart.security.handlers;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.services.ImageService;
import com.dataart.security.users.User;
import com.dataart.security.utils.UploadedImageFile;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.dataart.security.utils.Utils.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class ImageUploadHandler extends AbstractHttpHandler {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("POST");
    private static final String BOUNDARY_KEY = "boundary=";
    private static final ImageService IMAGE_SERVICE = ImageService.getInstance();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();

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

        String userLogin = httpExchange.getPrincipal() != null ? httpExchange.getPrincipal().getUsername() : null;
        User user = DATA_BASE.getUserByLogin(userLogin);

        if (user == null) {
            Logger.warn("Some issues with auth userLogin={}", userLogin);

            badRequest(HTTP_BAD_REQUEST, httpExchange);

            return;
        }

        // TODO: Check if user has required permissions to upload profile image

        String contentDelimiter = contentType.substring(contentType.indexOf(BOUNDARY_KEY) + BOUNDARY_KEY.length());
        UploadedImageFile uploadedImageFile = IMAGE_SERVICE.parseRequestBody(httpExchange, contentDelimiter);

        if (uploadedImageFile == null) {
            badRequest(HTTP_BAD_REQUEST, httpExchange, "Invalid request");

            return;
        }

        Path imageFilePath = IMAGE_SERVICE.writeImageFile(uploadedImageFile, userLogin);

        if (imageFilePath != null) {
            user.setProfileImage(imageFilePath);
        }

        redirect("/profile-page", httpExchange);
    }
}
