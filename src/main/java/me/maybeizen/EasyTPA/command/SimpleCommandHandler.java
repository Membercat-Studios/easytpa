package me.maybeizen.EasyTPA.command;

import me.maybeizen.EasyTPA.EasyTPA;
import me.maybeizen.EasyTPA.manager.ConfigManager;
import me.maybeizen.EasyTPA.manager.TeleportRequestManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SimpleCommandHandler implements CommandExecutor, TabCompleter {
    protected final EasyTPA plugin;
    protected final TeleportRequestManager requestManager;
    protected final ConfigManager configManager;

    public SimpleCommandHandler(EasyTPA plugin) {
        this.plugin = plugin;
        this.requestManager = plugin.getTeleportManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false; 
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    protected List<String> getOnlinePlayerNames(CommandSender sender) {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!player.getUniqueId().equals(p.getUniqueId())) {
                    names.add(player.getName());
                }
            } else {
                names.add(player.getName());
            }
        }
        return names;
    }
}

