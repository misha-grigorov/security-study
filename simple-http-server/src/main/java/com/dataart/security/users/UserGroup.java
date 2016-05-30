package com.dataart.security.users;

import com.dataart.security.permissions.AbsolutePermission;
import com.dataart.security.permissions.Permission;
import com.dataart.security.permissions.SimpleResourcePermission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum UserGroup implements IUserGroup {
    ADMIN {
        @Override
        public List<Permission> getPermissions() {
            return Collections.singletonList(AbsolutePermission.ABSOLUTE_PERMISSION);
        }
    },
    USER {
        @Override
        public List<Permission> getPermissions() {
            return Collections.singletonList(SimpleResourcePermission.READ);
        }
    },
    OWNER {
        @Override
        public List<Permission> getPermissions() {
            return new ArrayList<>();
        }
    }
}
