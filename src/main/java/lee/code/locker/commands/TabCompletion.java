package lee.code.locker.commands;

import lee.code.locker.GoldmanLocker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabCompletion implements TabCompleter {

    private final List<String> blank = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (sender instanceof Player) {

            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "remove"), new ArrayList<>());
            } else if (args[0].equals("add")) {
                if (args.length == 2)
                    return StringUtil.copyPartialMatches(args[1], plugin.getPU().getOnlinePlayers(), new ArrayList<>());
            } else if (args[0].equals("remove")) {
                if (args.length == 2)
                    return StringUtil.copyPartialMatches(args[1], plugin.getPU().getOnlinePlayers(), new ArrayList<>());
            }
        }
        return blank;
    }
}