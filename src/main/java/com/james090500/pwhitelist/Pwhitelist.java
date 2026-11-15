package com.james090500.pwhitelist;

import com.james090500.pwhitelist.commands.WhitelistCommand;
import com.james090500.pwhitelist.config.Configs;
import com.james090500.pwhitelist.listeners.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Pwhitelist extends JavaPlugin {

    public static final String PREFIX = "[PaperWhitelist] ";
    private static Pwhitelist instance;

    public static Pwhitelist getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        Configs.loadConfigs();

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

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
