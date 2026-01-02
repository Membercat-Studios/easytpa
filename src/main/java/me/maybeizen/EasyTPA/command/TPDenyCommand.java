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

public class TPDenyCommand extends SimpleCommandHandler {
    public TPDenyCommand(EasyTPA plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, configManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length == 0) {
            List<UUID> pending = requestManager.getPendingRequestsFor(player.getUniqueId());
            if (pending.isEmpty()) {
                MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("requests.no-pending-requests"));
                return true;
            }
            denyRequest(player, pending.get(0));
        } else {
            String targetName = args[0];
            Player requester = Bukkit.getPlayer(targetName);
            if (requester == null) {
                MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("general.player-not-found"));
                return true;
            }
            denyRequest(player, requester.getUniqueId());
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            List<UUID> pending = requestManager.getPendingRequestsFor(player.getUniqueId());
            List<String> names = new java.util.ArrayList<>();
            for (UUID requesterId : pending) {
                Player requester = Bukkit.getPlayer(requesterId);
                if (requester != null) {
                    names.add(requester.getName());
                }
            }
            return names;
        }
        return null;
    }

    private void denyRequest(Player denier, UUID requesterId) {
        requestManager.denyRequest(denier, requesterId).thenAccept(result -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player requester = Bukkit.getPlayer(requesterId);
                if (result && requester != null && requester.isOnline()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", denier.getName());
                    MessageUtil.sendMessageWithPlaceholders(requester, configManager.getPrefix() + configManager.getMessage("requests.denied", placeholders));

                    placeholders.put("player", requester.getName());
                    MessageUtil.sendMessageWithPlaceholders(denier, configManager.getPrefix() + configManager.getMessage("requests.denied-target", placeholders));
                } else {
                    MessageUtil.sendMessageWithPlaceholders(denier, configManager.getPrefix() + configManager.getMessage("requests.no-pending-request"));
                }
            });
        });
    }

    public void register(CommandDispatcher<CommandSender> dispatcher) {
        // Brigadier registration - Paper enhances commands automatically
    }
}
