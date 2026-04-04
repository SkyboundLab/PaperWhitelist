package com.james090500.VelocityWhitelist.commands;

import com.james090500.VelocityWhitelist.VelocityWhitelist;
import com.james090500.VelocityWhitelist.models.AuthType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.concurrent.CompletableFuture;

public class CommandBuilder {

    private static ProxyServer server;
    private static final CommandHandler handler = new CommandHandler();

    /**
     * Reginster all commands
     */
    public static void register() {
        server = VelocityWhitelist.getInstance().getServer();
        //Setup command flow
        server.getCommandManager().register(server.getCommandManager().metaBuilder("vwhitelist").build(), new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("vwhitelist").requires(sender -> sender.hasPermission("vwhitelist.admin")).executes(handler::about)
                        .then(LiteralArgumentBuilder.<CommandSource>literal("on").executes(handler::turnOn))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("off").executes(handler::turnOff))

                        // /vwhitelist add <username> - defaults to microsoft
                        .then(LiteralArgumentBuilder.<CommandSource>literal("add").executes(handler::add))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.word())
                                .suggests(CommandBuilder::allPlayers)
                                .executes(handler::add)
                                // /vwhitelist add <username> <type>
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("type", StringArgumentType.word())
                                .suggests(CommandBuilder::authTypes)
                                .executes(handler::addWithType))))

                        // /vwhitelist remove <username> - defaults to microsoft
                        .then(LiteralArgumentBuilder.<CommandSource>literal("remove").executes(handler::remove))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("remove")
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.word())
                                .suggests(CommandBuilder::allPlayers)
                                .executes(handler::remove)
                                // /vwhitelist remove <username> <type>
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("type", StringArgumentType.word())
                                .suggests(CommandBuilder::authTypes)
                                .executes(handler::removeWithType))))

                        .then(LiteralArgumentBuilder.<CommandSource>literal("reload").requires(source -> source.hasPermission("vwhitelist.admin")).executes(handler::reload))
        ));
    }

    /**
     * An all players tab suggestions
     * @param context
     * @param builder
     * @return
     */
    private static CompletableFuture<Suggestions> allPlayers(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        for (Player player : server.getAllPlayers()) {
            String username = player.getUsername();
            if (username.toLowerCase().startsWith(context.getInput().toLowerCase()) || username.equalsIgnoreCase(context.getInput())) {
                builder.suggest(username);
            }
        }
        return builder.buildFuture();
    }

    /**
     * Auth type suggestions for tab completion
     * @param context
     * @param builder
     * @return
     */
    private static CompletableFuture<Suggestions> authTypes(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        for (AuthType authType : AuthType.values()) {
            String name = authType.getName();
            String input = builder.getRemaining().toLowerCase();
            if (name.toLowerCase().startsWith(input) || input.isEmpty()) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}
