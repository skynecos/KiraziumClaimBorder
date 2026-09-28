package com.kirazium.claimborder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;

final class ClaimSeeListener implements Listener {

    private final BorderManager borderManager;

    ClaimSeeListener(BorderManager borderManager) {
        this.borderManager = borderManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        final String raw = event.getMessage();
        if (raw == null || raw.length() < 2 || raw.charAt(0) != '/') {
            return;
        }

        final String commandLine = raw.substring(1).trim();
        if (commandLine.isEmpty()) {
            return;
        }

        final String[] parts = commandLine.split("\\s+");
        if (parts.length != 2) {
            return;
        }

        final String root = parts[0].toLowerCase(Locale.ROOT);
        if (!root.equals("claim") && !root.equals("simpleclaimsystem:claim")) {
            return;
        }

        if (!parts[1].equalsIgnoreCase("see")) {
            return;
        }

        final Player player = event.getPlayer();

        // If the player cannot use the original command, let SimpleClaimSystem
        // handle the denial/message instead of replacing its behavior.
        if (!player.hasPermission("scs.command.claim")
                || !player.hasPermission("scs.command.claim.see")) {
            return;
        }

        // Stop SimpleClaimSystem's particle preview and replace it with our wall.
        event.setCancelled(true);
        borderManager.show(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        borderManager.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        borderManager.clear(event.getPlayer().getUniqueId());
    }
}
