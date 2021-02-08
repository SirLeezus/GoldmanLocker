package lee.code.locker.commands.tabs;

import lee.code.locker.GoldmanLocker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LockTab implements TabCompleter {

    private final List<String> blank = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "remove"), new ArrayList<>());
            } else if (args[0].equals("add")) {
                if (args.length == 2)
                    return StringUtil.copyPartialMatches(args[1], plugin.getPU().getOnlinePlayers(player), new ArrayList<>());
            } else if (args[0].equals("remove")) {
                if (args.length == 2)
                    return StringUtil.copyPartialMatches(args[1], plugin.getSqLite().getTrustedToLock(plugin.getPU().formatLockLocation(player.getTargetBlock(null, 5).getLocation())), new ArrayList<>());
            }
        }
        return blank;
    }
}