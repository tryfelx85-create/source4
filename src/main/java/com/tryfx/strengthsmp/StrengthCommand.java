package com.tryfx.strengthsmp;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class StrengthCommand implements CommandExecutor, TabCompleter {

    private final StrengthSMP plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public StrengthCommand(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        StrengthManager sm = plugin.getStrengthManager();

        Player target;
        boolean self;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Console must specify a player: /strength <player>");
                return true;
            }
            target = (Player) sender;
            self = true;
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            self = sender instanceof Player p && p.equals(target);
            if (target == null) {
                sender.sendMessage(MM.deserialize(prefix + "<red>Player not found or not online."));
                return true;
            }
        }

        int strength = sm.getStrength(target);
        String formatted = (strength > 0 ? "+" : "") + strength;

        String key = self ? "messages.check-self" : "messages.check-other";
        String msg = plugin.getConfig().getString(key, "")
                .replace("%player%", target.getName())
                .replace("%strength%", formatted);

        sender.sendMessage(MM.deserialize(prefix + msg));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
