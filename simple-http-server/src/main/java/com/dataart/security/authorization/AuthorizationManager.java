package com.dataart.security.authorization;

import com.dataart.security.permissions.Permission;
import com.dataart.security.users.User;

public class AuthorizationManager {
    private static class AuthorizationManagerHolder {
        private static final AuthorizationManager HOLDER_INSTANCE = new AuthorizationManager();
    }

    public static AuthorizationManager getInstance() {
        return AuthorizationManagerHolder.HOLDER_INSTANCE;
    }

    private AuthorizationManager() {
    }

    public boolean isPermitted(User user, Permission requiredPermission) {
        for (Permission userPermission : user.getUserGroup().getPermissions()) {
            if (userPermission.implies(requiredPermission)) {
                return true;
            }
        }

        return false;
    }
}
