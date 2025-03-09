package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class EasyTPACommand implements CommandExecutor, TabCompleter {
    private final EasyTPA plugin;

    public EasyTPACommand(EasyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("easytpa.admin")) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(MessageUtils.formatMessage("&cUsage: /easytpa reload"));
            return true;
        }

        sender.sendMessage(MessageUtils.formatMessage("&eReloading EasyTPA configuration..."));
        
        try {
            plugin.reloadConfiguration();
            
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("config-reloaded")));
            
        } catch (Exception e) {
            sender.sendMessage(MessageUtils.formatMessage("&cError reloading configuration: " + e.getMessage()));
            e.printStackTrace();
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission("easytpa.admin")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
        }
        
        return completions;
    }
} 