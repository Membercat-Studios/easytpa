package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.managers.TPARequest;
import com.maybeizen.EasyTPA.utils.MessageUtils;

import java.util.List;
import java.util.UUID;

public class TPListCommand implements CommandExecutor {
    private final EasyTPA plugin;

    public TPListCommand(EasyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("easytpa.tpa")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        UUID playerUUID = player.getUniqueId();
        List<TPARequest> requests = plugin.getTPAManager().getRequestsForPlayer(playerUUID);
        
        if (requests.isEmpty()) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-pending-requests"));
            return true;
        }
        
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("request-list-header"));
        
        for (TPARequest request : requests) {
            if (request.getTargetUUID().equals(playerUUID)) {
                Player senderPlayer = plugin.getServer().getPlayer(request.getSenderUUID());
                if (senderPlayer != null && senderPlayer.isOnline()) {
                    MessageUtils.sendMessage(player,
                        plugin.getConfigManager().getMessage("request-list-received", "player", senderPlayer.getName()));
                }
            } else if (request.isFrom(playerUUID)) {
                Player targetPlayer = plugin.getServer().getPlayer(request.getTargetUUID());
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    MessageUtils.sendMessage(player,
                        plugin.getConfigManager().getMessage("request-list-sent", "player", targetPlayer.getName()));
                }
            }
        }
        
        return true;
    }
}

