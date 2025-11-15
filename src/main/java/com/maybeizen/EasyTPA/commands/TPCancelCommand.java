package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

public class TPCancelCommand implements CommandExecutor {
    private final EasyTPA plugin;

    public TPCancelCommand(EasyTPA plugin) {
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
        
        if (plugin.getTPAManager().cancelTeleport(player.getUniqueId())) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("teleport-cancelled"));
            return true;
        }
        
        if (plugin.getTPAManager().cancelRequestBySender(player.getUniqueId())) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("request-cancelled"));
            return true;
        }
        
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("nothing-to-cancel"));
        return true;
    }
}

