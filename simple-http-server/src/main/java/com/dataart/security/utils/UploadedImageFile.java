package com.dataart.security.utils;

import java.util.Arrays;
import java.util.Objects;

public class UploadedImageFile {
    private String fileName;
    private String contentType;
    private byte[] content;

    public UploadedImageFile(String fileName, String contentType, byte[] content) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadedImageFile that = (UploadedImageFile) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(contentType, that.contentType) &&
                Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, contentType, content);
    }
}
