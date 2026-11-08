package com.james090500.pwhitelist.listeners;

import com.james090500.pwhitelist.config.Configs;
import com.james090500.pwhitelist.helpers.WhitelistHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Paper equivalent of the Velocity LoginEvent listener.
 *
 * PlayerLoginEvent (not AsyncPlayerPreLoginEvent) is used on purpose:
 * the bypass permission can only be checked once the Player exists, and
 * disallowing here still stops the login before the player joins.
 */
public class JoinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (Configs.getConfig().isEnabled()) {
            if (!WhitelistHelper.check(player)) {
                Component kickMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(Configs.getConfig().getMessage());
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, kickMessage);
            }
        }
    }
}
