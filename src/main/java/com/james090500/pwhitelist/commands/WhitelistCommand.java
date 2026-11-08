package com.james090500.pwhitelist.commands;

import com.james090500.pwhitelist.Pwhitelist;
import com.james090500.pwhitelist.config.Configs;
import com.james090500.pwhitelist.helpers.WhitelistHelper;
import com.james090500.pwhitelist.models.AuthType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Bukkit port of the Velocity Brigadier command tree:
 * /pwhitelist, on, off, add &lt;username&gt; [type], remove &lt;username&gt; [type], reload.
 * Tab completion suggests online players for usernames and the three auth
 * types, matching the original Brigadier suggestions.
 */
public class WhitelistCommand implements CommandExecutor, TabCompleter {

    private final Pwhitelist paperWhitelist;

    public WhitelistCommand(Pwhitelist paperWhitelist) {
        this.paperWhitelist = paperWhitelist;
    }

    private void send(CommandSender source, String message) {
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @Override
    public boolean onCommand(CommandSender source, Command command, String label, String[] args) {
        if (!source.hasPermission("pwhitelist.admin")) {
            send(source, "&c" + Pwhitelist.PREFIX + "You do not have permission to use this command");
            return true;
        }

        if (args.length == 0) {
            about(source);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> turnOn(source);
            case "off" -> turnOff(source);
            case "add" -> {
                if (args.length < 2) {
                    syntaxAdd(source);
                } else if (args.length == 2) {
                    new WhitelistHelper(paperWhitelist, source).add(args[1]);
                } else {
                    AuthType authType = AuthType.fromString(args[2]);
                    if (authType == null) {
                        invalidType(source, args[2]);
                    } else {
                        new WhitelistHelper(paperWhitelist, source).add(args[1], authType);
                    }
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    syntaxRemove(source);
                } else if (args.length == 2) {
                    new WhitelistHelper(paperWhitelist, source).remove(args[1]);
                } else {
                    AuthType authType = AuthType.fromString(args[2]);
                    if (authType == null) {
                        invalidType(source, args[2]);
                    } else {
                        new WhitelistHelper(paperWhitelist, source).remove(args[1], authType);
                    }
                }
            }
            case "reload" -> {
                Configs.loadConfigs();
                send(source, "&a" + Pwhitelist.PREFIX + "Reloaded");
            }
            default -> about(source);
        }
        return true;
    }

    private void about(CommandSender source) {
        String status = Configs.getConfig().isEnabled() ? "&2&lON" : "&c&lOFF";
        send(source, "&a" + Pwhitelist.PREFIX + "Whitelist is " + status);
        send(source, "&a" + Pwhitelist.PREFIX + "pwhitelist by james090500");
    }

    private void turnOn(CommandSender source) {
        if (Configs.getConfig().isEnabled()) {
            send(source, "&c" + Pwhitelist.PREFIX + "Whitelist is already turned on");
        } else {
            Configs.getConfig().setEnabled(true);
            Configs.saveConfig(paperWhitelist);
            send(source, "&a" + Pwhitelist.PREFIX + "Whitelist turned &2&lON");
        }
    }

    private void turnOff(CommandSender source) {
        if (!Configs.getConfig().isEnabled()) {
            send(source, "&c" + Pwhitelist.PREFIX + "Whitelist is already turned off");
        } else {
            Configs.getConfig().setEnabled(false);
            Configs.saveConfig(paperWhitelist);
            send(source, "&a" + Pwhitelist.PREFIX + "Whitelist turned &c&lOFF");
        }
    }

    private void syntaxAdd(CommandSender source) {
        send(source, "&c" + Pwhitelist.PREFIX + "Syntax /pwhitelist add <username> [type]");
        send(source, "&c" + Pwhitelist.PREFIX + "Types: microsoft, elyby, drasl");
    }

    private void syntaxRemove(CommandSender source) {
        send(source, "&c" + Pwhitelist.PREFIX + "Syntax /pwhitelist remove <username> [type]");
        send(source, "&c" + Pwhitelist.PREFIX + "Types: microsoft, elyby, drasl");
    }

    private void invalidType(CommandSender source, String type) {
        send(source, "&c" + Pwhitelist.PREFIX + "Invalid auth type: " + type);
        send(source, "&c" + Pwhitelist.PREFIX + "Valid types: microsoft, elyby, drasl");
    }

    @Override
    public List<String> onTabComplete(CommandSender source, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (!source.hasPermission("pwhitelist.admin")) {
            return suggestions;
        }
        if (args.length == 1) {
            for (String sub : List.of("on", "off", "add", "remove", "reload")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    suggestions.add(player.getName());
                }
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            for (AuthType authType : AuthType.values()) {
                if (authType.getName().startsWith(args[2].toLowerCase())) {
                    suggestions.add(authType.getName());
                }
            }
        }
        return suggestions;
    }
}
