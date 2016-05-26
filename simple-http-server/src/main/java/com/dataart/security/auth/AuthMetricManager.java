package com.dataart.security.auth;

import com.dataart.security.services.NotificationService;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

public class AuthMetricManager {
    private static final int MAX_ATTEMPTS = 3;
    private static final int SUSPENDED_TIME_OUT_MINUTES = 15;
    private static final int SUSPENDED_TIME_OUT_MILLIS = SUSPENDED_TIME_OUT_MINUTES * 60 * 1000;
    private static final Map<User, LoginAttempt> FAILED_LOGIN_MAP = new HashMap<>();

    private static class LoginAttemptsMetricHolder {
        private static final AuthMetricManager HOLDER_INSTANCE = new AuthMetricManager();
    }

    public static AuthMetricManager getInstance() {
        return LoginAttemptsMetricHolder.HOLDER_INSTANCE;
    }

    private AuthMetricManager() {
    }

    public synchronized UserStatus passwordChangeFail(User user) {
        long timeStamp = System.currentTimeMillis();
        Logger.info("User tried to change password, but provided invalid current password. userLogin={}, time={}", user.getLogin(), timeStamp);

        UserStatus result = doFail(user);

        if (result == UserStatus.SUSPENDED) {
            Logger.info("User was temporary suspended, maximum password change attempts reached! userLogin={}", user.getLogin());
        }

        return result;
    }

    public synchronized UserStatus loginFail(User user) {
        long timeStamp = System.currentTimeMillis();
        Logger.info("User failed to log in. user={}, time={}", user.getLogin(), timeStamp);

        UserStatus result = doFail(user);

        if (result == UserStatus.SUSPENDED) {
            Logger.info("User was temporary suspended, maximum login attempts reached. userLogin={}!", user.getLogin());
        }

        return result;
    }

    public synchronized void passwordChangeSuccess(User user) {
        Logger.info("User successfully changed password. userLogin={} time={}", user.getLogin(), System.currentTimeMillis());

        doSuccess(user);
    }

    public synchronized void loginSuccess(User user) {
        Logger.info("User successfully logged in. userLogin={}  time={}", user.getLogin(), System.currentTimeMillis());

        doSuccess(user);
    }

    public synchronized boolean checkUserStatus(User user) {
        if (user == null) {
            Logger.info("Invalid user login was provided. userLogin=null");

            return false;
        }

        UserStatus userStatus = user.getStatus();

        if (userStatus == UserStatus.ACTIVE) {
            return true;
        }

        LoginAttempt loginAttempt = FAILED_LOGIN_MAP.get(user);

        if (userStatus == UserStatus.SUSPENDED && loginAttempt.getTimeStamp() + SUSPENDED_TIME_OUT_MILLIS < System.currentTimeMillis()) {
            Logger.info("User was unlocked after suspension time expired. userLogin={}", user.getLogin());

            user.setStatus(UserStatus.ACTIVE);
            loginAttempt.resetCounter();

            return true;
        } else {
            Logger.info("Suspended user tried to log in. userLogin={}", user.getLogin());
        }

        return false;
    }

    private UserStatus doFail(User user) {
        LoginAttempt loginAttempt = FAILED_LOGIN_MAP.get(user);

        if (loginAttempt == null) {
            FAILED_LOGIN_MAP.put(user, new LoginAttempt());

            return user.getStatus();
        }

        int failedAttemptsCounter = loginAttempt.increaseAndGetCounter();

        if (failedAttemptsCounter >= MAX_ATTEMPTS) {
            user.setStatus(UserStatus.SUSPENDED);

            NotificationService.sendEmail(user.getEmail(), "Your account was temporarily disabled due to strange activity!" +
                    "\nIt will be automatically unlocked after " + SUSPENDED_TIME_OUT_MINUTES + " minutes.");

            return user.getStatus();
        }

        return user.getStatus();
    }

    private void doSuccess(User user) {
        if (FAILED_LOGIN_MAP.containsKey(user)) {
            FAILED_LOGIN_MAP.get(user).resetCounter();
        } else {
            FAILED_LOGIN_MAP.put(user, new LoginAttempt(0));
        }
    }
}
