package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

public class TPDenyCommand implements CommandExecutor {
    private final EasyTPA plugin;

    public TPDenyCommand(EasyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("easytpa.tpdeny")) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }
        
        if (args.length > 0) {
            Player requester = plugin.getServer().getPlayer(args[0]);
            if (requester == null) {
                player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-not-found")));
                return true;
            }
            
            if (plugin.getTPAManager().denyRequestFrom(player, requester)) {
                player.sendMessage(MessageUtils.formatMessage(
                    plugin.getConfigManager().getMessage("request-denied-target"), 
                    "player", requester.getName()
                ));
            }
        } else {
            String requesterName = plugin.getTPAManager().denyRequest(player);
            if (requesterName != null) {
                player.sendMessage(MessageUtils.formatMessage(
                    plugin.getConfigManager().getMessage("request-denied-target"), 
                    "player", requesterName
                ));
            } else {
                player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("no-pending-request")));
            }
        }

        return true;
    }
} 