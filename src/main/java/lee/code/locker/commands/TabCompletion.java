package lee.code.locker.commands;

import lee.code.locker.GoldmanLocker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabCompletion implements TabCompleter {

    private final List<String> blank = new ArrayList<>();
    private final List<String> subCommands = Arrays.asList("add", "remove", "admin");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (sender instanceof Player) {

            if (args.length == 1) {
                List<String> hasCommand = new ArrayList<>();
                for (String pluginCommand : subCommands) if (sender.hasPermission("lock.command." + pluginCommand)) hasCommand.add(pluginCommand);
                return StringUtil.copyPartialMatches(args[0], hasCommand, new ArrayList<>());
            } else if (args[0].equals("add")) {
                if (args.length == 2) return StringUtil.copyPartialMatches(args[1], plugin.getPU().getOnlinePlayers(), new ArrayList<>());
            } else if (args[0].equals("remove")) {
                if (args.length == 2) return StringUtil.copyPartialMatches(args[1], plugin.getPU().getOnlinePlayers(), new ArrayList<>());
            } else if (args[0].equals("admin")) {
                if (args.length == 2) return StringUtil.copyPartialMatches(args[1], Collections.singletonList("bypass"), new ArrayList<>());
            }
        }
        return blank;
    }
}