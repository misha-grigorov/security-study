package com.dataart.security.permissions;

public enum SimpleResourcePermission implements Permission {
    READ {
        @Override
        public boolean implies(Permission other) {
            return doesImply(other, READ);
        }
    },

    CREATE {
        @Override
        public boolean implies(Permission other) {
            return doesImply(other, CREATE);
        }
    },

    UPDATE {
        @Override
        public boolean implies(Permission other) {
            return doesImply(other, UPDATE);
        }
    },

    DELETE {
        @Override
        public boolean implies(Permission other) {
            return doesImply(other, DELETE);
        }
    },

    WILDCARD {
        @Override
        public boolean implies(Permission other) {
            return doesImply(other, WILDCARD);
        }
    };

    private static boolean doesImply(Permission other, SimpleResourcePermission current) {
        return other instanceof SimpleResourcePermission && (current.equals(other) || WILDCARD.equals(current));
    }
}
