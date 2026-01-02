package me.maybeizen.EasyTPA.command;

import com.mojang.brigadier.CommandDispatcher;
import me.maybeizen.EasyTPA.EasyTPA;
import me.maybeizen.EasyTPA.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPToggleCommand extends SimpleCommandHandler {
    public TPToggleCommand(EasyTPA plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, configManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;
        
        plugin.getDatabaseManager().areRequestsEnabled(player.getUniqueId()).thenAccept(current -> {
            boolean newValue = !current;
            plugin.getDatabaseManager().setRequestsEnabled(player.getUniqueId(), newValue).thenRun(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (newValue) {
                        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("toggle.enabled"));
                    } else {
                        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("toggle.disabled"));
                    }
                });
            });
        });

        return true;
    }

    public void register(CommandDispatcher<CommandSender> dispatcher) {
    }
}
