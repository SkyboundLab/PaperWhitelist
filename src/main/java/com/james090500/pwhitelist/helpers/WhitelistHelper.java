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

    public static boolean check(Player player) {
        if (player.isOp() || player.hasPermission("pwhitelist.bypass")) {
            return true;
        } else {
            return Configs.getWhitelist().stream().anyMatch(entry -> entry.getUuid().equals(player.getUniqueId()));
        }
    }

    public void add(String username, AuthType authType) {
        Player online = Bukkit.getPlayerExact(username);
        if (online != null) {
            addEntry(online.getUniqueId(), username, authType);
            return;
        }

        paperWhitelist.getServer().getScheduler().runTaskAsynchronously(paperWhitelist, () -> {
            UUID uuid = new MinecraftApi(paperWhitelist).getUUID(username, authType);
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

    public void add(String username) {
        add(username, AuthType.MICROSOFT);
    }

    public void remove(String username, AuthType authType) {
        Player online = Bukkit.getPlayerExact(username);
        if (online != null) {
            removeEntry(online.getUniqueId(), username, authType);
            return;
        }

        paperWhitelist.getServer().getScheduler().runTaskAsynchronously(paperWhitelist, () -> {
            UUID uuid = new MinecraftApi(paperWhitelist).getUUID(username, authType);
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

    public void remove(String username) {
        remove(username, AuthType.MICROSOFT);
    }
}
