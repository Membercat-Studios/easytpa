package me.maybeizen.EasyTPA.manager;

import me.maybeizen.EasyTPA.EasyTPA;
import me.maybeizen.EasyTPA.command.*;
import org.bukkit.command.PluginCommand;

public class CommandManager {
    private final EasyTPA plugin;
    private final TPACommand tpaCommand;
    private final TPAcceptCommand tpAcceptCommand;
    private final TPDenyCommand tpDenyCommand;
    private final TPCancelCommand tpCancelCommand;
    private final TPListCommand tpListCommand;
    private final TPToggleCommand tpToggleCommand;
    private final EasyTPACommand easyTPACommand;

    public CommandManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.tpaCommand = new TPACommand(plugin);
        this.tpAcceptCommand = new TPAcceptCommand(plugin);
        this.tpDenyCommand = new TPDenyCommand(plugin);
        this.tpCancelCommand = new TPCancelCommand(plugin);
        this.tpListCommand = new TPListCommand(plugin);
        this.tpToggleCommand = new TPToggleCommand(plugin);
        this.easyTPACommand = new EasyTPACommand(plugin);
    }

    public void registerCommands() {
        registerCommand("tpa", tpaCommand);
        registerCommand("tpaccept", tpAcceptCommand);
        registerCommand("tpdeny", tpDenyCommand);
        registerCommand("tpcancel", tpCancelCommand);
        registerCommand("tplist", tpListCommand);
        registerCommand("tptoggle", tpToggleCommand);
        registerCommand("easytpa", easyTPACommand);
        
        plugin.getLogger().info("Registered all commands");
    }

    private void registerCommand(String name, SimpleCommandHandler handler) {
        PluginCommand command = plugin.getCommand(name);
        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            plugin.getLogger().warning("Command " + name + " not found in plugin.yml!");
        }
    }
}

