package com.james090500.VelocityWhitelist.models;

public enum AuthType {
    MICROSOFT("microsoft"),
    ELYBY("elyby"),
    DRASL("drasl");

    private final String name;

    AuthType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AuthType fromString(String text) {
        for (AuthType type : AuthType.values()) {
            if (type.name.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
