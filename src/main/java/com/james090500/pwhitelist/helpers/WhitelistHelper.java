package com.james090500.pwhitelist.helpers;

import com.james090500.pwhitelist.Pwhitelist;
import com.james090500.pwhitelist.config.Configs;
import com.james090500.pwhitelist.models.AuthType;
import com.james090500.pwhitelist.models.WhitelistEntry;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WhitelistHelper {

    private final Pwhitelist paperWhitelist;
    private final CommandSender source;

    public WhitelistHelper(Pwhitelist paperWhitelist, CommandSender source) {
        this.paperWhitelist = paperWhitelist;
        this.source = source;
    }

    private void send(String message) {
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    /**
     * Check if the player is in the whitelist OR has bypass permissions
     * @param player
     * @return If player is whitelisted or has bypass
     */
    public static boolean check(Player player) {
        // isOp() is checked explicitly: at PlayerLoginEvent time an op's
        // hasPermission() still returns false, because the Bukkit
        // permission attachment only learns about the op status after the
        // login event. isOp() reads the operator list directly, so it is
        // already correct here. (Verified live: op=true, hasPermission
        // "pwhitelist.bypass"=false at login.)
        if (player.isOp() || player.hasPermission("pwhitelist.bypass")) {
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
        // Online-player shortcut on the main thread (Bukkit API is not
        // thread-safe, so this must not run inside the async lookup below).
        Player online = Bukkit.getPlayerExact(username);
        if (online != null) {
            addEntry(online.getUniqueId(), username, authType);
            return;
        }

        paperWhitelist.getServer().getScheduler().runTaskAsynchronously(paperWhitelist, () -> {
            UUID uuid = new MinecraftApi(paperWhitelist).getUUID(username, authType);
            // Back on the main thread to touch the whitelist and reply.
            // The lookup can outlive the plugin (slow API + server stop),
            // and scheduling on a disabled plugin throws, so bail out.
            if (!paperWhitelist.isEnabled()) {
                return;
            }
            paperWhitelist.getServer().getScheduler().runTask(paperWhitelist, () -> {
                if (uuid == null) {
                    send("&c" + Pwhitelist.PREFIX + username + " is not a valid username for auth type: " + authType.getName());
                } else {
                    addEntry(uuid, username, authType);
                }
            });
        });
    }

    private void addEntry(UUID uuid, String username, AuthType authType) {
        WhitelistEntry entry = new WhitelistEntry(uuid, authType.getName());
        if (Configs.getWhitelist().contains(entry)) {
            send("&a" + Pwhitelist.PREFIX + username + " is already in the whitelist");
        } else {
            Configs.getWhitelist().add(entry);
            Configs.saveWhitelist(paperWhitelist);
            send("&a" + Pwhitelist.PREFIX + username + " (" + authType.getName() + ") has been added to the whitelist");
        }
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
        // Online-player shortcut on the main thread (see add()).
        Player online = Bukkit.getPlayerExact(username);
        if (online != null) {
            removeEntry(online.getUniqueId(), username, authType);
            return;
        }

        paperWhitelist.getServer().getScheduler().runTaskAsynchronously(paperWhitelist, () -> {
            UUID uuid = new MinecraftApi(paperWhitelist).getUUID(username, authType);
            // Back on the main thread to touch the whitelist and reply.
            // The lookup can outlive the plugin (slow API + server stop),
            // and scheduling on a disabled plugin throws, so bail out.
            if (!paperWhitelist.isEnabled()) {
                return;
            }
            paperWhitelist.getServer().getScheduler().runTask(paperWhitelist, () -> {
                if (uuid == null) {
                    send("&c" + Pwhitelist.PREFIX + username + " is not a valid username for auth type: " + authType.getName());
                } else {
                    removeEntry(uuid, username, authType);
                }
            });
        });
    }

    private void removeEntry(UUID uuid, String username, AuthType authType) {
        WhitelistEntry entry = new WhitelistEntry(uuid, authType.getName());
        if (!Configs.getWhitelist().contains(entry)) {
            send("&a" + Pwhitelist.PREFIX + username + " is not in the whitelist");
        } else {
            Configs.getWhitelist().remove(entry);
            Configs.saveWhitelist(paperWhitelist);
            send("&a" + Pwhitelist.PREFIX + username + " (" + authType.getName() + ") has been removed from the whitelist");
        }
    }

    /**
     * Remove a player from the whitelist (defaults to Microsoft auth)
     * @param username
     */
    public void remove(String username) {
        remove(username, AuthType.MICROSOFT);
    }
}
