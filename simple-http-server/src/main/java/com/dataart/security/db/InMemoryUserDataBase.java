package com.dataart.security.db;

import com.dataart.security.users.User;
import com.dataart.security.users.UserGroup;
import com.dataart.security.users.UserStatus;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserDataBase {
    private static final Map<String, User> USERS = new HashMap<>();

    static {
        User admin = new User("admin-ubmzc", "admin@gmail.com", "$3cur!tY-SSZ2Q");

        admin.setStatus(UserStatus.ACTIVE);
        admin.setUserGroup(UserGroup.ADMIN);

        USERS.put(admin.getLogin(), admin);
    }

    private static class InMemoryUserDataBaseHolder {
        private static final InMemoryUserDataBase HOLDER_INSTANCE = new InMemoryUserDataBase();
    }

    public static InMemoryUserDataBase getInstance() {
        return InMemoryUserDataBaseHolder.HOLDER_INSTANCE;
    }

    public synchronized User getUserByLogin(String login) {
        if (login == null) {
            return null;
        }

        return USERS.get(login.toLowerCase());
    }

    public synchronized User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }

        for (User user : USERS.values()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }

        return null;
    }

    public synchronized void registerNewUser(User newUser) {
        if (newUser == null) {
            return;
        }

        USERS.put(newUser.getLogin(), newUser);
    }
}
