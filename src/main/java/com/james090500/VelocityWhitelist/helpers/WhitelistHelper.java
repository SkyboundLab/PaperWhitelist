package com.james090500.VelocityWhitelist.helpers;

import com.james090500.VelocityWhitelist.VelocityWhitelist;
import com.james090500.VelocityWhitelist.config.Configs;
import com.james090500.VelocityWhitelist.models.AuthType;
import com.james090500.VelocityWhitelist.models.WhitelistEntry;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class WhitelistHelper {

    private VelocityWhitelist velocityWhitelist;
    private CommandSource source;

    public WhitelistHelper(VelocityWhitelist velocityWhitelist, CommandSource source) {
        this.velocityWhitelist = velocityWhitelist;
        this.source = source;
    }

    /**
     * Check if the player is in the whitelist OR has bypass permissions
     * @param player
     * @return If player is whitelisted or has bypass
     */
    public static boolean check(Player player) {
        if(player.hasPermission("vwhitelist.bypass")) {
            return true;
        } else {
            // Check if any WhitelistEntry has matching UUID
            return Configs.getWhitelist().stream()
                    .anyMatch(entry -> entry.getUuid().equals(player.getUniqueId()));
        }
    }

    /**
     * Add a player to the whitelist with specified auth type
     * @param username
     * @param authType
     */
    public void add(String username, AuthType authType) {
        velocityWhitelist.getServer().getScheduler().buildTask(velocityWhitelist, () -> {
            UUID uuid = new MinecraftApi(velocityWhitelist).getUUID(username, authType);
            if(uuid == null) {
                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + username + " is not a valid username for auth type: " + authType.getName()));
            } else {
                WhitelistEntry entry = new WhitelistEntry(uuid, authType.getName());
                if(Configs.getWhitelist().contains(entry)) {
                    source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + username + " is already in the whitelist"));
                } else {
                    Configs.getWhitelist().add(entry);
                    Configs.saveWhitelist(velocityWhitelist);
                    source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + username + " (" + authType.getName() + ") has been added to the whitelist"));
                }
            }
        }).schedule();
    }

    /**
     * Add a player to the whitelist (defaults to Microsoft auth)
     * @param username
     */
    public void add(String username) {
        add(username, AuthType.MICROSOFT);
    }

    /**
     * Remove a player from the whitelist with specified auth type
     * @param username
     * @param authType
     */
    public void remove(String username, AuthType authType) {
        velocityWhitelist.getServer().getScheduler().buildTask(velocityWhitelist, () -> {
            UUID uuid = new MinecraftApi(velocityWhitelist).getUUID(username, authType);
            if(uuid == null) {
                source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + username + " is not a valid username for auth type: " + authType.getName()));
            } else {
                WhitelistEntry entry = new WhitelistEntry(uuid, authType.getName());
                if(!Configs.getWhitelist().contains(entry)) {
                    source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + username + " is not in the whitelist"));
                } else {
                    Configs.getWhitelist().remove(entry);
                    Configs.saveWhitelist(velocityWhitelist);
                    source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + username + " (" + authType.getName() + ") has been removed from the whitelist"));
                }
            }
        }).schedule();
    }

    /**
     * Remove a player from the whitelist (defaults to Microsoft auth)
     * @param username
     */
    public void remove(String username) {
        remove(username, AuthType.MICROSOFT);
    }

}
