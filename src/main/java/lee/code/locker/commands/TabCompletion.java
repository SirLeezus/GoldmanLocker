package lee.code.locker.commands;

import lee.code.locker.GoldmanLocker;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.*;

public class TabCompletion implements TabCompleter {

    private final List<String> blank = new ArrayList<>();
    private final List<String> subCommands = Arrays.asList("add", "remove", "admin");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
                List<String> hasCommand = new ArrayList<>();
                for (String pluginCommand : subCommands) if (sender.hasPermission("lock.command." + pluginCommand)) hasCommand.add(pluginCommand);
                return StringUtil.copyPartialMatches(args[0], hasCommand, new ArrayList<>());
            } else if (args[0].equals("add")) {
                if (args.length == 2) return StringUtil.copyPartialMatches(args[1], plugin.getPU().getOnlinePlayers(), new ArrayList<>());
            } else if (args[0].equals("remove")) {
                Block block = player.getTargetBlock(null, 5);
                if (block.getState().getBlockData() instanceof WallSign) {
                    TileState state = (TileState) block.getState();
                    PersistentDataContainer container = state.getPersistentDataContainer();
                    NamespacedKey key = new NamespacedKey(plugin, "lock-owner");
                    if (container.has(key, PersistentDataType.STRING)) {
                        List<UUID> trusted = plugin.getPU().getLockTrusted(state);
                        List<String> names = new ArrayList<>();
                        for (UUID tPlayer : trusted) names.add(Bukkit.getOfflinePlayer(tPlayer).getName());
                        return StringUtil.copyPartialMatches(args[1], names, new ArrayList<>());
                    }
                }
            } else if (args[0].equals("admin")) {
                if (args.length == 2) return StringUtil.copyPartialMatches(args[1], Collections.singletonList("bypass"), new ArrayList<>());
            }
        }
        return blank;
    }
}