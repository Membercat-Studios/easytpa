package me.maybeizen.EasyTPA.command;

import com.mojang.brigadier.CommandDispatcher;
import me.maybeizen.EasyTPA.EasyTPA;
import me.maybeizen.EasyTPA.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TPACommand extends SimpleCommandHandler {
    public TPACommand(EasyTPA plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, configManager.getMessage("general.player-only"));
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessageWithPlaceholders((Player) sender, configManager.getPrefix() + configManager.getMessage("commands.tpa.usage"));
            return true;
        }

        Player player = (Player) sender;
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("general.player-not-found"));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("commands.tpa.cannot-teleport-self"));
            return true;
        }

        requestManager.sendRequest(player, target).thenAccept(result -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                switch (result) {
                    case SUCCESS:
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", target.getName());
                        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("requests.sent", placeholders));

                        placeholders.put("player", player.getName());
                        String receivedMsg = configManager.getPrefix() + configManager.getMessage("requests.received", placeholders);
                        MessageUtil.sendMessageWithPlaceholders(target, receivedMsg);

                        sendInteractiveRequest(target, player);
                        break;
                        
                    case ALREADY_HAS_REQUEST:
                        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("requests.already-has-request"));
                        break;
                        
                    case ON_COOLDOWN:
                        long lastRequest = requestManager.getCooldown(player.getUniqueId());
                        long cooldownMs = configManager.getCooldown() * 1000L;
                        long remaining = (cooldownMs - (System.currentTimeMillis() - lastRequest)) / 1000;
                        
                        Map<String, String> cooldownPlaceholders = new HashMap<>();
                        cooldownPlaceholders.put("time", String.valueOf(remaining));
                        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("cooldown.message", cooldownPlaceholders));
                        break;
                        
                    case REQUESTS_DISABLED:
                        Map<String, String> disabledPlaceholders = new HashMap<>();
                        disabledPlaceholders.put("player", target.getName());
                        MessageUtil.sendMessageWithPlaceholders(player, configManager.getPrefix() + configManager.getMessage("toggle.target-has-tp-disabled", disabledPlaceholders));
                        break;
                }
            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getOnlinePlayerNames(sender);
        }
        return null;
    }

    public void register(CommandDispatcher<CommandSender> dispatcher) {
    }

    private void sendInteractiveRequest(Player target, Player requester) {
        Component acceptButton = net.kyori.adventure.text.Component.text()
            .content("[Accept]")
            .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tpaccept " + requester.getName()))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                MessageUtil.toComponent(configManager.getMessage("ui.hover.accept"))
            ))
            .build();

        Component denyButton = net.kyori.adventure.text.Component.text()
            .content("[Deny]")
            .color(net.kyori.adventure.text.format.NamedTextColor.RED)
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tpdeny " + requester.getName()))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                MessageUtil.toComponent(configManager.getMessage("ui.hover.deny"))
            ))
            .build();

        Component message = net.kyori.adventure.text.Component.text()
            .append(acceptButton)
            .append(net.kyori.adventure.text.Component.text(" "))
            .append(denyButton)
            .build();

        target.sendMessage(message);
    }
}

