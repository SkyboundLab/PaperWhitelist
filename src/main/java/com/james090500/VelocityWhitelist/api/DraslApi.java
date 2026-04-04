package com.james090500.VelocityWhitelist.api;

import com.google.gson.JsonArray;
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

public class DraslApi {

    /**
     * Get UUID from Drasl username
     *
     * @param username The username to lookup
     * @param draslUrl The base URL of the Drasl instance
     * @param token The bearer token for authentication
     * @return The UUID or null if not found
     */
    public static UUID getUUID(String username, String draslUrl, String token) {
        if (draslUrl == null || draslUrl.isEmpty() || token == null || token.isEmpty()) {
            return null;
        }

        JsonArray players = getApiData(draslUrl, token);
        if (players != null) {
            for (JsonElement element : players) {
                if (element.isJsonObject()) {
                    JsonObject player = element.getAsJsonObject();
                    JsonElement nameElement = player.get("name");
                    if (nameElement != null && !nameElement.isJsonNull()) {
                        String playerName = nameElement.getAsString();
                        if (playerName.equalsIgnoreCase(username)) {
                            JsonElement uuidElement = player.get("uuid");
                            if (uuidElement != null && !uuidElement.isJsonNull()) {
                                return UUID.fromString(uuidElement.getAsString());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Request API call for all players from Drasl
     *
     * @param draslUrl The base URL of the Drasl instance
     * @param token The bearer token for authentication
     * @return The response data as a JSON array
     */
    private static JsonArray getApiData(String draslUrl, String token) {
        HttpURLConnection conn = null;
        try {
            // Remove trailing slash if present
            String baseUrl = draslUrl.endsWith("/") ? draslUrl.substring(0, draslUrl.length() - 1) : draslUrl;
            URI uri = URI.create(baseUrl + "/drasl/api/v2/players");

            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestProperty("User-Agent", "velocitywhitelist/1.0.2-SNAPSHOT");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null || code < 200 || code >= 300) return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[2048];
                int n;
                while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
                return JsonParser.parseString(sb.toString()).getAsJsonArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
