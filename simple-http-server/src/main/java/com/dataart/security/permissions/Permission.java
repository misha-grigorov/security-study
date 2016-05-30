package com.dataart.security.permissions;

public interface Permission {
    boolean implies(Permission other);
}
