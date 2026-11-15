package com.james090500.pwhitelist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.james090500.pwhitelist.Pwhitelist;
import com.james090500.pwhitelist.models.WhitelistEntry;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Configs {

    private static final Pwhitelist paperWhitelist = Pwhitelist.getInstance();

    private static Config config;
    private static Set<WhitelistEntry> whitelist = new HashSet<>();
    private static Path configFile;
    private static Path whitelistFile;

    public static Config getConfig() {
        return config;
    }

    public static Set<WhitelistEntry> getWhitelist() {
        return whitelist;
    }

    public static void loadConfigs() {
        File dataFolder = new File(paperWhitelist.getDataFolder().getParentFile(), "paperwhitelist");
        configFile = Path.of(dataFolder + "/config.toml");
        whitelistFile = Path.of(dataFolder + "/whitelist.json");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        if (!configFile.toFile().exists()) {
            try {
                Files.copy(Pwhitelist.class.getResourceAsStream("/config.toml"), configFile);
            } catch (Exception e) {
                paperWhitelist.getLogger().severe("Error loading config.toml");
                e.printStackTrace();
            }
        }

        config = new Toml().read(configFile.toFile()).to(Config.class);

        if (whitelistFile.toFile().exists()) {
            try {
                String json = Files.readString(whitelistFile, StandardCharsets.UTF_8);
                try {
                    Type whitelistSetType = new TypeToken<HashSet<WhitelistEntry>>() {}.getType();
                    whitelist = new Gson().fromJson(json, whitelistSetType);
                    if (whitelist == null) {
                        whitelist = new HashSet<>();
                    }
                } catch (Exception e) {
                    Type oldWhitelistSetType = new TypeToken<HashSet<UUID>>() {}.getType();
                    Set<UUID> oldWhitelist = new Gson().fromJson(json, oldWhitelistSetType);
                    if (oldWhitelist != null) {
                        whitelist = oldWhitelist.stream()
                                .map(uuid -> new WhitelistEntry(uuid, "microsoft"))
                                .collect(Collectors.toSet());
                        saveWhitelist(paperWhitelist);
                        paperWhitelist.getLogger().info("Migrated whitelist to new format with auth types");
                    } else {
                        whitelist = new HashSet<>();
                    }
                }
            } catch (Exception e) {
                paperWhitelist.getLogger().severe("Error loading whitelist.json");
                e.printStackTrace();
                whitelist = new HashSet<>();
            }
        }
    }

    public static void saveConfig(Pwhitelist paperWhitelist) {
        try {
            new TomlWriter().write(config, configFile.toFile());
        } catch (Exception e) {
            paperWhitelist.getLogger().severe("Error writing config.toml");
            e.printStackTrace();
        }
    }

    public static void saveWhitelist(Pwhitelist paperWhitelist) {
        try {
            FileWriter fileWriter = new FileWriter(whitelistFile.toFile());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(whitelist, fileWriter);

            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            paperWhitelist.getLogger().severe("Error writing whitelist.json");
            e.printStackTrace();
        }
    }

    public static class Config {

        private boolean enabled;
        private String message;

        private Auth auth;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMessage() {
            return message != null && !message.isEmpty() ? message : "&cWhitelist enabled!";
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDraslUrl() {
            return auth != null && auth.drasl != null ? auth.drasl.url : "";
        }

        public String getDraslToken() {
            return auth != null && auth.drasl != null ? auth.drasl.token : "";
        }

        public static class Auth {
            private DraslConfig drasl;
        }

        public static class DraslConfig {
            private String url;
            private String token;
        }
    }
}
