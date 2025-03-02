package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

public class TPACommand implements CommandExecutor {
    private final EasyTPA plugin;

    public TPACommand(EasyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("tpa-usage")));
            return true;
        }

        Player player = (Player) sender;
        Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-not-found")));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("cannot-teleport-self")));
            return true;
        }

        if (plugin.getCooldownManager().hasCooldown(player.getUniqueId())) {
            String timeLeft = plugin.getCooldownManager().getRemainingTimeString(player.getUniqueId());
            player.sendMessage(MessageUtils.formatMessage("&cPlease wait " + timeLeft + " before sending another request."));
            return true;
        }

        if (plugin.getTPAManager().sendRequest(player, target)) {
            plugin.getCooldownManager().setCooldown(player.getUniqueId());
            
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("request-sent", "player", target.getName())));
            MessageUtils.sendTeleportRequest(player, target);
        }

        return true;
    }
} 