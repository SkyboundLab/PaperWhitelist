package com.james090500.VelocityWhitelist.commands;

import com.james090500.VelocityWhitelist.VelocityWhitelist;
import com.james090500.VelocityWhitelist.config.Configs;
import com.james090500.VelocityWhitelist.helpers.WhitelistHelper;
import com.james090500.VelocityWhitelist.models.AuthType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CommandHandler {

    private final VelocityWhitelist velocityWhitelist = VelocityWhitelist.getInstance();

    /**
     * A bit of basic about information
     * @param commandSourceCommandContext
     * @return
     */
    public int about(CommandContext<CommandSource> commandSourceCommandContext) {
        CommandSource source = commandSourceCommandContext.getSource();
        String status = Configs.getConfig().isEnabled() ? "&2&lON" : "&c&lOFF";
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + "Whitelist is " + status));
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + "VelocityWhitelist by james090500"));
        return 1;
    }

    /**
     * Turn on the whitelist
     * @param commandSourceCommandContext
     * @return
     */
    public int turnOn(CommandContext<CommandSource> commandSourceCommandContext) {
        CommandSource source = commandSourceCommandContext.getSource();
        if(Configs.getConfig().isEnabled()) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Whitelist is already turned on"));
        } else {
            Configs.getConfig().setEnabled(true);
            Configs.saveConfig(velocityWhitelist);
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + "Whitelist turned &2&lON"));
        }
        return 1;
    }

    /**
     * Turn off the whitelist
     * @param commandSourceCommandContext
     * @return
     */
    public int turnOff(CommandContext<CommandSource> commandSourceCommandContext) {
        CommandSource source = commandSourceCommandContext.getSource();
        if(!Configs.getConfig().isEnabled()) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Whitelist is already turned off"));
        } else {
            Configs.getConfig().setEnabled(false);
            Configs.saveConfig(velocityWhitelist);
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + "Whitelist turned &c&lOFF"));
        }
        return 1;
    }

    /**
     * The command for /vwhitelist add <username>
     * Handles adding a user to the whitelist (defaults to Microsoft auth)
     * @param commandSourceCommandContext
     * @return
     */
    public int add(CommandContext<CommandSource> commandSourceCommandContext) {
        CommandSource source = commandSourceCommandContext.getSource();
        ParsedArgument<CommandSource, ?> username = commandSourceCommandContext.getArguments().get("username");
        if(username == null) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Syntax /vwhitelist add <username> [type]"));
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Types: microsoft, elyby, drasl"));
            return 1;
        }

        new WhitelistHelper(velocityWhitelist, source).add((String) username.getResult());
        return 1;
    }

    /**
     * The command for /vwhitelist add <username> <type>
     * Handles adding a user to the whitelist with specified auth type
     * @param commandSourceCommandContext
     * @return
     */
    public int addWithType(CommandContext<CommandSource> commandSourceCommandContext) {
        CommandSource source = commandSourceCommandContext.getSource();
        ParsedArgument<CommandSource, ?> username = commandSourceCommandContext.getArguments().get("username");
        ParsedArgument<CommandSource, ?> type = commandSourceCommandContext.getArguments().get("type");
        
        if(username == null || type == null) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Syntax /vwhitelist add <username> <type>"));
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Types: microsoft, elyby, drasl"));
            return 1;
        }

        String typeStr = (String) type.getResult();
        AuthType authType = AuthType.fromString(typeStr);
        
        if(authType == null) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Invalid auth type: " + typeStr));
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Valid types: microsoft, elyby, drasl"));
            return 1;
        }

        new WhitelistHelper(velocityWhitelist, source).add((String) username.getResult(), authType);
        return 1;
    }

    /**
     * The command for /vwhitelist remove <username>
     * Handles removing a user from the whitelist (defaults to Microsoft auth)
     * @param commandSourceCommandContext
     * @return
     */
    public int remove(CommandContext<CommandSource> commandSourceCommandContext) {
        CommandSource source = commandSourceCommandContext.getSource();
        ParsedArgument<CommandSource, ?> username = commandSourceCommandContext.getArguments().get("username");
        if(username == null) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Syntax /vwhitelist remove <username> [type]"));
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Types: microsoft, elyby, drasl"));
            return 1;
        }

        new WhitelistHelper(velocityWhitelist, source).remove((String) username.getResult());
        return 1;
    }

    /**
     * The command for /vwhitelist remove <username> <type>
     * Handles removing a user from the whitelist with specified auth type
     * @param commandSourceCommandContext
     * @return
     */
    public int removeWithType(CommandContext<CommandSource> commandSourceCommandContext) {
        CommandSource source = commandSourceCommandContext.getSource();
        ParsedArgument<CommandSource, ?> username = commandSourceCommandContext.getArguments().get("username");
        ParsedArgument<CommandSource, ?> type = commandSourceCommandContext.getArguments().get("type");
        
        if(username == null || type == null) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Syntax /vwhitelist remove <username> <type>"));
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Types: microsoft, elyby, drasl"));
            return 1;
        }

        String typeStr = (String) type.getResult();
        AuthType authType = AuthType.fromString(typeStr);
        
        if(authType == null) {
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Invalid auth type: " + typeStr));
            source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c" + velocityWhitelist.PREFIX + "Valid types: microsoft, elyby, drasl"));
            return 1;
        }

        new WhitelistHelper(velocityWhitelist, source).remove((String) username.getResult(), authType);
        return 1;
    }

    /**
     * Reloads the configs
     * @param commandSourceCommandContext
     * @return
     */
    public int reload(CommandContext<CommandSource> commandSourceCommandContext) {
        Configs.loadConfigs();
        CommandSource source = commandSourceCommandContext.getSource();
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a" + velocityWhitelist.PREFIX + "Reloaded"));
        return 1;
    }
}