package me.maybeizen.EasyTPA.command;

import com.mojang.brigadier.CommandDispatcher;
import me.maybeizen.EasyTPA.EasyTPA;
import me.maybeizen.EasyTPA.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCancelCommand extends SimpleCommandHandler {
    public TPCancelCommand(EasyTPA plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, configManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        requestManager.cancelTeleport(player.getUniqueId());

        boolean cancelled = requestManager.cancelRequest(player.getUniqueId());

        if (cancelled) {
            MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("requests.cancelled"));
        } else {
            MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("admin.nothing-to-cancel"));
        }

        return true;
    }

    public void register(CommandDispatcher<CommandSender> dispatcher) {
    }
}
