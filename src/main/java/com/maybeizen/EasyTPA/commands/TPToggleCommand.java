package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

public class TPToggleCommand implements CommandExecutor {
    private final EasyTPA plugin;

    public TPToggleCommand(EasyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("easytpa.toggle")) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        boolean newState = plugin.getToggleManager().toggleTP(player);
        String message = newState ? 
            plugin.getConfigManager().getMessage("toggle-enabled") :
            plugin.getConfigManager().getMessage("toggle-disabled");
        
        player.sendMessage(MessageUtils.formatMessage(message));
        return true;
    }
} 