package com.dataart.security;

import org.boon.json.JsonFactory;
import org.pmw.tinylog.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
