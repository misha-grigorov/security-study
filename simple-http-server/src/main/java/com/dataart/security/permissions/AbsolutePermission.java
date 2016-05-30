package com.dataart.security.permissions;

public enum AbsolutePermission implements Permission {
    ABSOLUTE_PERMISSION {
        @Override
        public boolean implies(Permission other) {
            return true;
        }
    }
}
