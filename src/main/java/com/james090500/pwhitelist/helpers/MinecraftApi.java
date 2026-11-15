package com.james090500.pwhitelist.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.james090500.pwhitelist.Pwhitelist;
import com.james090500.pwhitelist.api.DraslApi;
import com.james090500.pwhitelist.api.ElybyApi;
import com.james090500.pwhitelist.config.Configs;
import com.james090500.pwhitelist.models.AuthType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record MinecraftApi(Pwhitelist paperWhitelist) {

    /**
     * HTTP only; the online-player shortcut lives in WhitelistHelper (Bukkit.getPlayerExact must run on the main thread).
     *
     * @param username Username to get UUID from
     * @return Players uuid
     */
    public UUID getUUID(String username, AuthType authType) {
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

    public UUID getUUID(String username) {
        return getUUID(username, AuthType.MICROSOFT);
    }

    private UUID getUUIDMicrosoft(String username) {
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
            conn.setRequestProperty("User-Agent", "pwhitelist/1.0.0");

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
