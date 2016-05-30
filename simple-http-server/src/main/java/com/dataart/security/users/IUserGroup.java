package com.dataart.security.users;

import com.dataart.security.permissions.Permission;

import java.util.List;

public interface IUserGroup {
    List<Permission> getPermissions();
}
