package com.james090500.pwhitelist;

import com.james090500.pwhitelist.commands.WhitelistCommand;
import com.james090500.pwhitelist.config.Configs;
import com.james090500.pwhitelist.listeners.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper port of VelocityWhitelist (base 915d0d8).
 *
 * Same behaviour and same files as the Velocity plugin — only the
 * platform changes: Velocity's LoginEvent becomes Bukkit's
 * PlayerLoginEvent, and the Brigadier command becomes a Bukkit command.
 * The plugin's name is PaperWhitelist, the command is /pwhitelist
 * (with /vwhitelist kept as an alias), the permissions are
 * pwhitelist.admin / pwhitelist.bypass, and the data folder is
 * plugins/paperwhitelist (lowercase, forced in Configs — Bukkit would
 * otherwise derive plugins/PaperWhitelist from the plugin name).
 *
 * Note on commands: this plugin uses plugin.yml + Bukkit's CommandExecutor
 * on purpose. Paper's Brigadier API (BasicCommand) does not exist in the
 * Paper 1.20.4 API, so a paper-plugin.yml + Brigadier port would not load
 * on Paper 1.20-1.20.5. The Bukkit route runs on every Paper 1.20+ build.
 */
public class Pwhitelist extends JavaPlugin {

    public static final String PREFIX = "[pwhitelist] ";
    private static Pwhitelist instance;

    public static Pwhitelist getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Load configs
        Configs.loadConfigs();

        // Register listeners
        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        // Register commands
        WhitelistCommand command = new WhitelistCommand(this);
        getCommand("pwhitelist").setExecutor(command);
        getCommand("pwhitelist").setTabCompleter(command);

        getLogger().info("PaperWhitelist enabled. Whitelist is " + (Configs.getConfig().isEnabled() ? "ON" : "OFF"));
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
