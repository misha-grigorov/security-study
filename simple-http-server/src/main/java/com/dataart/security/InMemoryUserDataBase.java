package com.dataart.security;

import com.dataart.security.users.User;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserDataBase {
    private static final ConcurrentHashMap<String, User> USERS = new ConcurrentHashMap<>();

    static {
        User admin = new User();

        admin.setLogin("admin");
        admin.setPassword("password");
        admin.setEmail("admin@localhost");

        USERS.put(admin.getLogin(), admin);
    }

    private static class InMemoryUserDataBaseHolder {
        private static final InMemoryUserDataBase HOLDER_INSTANCE = new InMemoryUserDataBase();
    }

    public static InMemoryUserDataBase getInstance() {
        return InMemoryUserDataBaseHolder.HOLDER_INSTANCE;
    }

    public User getUserByLogin(String login) {
        return USERS.get(login);
    }
}
