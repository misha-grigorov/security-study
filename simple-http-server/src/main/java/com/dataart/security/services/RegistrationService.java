package com.dataart.security.services;

import com.dataart.security.db.InMemoryUserDataBase;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
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
        return "user-" + generateUniqueString(5).toLowerCase();
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
        User user = DATA_BASE.getUserByEmail(email);

        if (user != null) {
            Logger.info("User with such e-mail already registered. e-mail={}", email);

            return null;
        }

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

    public synchronized static RegistrationToken recoverUser(String email) {
        User user = DATA_BASE.getUserByEmail(email);

        if (user == null) {
            return null;
        }

        removeAllPreviousTokensForUser(user);

        if (user.getStatus() == UserStatus.BLOCKED) {
            Logger.info("Blocked user tried to recover password. userLogin={}", user.getLogin());

            return null;
        }

        RegistrationToken registrationToken = getUniqueRegistrationToken(user);

        REGISTRATION_TOKENS.put(registrationToken.getTokenId(), registrationToken);

        return registrationToken;
    }

    public synchronized static void removeAllPreviousTokensForUser(User user) {
        REGISTRATION_TOKENS.values().removeIf(registrationToken -> registrationToken.getUser().equals(user));
    }

    public synchronized static User checkRegistrationToken(String tokenId, boolean removeToken) {
        if (tokenId == null) {
            Logger.info("Invalid registration token");

            return null;
        }

        RegistrationToken registrationToken = REGISTRATION_TOKENS.get(tokenId);

        if (registrationToken == null) {
            Logger.info("Unknown registration token");

            return null;
        }

        if (removeToken) {
            REGISTRATION_TOKENS.remove(tokenId);
        }

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
