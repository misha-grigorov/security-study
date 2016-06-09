package com.dataart.security.oauth;

public enum OAuthApplicationType {
    WEB_APPLICATION("web application"),
    USER_AGENT_BASED_APPLICATION("user-agent-based application"),
    NATIVE_APPLICATION("native application");

    private final String name;

    OAuthApplicationType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
