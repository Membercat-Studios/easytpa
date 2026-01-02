package me.maybeizen.EasyTPA.command;

import com.mojang.brigadier.CommandDispatcher;
import me.maybeizen.EasyTPA.EasyTPA;
import me.maybeizen.EasyTPA.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TPListCommand extends SimpleCommandHandler {
    public TPListCommand(EasyTPA plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, configManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;
        List<UUID> received = requestManager.getPendingRequestsFor(player.getUniqueId());
        List<UUID> sent = requestManager.getSentRequestsBy(player.getUniqueId());

        if (received.isEmpty() && sent.isEmpty()) {
            MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("requests.no-pending-requests"));
            return true;
        }

        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("list.header"));

        Map<String, String> placeholders = new HashMap<>();
        for (UUID requesterId : received) {
            Player requester = Bukkit.getPlayer(requesterId);
            if (requester != null) {
                placeholders.put("player", requester.getName());
                MessageUtil.sendMessageWithPlaceholders(player, configManager.getMessage("list.received", placeholders));
            }
        }

        for (UUID targetId : sent) {
            Player target = Bukkit.getPlayer(targetId);
            if (target != null) {
                placeholders.put("player", target.getName());
                MessageUtil.sendMessageWithPlaceholders(player, configManager.getMessage("list.sent", placeholders));
            }
        }

        return true;
    }

    public void register(CommandDispatcher<CommandSender> dispatcher) {
    }
}
