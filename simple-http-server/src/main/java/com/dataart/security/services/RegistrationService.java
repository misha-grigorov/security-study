package com.dataart.security.services;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.users.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.pmw.tinylog.Logger;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class RegistrationService {
    private static final long TOKEN_VALID_TIMEOUT_MILLIS = 120000; // 2 minutes
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final InMemoryUserDataBase DATA_BASE = InMemoryUserDataBase.getInstance();
    private static final Map<String, RegistrationToken> REGISTRATION_TOKENS = new HashMap<>();

    private RegistrationService() {
    }

    public static String getUniqueUserLogin() {
        return "user-" + generateUniqueString(5);
    }

    public static String getUniquePassword() {
        return generateUniqueString(24);
    }

    public static String getUniqueRegistrationLink(RegistrationToken registrationToken) {

        return "";
    }

    public static RegistrationToken getUniqueRegistrationToken(User user) {
        String tokenId = generateUniqueString(64);

        return new RegistrationToken(tokenId, user, System.currentTimeMillis() + TOKEN_VALID_TIMEOUT_MILLIS);
    }

    public synchronized static RegistrationToken registerNewUser(String email) {
        String newUserLogin = null;

        do {
            newUserLogin = getUniqueUserLogin();
        } while (DATA_BASE.getUserByLogin(newUserLogin) != null);

        User newUser = new User(newUserLogin, email, getUniquePassword());

        DATA_BASE.registerNewUser(newUser);

        RegistrationToken registrationToken = getUniqueRegistrationToken(newUser);

        REGISTRATION_TOKENS.put(registrationToken.getTokenId(), registrationToken);

        return registrationToken;
    }

    public synchronized static User checkRegistrationToken(String tokenId) {
        if (tokenId == null) {
            Logger.info("Invalid registration token");

            return null;
        }

        RegistrationToken registrationToken = REGISTRATION_TOKENS.get(tokenId);

        if (registrationToken == null) {
            Logger.info("Unknown registration token");

            return null;
        }

        REGISTRATION_TOKENS.remove(tokenId);

        if (registrationToken.getValidUntil() - System.currentTimeMillis() >= TOKEN_VALID_TIMEOUT_MILLIS) {
            Logger.info("Registration token expired, registration failed");

            return null;
        }

        return registrationToken.getUser();
    }

    private static String generateUniqueString(int length) {
        return RandomStringUtils.random(length, 32, 126, true, true, null, SECURE_RANDOM);
    }
}
