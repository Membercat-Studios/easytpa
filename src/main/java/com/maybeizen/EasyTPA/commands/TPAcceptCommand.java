package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;

public class TPAcceptCommand implements CommandExecutor {
    private final EasyTPA plugin;

    public TPAcceptCommand(EasyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (plugin.getTPAManager().acceptRequest(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("request-accepted"));
        }

        return true;
    }
} 