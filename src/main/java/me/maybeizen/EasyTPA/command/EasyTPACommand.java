package me.maybeizen.EasyTPA.command;

import com.mojang.brigadier.CommandDispatcher;
import me.maybeizen.EasyTPA.EasyTPA;
import me.maybeizen.EasyTPA.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EasyTPACommand extends SimpleCommandHandler {
    public EasyTPACommand(EasyTPA plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            MessageUtil.sendMessage(sender, configManager.getPrefix() + configManager.getMessage("admin.config-reloading"));
            
            configManager.reload();
            
            MessageUtil.sendMessage(sender, configManager.getPrefix() + configManager.getMessage("admin.config-reloaded"));
            return true;
        }

        return false;
    }

    public void register(CommandDispatcher<CommandSender> dispatcher) {
    }
}
