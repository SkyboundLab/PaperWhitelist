package com.james090500.VelocityWhitelist.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.james090500.VelocityWhitelist.VelocityWhitelist;
import com.james090500.VelocityWhitelist.api.DraslApi;
import com.james090500.VelocityWhitelist.api.ElybyApi;
import com.james090500.VelocityWhitelist.config.Configs;
import com.james090500.VelocityWhitelist.models.AuthType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record MinecraftApi(VelocityWhitelist velocityWhitelist) {

    /**
     * Returns player UUID using the specified auth type
     *
     * @param username Username to get UUID from
     * @param authType The authentication type to use
     * @return Players uuid
     */
    public UUID getUUID(String username, AuthType authType) {
        //Try get online player first
        if (velocityWhitelist.getServer().getPlayer(username).isPresent()) {
            return velocityWhitelist.getServer().getPlayer(username).get().getUniqueId();
        }

        // Use appropriate API based on auth type
        return switch (authType) {
            case MICROSOFT -> getUUIDMicrosoft(username);
            case ELYBY -> ElybyApi.getUUID(username);
            case DRASL -> {
                String draslUrl = Configs.getConfig().getDraslUrl();
                String draslToken = Configs.getConfig().getDraslToken();
                yield DraslApi.getUUID(username, draslUrl, draslToken);
            }
        };
    }

    /**
     * Returns player UUID (Microsoft/Mojang - default)
     *
     * @param username Username to get UUID from
     * @return Players uuid
     */
    public UUID getUUID(String username) {
        return getUUID(username, AuthType.MICROSOFT);
    }

    /**
     * Get UUID from Microsoft/Mojang API
     *
     * @param username Username to get UUID from
     * @return Players uuid
     */
    private UUID getUUIDMicrosoft(String username) {
        // Fallback to API
        JsonObject playerElement = getApiData(username);
        if (playerElement != null) {
            JsonElement playerUUID = playerElement.get("full_uuid");
            if (playerUUID != null && !playerUUID.isJsonNull()) {
                return UUID.fromString(playerUUID.getAsString());
            }
        }
        return null;
    }

    /**
     * Request API call for user data
     *
     * @param data The username/uuid to send
     * @return The response data
     */
    private JsonObject getApiData(String data) {
        HttpURLConnection conn = null;
        try {
            URI uri = URI.create("https://api.minecraftapi.net/v3/profile/" + data + "?params=[full_uuid,name]");

            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestProperty("User-Agent", "velocitywhitelist/1.0.2-SNAPSHOT");

            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null || code < 200 || code >= 300) return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[2048];
                int n;
                while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
                return JsonParser.parseString(sb.toString()).getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}