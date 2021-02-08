package lee.code.locker.commands.cmds;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.database.SQLite;
import lee.code.locker.lists.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            SQLite SQL = plugin.getSqLite();

            if (args.length > 0) {

                    String arg = args[0].toLowerCase();

                    switch (arg) {

                        //lock add player
                        case "add":

                            if (args.length > 1) {

                                Player target = player;
                                Block block = player.getTargetBlock(null, 5);

                                if (block.getState().getBlockData() instanceof WallSign) {

                                    Location location = block.getLocation();
                                    String lockSign = plugin.getPluginUtility().formatLockLocation(location);

                                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {
                                        target = Bukkit.getPlayer(args[1]);
                                    } else {
                                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_PLAYER_NOT_ONLINE.getString(new String[]{args[1]}));
                                        return true;
                                    }

                                    if (SQL.isLockOwner(lockSign, uuid)) {
                                        if (!target.equals(player)) {

                                            if (!SQL.getTrustedToLock(lockSign).contains(target.getName())) {
                                                SQL.addLockTrusted(lockSign, target.getUniqueId());

                                                Directional directional = (Directional) block.getState().getBlockData();
                                                Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                                                player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_ADD_TRUST_SUCCESSFUL.getString(new String[] { target.getName(), plugin.getPluginUtility().formatBlockName(blockBehind.getType().name()) }));
                                                return true;
                                            } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_ALREADY_ADDED.getString(new String[] { target.getName() }));
                                        } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_OWNER.getString(null));
                                    } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_NOT_OWNER.getString(new String[] { Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName() }));
                                } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCK_SIGN_NOT_FOUND.getString(null));
                            } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_TARGET_PLAYER.getString(null));
                            break;

                        case "remove":

                            if (args.length > 1) {

                                String target = args[1];
                                Block block = player.getTargetBlock(null, 5);

                                if (block.getState().getBlockData() instanceof WallSign) {

                                    Location location = block.getLocation();
                                    String lockSign = plugin.getPluginUtility().formatLockLocation(location);

                                    if (SQL.isLockOwner(lockSign, uuid)) {

                                        Directional directional = (Directional) block.getState().getBlockData();
                                        Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                                        if (SQL.getTrustedToLock(lockSign).contains(target)) {
                                            SQL.removeLockTrusted (lockSign, target);
                                            player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_TRUST_SUCCESSFUL.getString(new String[] { target, plugin.getPluginUtility().formatBlockName(blockBehind.getType().name()) }));
                                            return true;

                                        } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_REMOVE_NOT_TRUSTED.getString(new String[] { target, plugin.getPluginUtility().formatBlockName(blockBehind.getType().name()) }));
                                    } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_REMOVE_NOT_OWNER.getString(new String[] { Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName() }));
                                } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCK_SIGN_NOT_FOUND.getString(null));
                            } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_REMOVE_TARGET_PLAYER.getString(null));
                            break;
                    }
            } else {

                List<String> lines = new ArrayList<>();
                lines.add(Lang.MESSAGE_HELP_DIVIDER.getString(null));
                lines.add(Lang.MESSAGE_HELP_TITLE.getString(null));
                lines.add("&r");
                lines.add("&31&b. " + Lang.MESSAGE_HELP_ADD.getString(null));
                lines.add("&32&b. " + Lang.MESSAGE_HELP_REMOVE.getString(null));
                lines.add("&r");
                lines.add(Lang.MESSAGE_HELP_DIVIDER.getString(null));

                for (String line : lines) player.sendMessage(plugin.getPluginUtility().format(line));

            }
        }
        return true;
    }
}
