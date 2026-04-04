package com.james090500.VelocityWhitelist.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ElybyApi {

    /**
     * Get UUID from Ely.by username
     *
     * @param username The username to lookup
     * @return The UUID or null if not found
     */
    public static UUID getUUID(String username) {
        JsonObject playerElement = getApiData(username);
        if (playerElement != null) {
            JsonElement playerId = playerElement.get("id");
            if (playerId != null && !playerId.isJsonNull()) {
                String id = playerId.getAsString();
                // Convert the ID string to UUID format (add dashes)
                return UUID.fromString(id.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"
                ));
            }
        }
        return null;
    }

    /**
     * Request API call for user data from Ely.by
     *
     * @param username The username to lookup
     * @return The response data
     */
    private static JsonObject getApiData(String username) {
        HttpURLConnection conn = null;
        try {
            URI uri = URI.create("https://authserver.ely.by/api/users/profiles/minecraft/" + username);

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
