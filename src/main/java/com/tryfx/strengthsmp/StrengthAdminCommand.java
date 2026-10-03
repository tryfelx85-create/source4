package com.tryfx.strengthsmp;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class StrengthAdminCommand implements CommandExecutor, TabCompleter {

    private final StrengthSMP plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public StrengthAdminCommand(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");

        if (args.length == 0) {
            sender.sendMessage(MM.deserialize(prefix + "<yellow>Usage: /strengthadmin <set|reset|reload> [player] [value]"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(MM.deserialize(prefix + "<green>Config reloaded."));
                return true;
            }
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage(MM.deserialize(prefix + "<red>Usage: /strengthadmin set <player> <value>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(MM.deserialize(prefix + "<red>Player not found or not online."));
                    return true;
                }
                int value;
                try {
                    value = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MM.deserialize(prefix + "<red>Invalid number: " + args[2]));
                    return true;
                }
                plugin.getStrengthManager().setStrength(target, value);
                int actual = plugin.getStrengthManager().getStrength(target);
                sender.sendMessage(MM.deserialize(prefix + "<green>Set " + target.getName() + "'s Strength to " + actual + " (clamped to configured range)."));
                return true;
            }
            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage(MM.deserialize(prefix + "<red>Usage: /strengthadmin reset <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(MM.deserialize(prefix + "<red>Player not found or not online."));
                    return true;
                }
                plugin.getStrengthManager().setStrength(target, plugin.getStrengthManager().getDefault());
                sender.sendMessage(MM.deserialize(prefix + "<green>Reset " + target.getName() + "'s Strength to default."));
                return true;
            }
            default -> {
                sender.sendMessage(MM.deserialize(prefix + "<red>Unknown subcommand. Usage: /strengthadmin <set|reset|reload> [player] [value]"));
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("set", "reset", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))) {
            String partial = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
