package com.dataart.security.users;

import com.dataart.security.utils.Utils;

import java.nio.file.Path;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class User {
    private String login;
    private String email;
    private String password;
    private String salt;
    private Path profileImage;
    private UserStatus status;
    private UserGroup userGroup;

    public User(String login, String email, String password) {
        this.login = login;
        this.email = email;
        this.status = UserStatus.NOT_VERIFIED;
        this.userGroup = UserGroup.USER;

        setPassword(password);
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.salt = Utils.generateSecureRandom();
        this.password = Utils.hashPassword(password.toCharArray(), salt.getBytes(UTF_8));
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public Path getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Path profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, email, password);
    }
}
