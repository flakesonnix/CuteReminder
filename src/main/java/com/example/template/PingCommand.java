package com.example.template;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class PingCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        // /ping [player]
        if (args.length == 0) {
            if (sender instanceof Player p) {
                sender.sendMessage("§aPong! §7Your ping: §e" + p.getPing() + "ms");
            } else {
                sender.sendMessage("§cUsage: /ping <player> (console must specify player)");
            }
            return true;
        }

        if (args.length == 1) {
            var target = sender.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return true;
            }
            if (!sender.hasPermission("template.ping.others") && !sender.equals(target)) {
                sender.sendMessage("§cNo permission to check others' ping.");
                return true;
            }
            sender.sendMessage("§a" + target.getName() + "'s ping: §e" + target.getPing() + "ms");
            return true;
        }

        sender.sendMessage("§cUsage: /ping [player]");
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1 && sender.hasPermission("template.ping.others")) {
            String prefix = args[0].toLowerCase();
            return sender.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .sorted()
                    .toList();
        }
        return Collections.emptyList();
    }
}
