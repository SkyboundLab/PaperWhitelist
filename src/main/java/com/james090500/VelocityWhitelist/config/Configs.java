package com.james090500.VelocityWhitelist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.james090500.VelocityWhitelist.VelocityWhitelist;
import com.james090500.VelocityWhitelist.models.WhitelistEntry;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Configs {

    private static final VelocityWhitelist velocityWhitelist = VelocityWhitelist.getInstance();

    @Getter private static Config config;
    @Getter private static Set<WhitelistEntry> whitelist = new HashSet<>();
    private static Path configFile;
    private static Path whitelistFile;

    /**
     * Loads the config files.
     */
    public static void loadConfigs() {
        configFile = Path.of(velocityWhitelist.getDataDirectory() + "/config.toml");
        whitelistFile = Path.of(velocityWhitelist.getDataDirectory() + "/whitelist.json");

        //Create data directory
        if(!velocityWhitelist.getDataDirectory().toFile().exists()) {
            velocityWhitelist.getDataDirectory().toFile().mkdir();
        }

        //Load the config.toml to memory
        if(!configFile.toFile().exists()) {
            try (InputStream in = VelocityWhitelist.class.getResourceAsStream("/config.toml")) {
                Files.copy(in, configFile);
            } catch (Exception e) {
                velocityWhitelist.getLogger().error("Error loading config.toml");
                e.printStackTrace();
            }
        }
        config = new Toml().read(configFile.toFile()).to(Config.class);

        //Load whitelist players to memory (if any)
        if(whitelistFile.toFile().exists()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(whitelistFile.toFile()), "UTF8")) {
                // Try to load as new format first
                try {
                    Type whitelistSetType = new TypeToken<HashSet<WhitelistEntry>>(){}.getType();
                    whitelist = new Gson().fromJson(inputStreamReader, whitelistSetType);
                    if (whitelist == null) {
                        whitelist = new HashSet<>();
                    }
                } catch (Exception e) {
                    // Fallback to old format (Set<UUID>) and migrate
                    inputStreamReader.close();
                    try (InputStreamReader inputStreamReader2 = new InputStreamReader(new FileInputStream(whitelistFile.toFile()), "UTF8")) {
                        Type oldWhitelistSetType = new TypeToken<HashSet<UUID>>(){}.getType();
                        Set<UUID> oldWhitelist = new Gson().fromJson(inputStreamReader2, oldWhitelistSetType);
                        if (oldWhitelist != null) {
                            whitelist = oldWhitelist.stream()
                                    .map(uuid -> new WhitelistEntry(uuid, "microsoft"))
                                    .collect(Collectors.toSet());
                            // Save in new format
                            saveWhitelist(velocityWhitelist);
                            velocityWhitelist.getLogger().info("Migrated whitelist to new format with auth types");
                        } else {
                            whitelist = new HashSet<>();
                        }
                    }
                }
            } catch (Exception e) {
                velocityWhitelist.getLogger().error("Error loading whitelist.json");
                e.printStackTrace();
                whitelist = new HashSet<>();
            }
        }
    }

    /**
     * Save the config
     * @param velocityWhitelist
     */
    public static void saveConfig(VelocityWhitelist velocityWhitelist) {
        try {
            new TomlWriter().write(config, configFile.toFile());
        } catch (Exception e) {
            velocityWhitelist.getLogger().error("Error writing config.toml");
            e.printStackTrace();
        }
    }

    /**
     * Save the whitelist file
     * @param velocityWhitelist
     */
    public static void saveWhitelist(VelocityWhitelist velocityWhitelist) {
        try {
            FileWriter fileWriter = new FileWriter(whitelistFile.toFile());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(whitelist, fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            velocityWhitelist.getLogger().error("Error writing whitelist.json");
            e.printStackTrace();
        }
    }

    /**
     * The main config
     */
    public static class Config {

        @Getter @Setter
        private boolean enabled;
        @Getter
        private String message;
        
        // Auth configurations
        private Auth auth;
        
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