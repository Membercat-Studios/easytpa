package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

public class TPAcceptCommand implements CommandExecutor {
    private final EasyTPA plugin;

    public TPAcceptCommand(EasyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("easytpa.tpaccept")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length > 0) {
            Player requester = plugin.getServer().getPlayer(args[0]);
            if (requester == null) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }
            
            if (plugin.getTPAManager().acceptRequestFrom(player, requester)) {
            }
        } else {
            if (plugin.getTPAManager().acceptRequest(player)) {
            }
        }

        return true;
    }
} 