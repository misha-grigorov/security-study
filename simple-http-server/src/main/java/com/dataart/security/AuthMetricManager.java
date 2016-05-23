package com.dataart.security;

import com.dataart.security.services.NotificationService;
import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

public class AuthMetricManager {
    private static final int MAX_ATTEMPTS = 3;
    private static final Map<User, Integer> FAILED_LOGIN_MAP = new HashMap<>();

    private static class LoginAttemptsMetricHolder {
        private static final AuthMetricManager HOLDER_INSTANCE = new AuthMetricManager();
    }

    public static AuthMetricManager getInstance() {
        return LoginAttemptsMetricHolder.HOLDER_INSTANCE;
    }

    private AuthMetricManager() {
    }

    public synchronized UserStatus loginFail(User user) {
        Logger.info("user={} failed to log in time={}", user.getLogin(), System.currentTimeMillis());

        Integer failedAttempts = FAILED_LOGIN_MAP.get(user);

        if (failedAttempts == null) {
            FAILED_LOGIN_MAP.put(user, 1);

            return UserStatus.ACTIVE;
        }

        int failedAttemptsCounter = ++failedAttempts;
        FAILED_LOGIN_MAP.put(user, failedAttemptsCounter);

        if (failedAttemptsCounter == MAX_ATTEMPTS) {
            Logger.info("user={} was blocked, maximum failed log in attempts reached!", user.getLogin());

            user.setStatus(UserStatus.BLOCKED);

            NotificationService.sendEmail(user.getEmail(), "Your account was temporarily disabled due to strange activity!" +
                    "\nPlease contact the administrator");

            return UserStatus.BLOCKED;
        }

        return UserStatus.ACTIVE;
    }

    public synchronized void loginSuccess(User user) {
        Logger.info("user={} logged time={}", user.getLogin(), System.currentTimeMillis());

        FAILED_LOGIN_MAP.put(user, 0);
    }
}
