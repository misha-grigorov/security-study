package com.dataart.security.services;

import com.dataart.security.users.User;
import com.dataart.security.utils.KMPMatch;
import com.dataart.security.utils.UploadedImageFile;
import com.dataart.security.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import org.pmw.tinylog.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ImageService {
    public static final Path IMAGES;

    private static final int MAX_IMAGE_SIZE_BYTES = 2097152; // 2 MB
    private static final String FILENAME_KEY = "filename=\"";
    private static final String CONTENT_TYPE_KEY = "Content-Type: ";
    private static final String CRLF = "\r\n";

    static {
        try {
            IMAGES = Paths.get("images").toRealPath();

            if (Files.notExists(IMAGES)) {
                Files.createDirectory(IMAGES);
            }
        } catch (IOException e) {
            Logger.error("Failed to create images directory");

            throw new RuntimeException(e);
        }
    }

    private static class ImageServiceHolder {
        private static final ImageService HOLDER_INSTANCE = new ImageService();
    }

    public static ImageService getInstance() {
        return ImageService.ImageServiceHolder.HOLDER_INSTANCE;
    }

    public UploadedImageFile parseRequestBody(HttpExchange httpExchange, String contentDelimiter) {
        if (httpExchange == null || contentDelimiter == null) {
            return null;
        }

        byte[] fileContent = Utils.readBytesFromRequestBody(httpExchange.getRequestBody(), false);

        return parseContent(fileContent, contentDelimiter);
    }

    public Path writeImageFile(UploadedImageFile uploadedImageFile, String userLogin) {
        Path imageFilePath = Paths.get(IMAGES.toString(),
                DatatypeConverter.printHexBinary((userLogin + "-image").getBytes(UTF_8)) + ".jpg");

        try {
            Files.write(imageFilePath, uploadedImageFile.getContent());

            return imageFilePath;
        } catch (IOException e) {
            Logger.error(e);
        }

        return null;
    }

    public String getProfileImageRelativePath(User user) {
        try {
            if (user == null || user.getProfileImage() == null || Files.notExists(user.getProfileImage()) ||
                    !user.getProfileImage().toRealPath().startsWith(IMAGES)) {
                return null;
            }
        } catch (IOException e) {
            Logger.warn(e);
        }

        String result = IMAGES.getFileName() + "/" + user.getProfileImage().getFileName().toString();

        Path resultPath = null;

        try {
            resultPath = Paths.get(result).toRealPath();
        } catch (IOException e) {
            Logger.warn(e);
        }

        if (resultPath == null || !resultPath.startsWith(IMAGES) || Files.notExists(resultPath)) {
            return null;
        }

        return result;
    }

    public byte[] readImage(HttpExchange httpExchange) {
        String contextPath = httpExchange.getHttpContext().getPath() + "/";
        String requestPath = httpExchange.getRequestURI().getPath();

        int startIndex = requestPath.indexOf(contextPath);

        if (startIndex == -1) {
            return null;
        }

        String imageFileName = requestPath.substring(startIndex + contextPath.length());

        if (!imageFileName.endsWith(".jpg") || imageFileName.contains("%00")) {
            return null;
        }

        Path path = null;

        try {
            path = Paths.get(IMAGES.toString(), imageFileName).toRealPath();
        } catch (IOException e) {
            Logger.warn(e);
        }

        if (path == null || !path.startsWith(IMAGES) || Files.notExists(path)) {
            return null;
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            Logger.warn(e);
        }

        return null;
    }

    protected UploadedImageFile parseContent(byte[] fileContent, String contentDelimiter) {
        String content = new String(fileContent, UTF_8);
        String fileName = getFileName(content);
        String contentTypeFromImage = getContentTypeFromImage(content);
        byte[] imageData = getImageData(fileContent, contentDelimiter);

        if (fileName == null || contentTypeFromImage == null || !fileName.endsWith(".jpg") ||
                !contentTypeFromImage.equals("image/jpeg") || !checkImageDataHasValidContent(imageData)) {
            return null;
        }

        return new UploadedImageFile(fileName, contentTypeFromImage, imageData);
    }

    private boolean checkImageDataHasValidContent(byte[] imageData) {
        // TODO: check image metadata and content
        return imageData != null && imageData.length <= MAX_IMAGE_SIZE_BYTES;
    }

    protected String getFileName(String fileContent) {
        int fileNameKeyIndex = fileContent.indexOf(FILENAME_KEY);

        if (fileNameKeyIndex == -1) {
            return null;
        }

        int startIndex = fileNameKeyIndex + FILENAME_KEY.length();
        int endIndex = fileContent.indexOf("\"", startIndex);

        if (endIndex == -1) {
            return null;
        }

        return fileContent.substring(startIndex, endIndex);
    }

    protected String getContentTypeFromImage(String fileContent) {
        int contentTypeIndex = fileContent.indexOf(CONTENT_TYPE_KEY);

        if (contentTypeIndex == -1) {
            return null;
        }

        int startIndex = contentTypeIndex + CONTENT_TYPE_KEY.length();
        int endIndex = fileContent.indexOf(CRLF, startIndex);

        if (endIndex == -1) {
            return null;
        }

        return fileContent.substring(startIndex, endIndex);
    }

    protected byte[] getImageData(byte[] fileContent, String contentDelimiter) {
        int imageDataIndex = KMPMatch.indexOf(fileContent, (CRLF + CRLF).getBytes());

        if (imageDataIndex == -1) {
            return null;
        }

        int startIndex = imageDataIndex + (CRLF + CRLF).length();
        byte[] buffer = Arrays.copyOfRange(fileContent, startIndex, fileContent.length);
        int endIndex = KMPMatch.indexOf(buffer, US_ASCII.encode("--" + contentDelimiter).array()) + startIndex;

        if (endIndex == -1) {
            return null;
        }

        return Arrays.copyOfRange(fileContent, startIndex, endIndex);
    }
}
