package com.dataart.security.services;

import org.pmw.tinylog.Logger;

public class NotificationService {

    private NotificationService() {
    }

    public static void sendEmail(String emailAddress, String message) {
        Logger.info("new email! email={} message={}", emailAddress, message);
    }
}
