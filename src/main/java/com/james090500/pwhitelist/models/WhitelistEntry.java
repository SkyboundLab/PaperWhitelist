package com.james090500.pwhitelist.models;

import java.util.UUID;

public class WhitelistEntry {
    private UUID uuid;
    private String authType; // "microsoft", "elyby", or "drasl"

    public WhitelistEntry() {
    }

    public WhitelistEntry(UUID uuid, String authType) {
        this.uuid = uuid;
        this.authType = authType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhitelistEntry that = (WhitelistEntry) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
