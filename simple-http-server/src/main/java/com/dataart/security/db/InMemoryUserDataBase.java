package com.dataart.security.db;

import com.dataart.security.users.User;
import com.dataart.security.users.UserStatus;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserDataBase {
    private static final Map<String, User> USERS = new HashMap<>();

    static {
        User admin = new User();

        admin.setLogin("admin-ubMzc"); // unique login
//        String login = "admin-" + RandomStringUtils.random(5, 32, 126, true, true, null, new SecureRandom());
        admin.setPassword("$3cur!tY-SSZ2Q");
        admin.setEmail("admin@localhost");
        admin.setStatus(UserStatus.ACTIVE);

        USERS.put(admin.getLogin(), admin);
    }

    private static class InMemoryUserDataBaseHolder {
        private static final InMemoryUserDataBase HOLDER_INSTANCE = new InMemoryUserDataBase();
    }

    public static InMemoryUserDataBase getInstance() {
        return InMemoryUserDataBaseHolder.HOLDER_INSTANCE;
    }

    public synchronized User getUserByLogin(String login) {
        return USERS.get(login);
    }
}
