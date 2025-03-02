package com.maybeizen.EasyTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;


public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final TextComponent message = Component.text("Hello, world!").color(NamedTextColor.YELLOW);
        sender.sendMessage(message);
        return true;
    }
}