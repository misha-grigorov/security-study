package com.dataart.security;

import org.boon.json.JsonFactory;
import org.pmw.tinylog.Logger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientApplication {
    public static final String SERVER_URL = "http://127.0.0.1:55555";
    public static final String USER_AGENT = "Mozilla/5.0";
    public static final String BASIC_AUTH = "Basic " + Base64.getEncoder().encodeToString(("admin" + ":" + "password").getBytes(UTF_8));

    public static void main(String[] args) throws IOException {
        getRequestToServerRoot();
        postRequestToServerJson();
        postRequestToServerForm();
        postFileToServer();
    }

    public static void getRequestToServerRoot() throws IOException {
        String requestUrl = SERVER_URL + "/";
        URL connectUrl = new URL(requestUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connectUrl.openConnection();

        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

        String response = readResponse(httpURLConnection.getInputStream());

        Logger.info("GET REQUEST TO " + requestUrl);
        Logger.info("RESPONSE FROM SERVER: {} {} \n{}", httpURLConnection.getResponseCode(), httpURLConnection.getResponseMessage(),
                response);
    }

    public static void postRequestToServerJson() throws IOException {
        String requestUrl = SERVER_URL + "/json";
        URL connectUrl = new URL(requestUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connectUrl.openConnection();

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpURLConnection.setRequestProperty("Authorization", BASIC_AUTH);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);

        String jsonStr = JsonFactory.toJson("{\"power\":0.5}");

        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        dataOutputStream.writeBytes(jsonStr);
        dataOutputStream.flush();
        dataOutputStream.close();

        String response = readResponse(httpURLConnection.getInputStream());

        Logger.info("POST JSON " + jsonStr + " TO " + requestUrl + " AUTH IS " + BASIC_AUTH);
        Logger.info("RESPONSE FROM SERVER: {} {} \n{}", httpURLConnection.getResponseCode(), httpURLConnection.getResponseMessage(),
                response);
    }

    public static void postRequestToServerForm() throws IOException {
        String requestUrl = SERVER_URL + "/form";
        URL connectUrl = new URL(requestUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connectUrl.openConnection();

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpURLConnection.setRequestProperty("Authorization", BASIC_AUTH);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setDoOutput(true);

        Map<String, String> params = new HashMap<>();

        params.put("name", "Peter Lee");
        params.put("address", "#123 Happy Ave");
        params.put("Language", "C++");

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), UTF_8.name());
            String value = URLEncoder.encode(entry.getValue(), UTF_8.name());

            stringBuilder.append(key).append("=").append(value).append("&");
        }

        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("&"));

        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        dataOutputStream.writeBytes(stringBuilder.toString());
        dataOutputStream.flush();
        dataOutputStream.close();

        String response = readResponse(httpURLConnection.getInputStream());

        Logger.info("POST FORM " + stringBuilder.toString() + " TO " + requestUrl + " AUTH IS " + BASIC_AUTH);
        Logger.info("RESPONSE FROM SERVER: {} {} \n{}", httpURLConnection.getResponseCode(), httpURLConnection.getResponseMessage(),
                response);
    }

    public static void postFileToServer() throws IOException {
        String requestUrl = SERVER_URL + "/upload";
        URL connectUrl = new URL(requestUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connectUrl.openConnection();

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpURLConnection.setRequestProperty("Authorization", BASIC_AUTH);
        httpURLConnection.setDoOutput(true);

        File file = new File("D:\\upload.json");
        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";

        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream output = httpURLConnection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, UTF_8), true);

        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(file.toPath(), output);
        output.flush();
        writer.append(CRLF).flush();
        writer.append("--").append(boundary).append("--").append(CRLF).flush();

        String response = readResponse(httpURLConnection.getInputStream());

        Logger.info("POST FORM " + file.getName() + " TO " + requestUrl + " AUTH IS " + BASIC_AUTH);
        Logger.info("RESPONSE FROM SERVER: {} {} \n{}", httpURLConnection.getResponseCode(), httpURLConnection.getResponseMessage(),
                response);
    }

    public static String readResponse(InputStream stream) {
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream inputStream = stream) {
            int buffer;
            InputStreamReader streamReader = new InputStreamReader(inputStream, UTF_8);

            while ((buffer = streamReader.read()) != -1) {
                stringBuilder.append((char) buffer);
            }
        } catch (IOException e) {
            Logger.warn(e.getMessage());
        }

        return stringBuilder.toString();
    }
}
