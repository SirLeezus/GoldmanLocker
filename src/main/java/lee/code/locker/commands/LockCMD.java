package lee.code.locker.commands;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.lists.Lang;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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

            if (args.length > 0) {
                String arg = args[0].toLowerCase();
                switch (arg) {
                    case "add":
                        if (args.length > 1) {
                            String target = args[1];
                            UUID targetUUID = Bukkit.getPlayerUniqueId(target);
                            Block block = player.getTargetBlock(null, 5);
                            if (block.getState().getBlockData() instanceof WallSign) {
                                if (targetUUID != null && plugin.getPU().getOnlinePlayers(player).contains(args[1])) {

                                    TileState state = (TileState) block.getState();

                                    UUID ownerUUID = plugin.getPU().getLockOwner(state);
                                    String targetName = Bukkit.getOfflinePlayer(targetUUID).getName();
                                    List<UUID> trusted = plugin.getPU().getLockTrusted(state);
                                    PersistentDataContainer container = state.getPersistentDataContainer();
                                    NamespacedKey trustedKey = new NamespacedKey(plugin, "lock-trusted");

                                    if (ownerUUID != null) {
                                        String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                                        if (ownerUUID.equals(uuid)) {
                                            if (!ownerUUID.equals(targetUUID)) {
                                                String sTrusted = targetUUID.toString();
                                                if (trusted != null) {
                                                    if (!trusted.contains(targetUUID)) {
                                                        sTrusted = StringUtils.join(trusted, ",") + "," + targetUUID.toString();
                                                    } else {
                                                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_ALREADY_ADDED.getString(new String[]{targetName}));
                                                        return true;
                                                    }
                                                }
                                                Directional directional = (Directional) block.getState().getBlockData();
                                                Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                                                container.set(trustedKey, PersistentDataType.STRING, sTrusted);
                                                state.update();

                                                player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_ADD_TRUST_SUCCESSFUL.getString(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())}));
                                                return true;
                                            } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_OWNER.getString(null));
                                        } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_NOT_OWNER.getString(new String[]{ownerName}));
                                    } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCK_SIGN_NOT_FOUND.getString(null));
                                } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_PLAYER_NOT_ONLINE.getString(new String[]{args[1]}));
                            } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCK_SIGN_NOT_FOUND.getString(null));
                        } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_ADD_TARGET_PLAYER.getString(null));
                        break;

                    case "remove":
                        if (args.length > 1) {
                            String target = args[1];
                            UUID targetUUID = Bukkit.getPlayerUniqueId(target);
                            Block block = player.getTargetBlock(null, 5);
                            if (block.getState().getBlockData() instanceof WallSign) {

                                if (targetUUID != null) {
                                    TileState state = (TileState) block.getState();

                                    UUID ownerUUID = plugin.getPU().getLockOwner(state);
                                    String targetName = Bukkit.getOfflinePlayer(targetUUID).getName();
                                    List<UUID> trusted = plugin.getPU().getLockTrusted(state);
                                    PersistentDataContainer container = state.getPersistentDataContainer();
                                    NamespacedKey trustedKey = new NamespacedKey(plugin, "lock-trusted");
                                    Directional directional = (Directional) block.getState().getBlockData();
                                    Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                                    if (ownerUUID != null) {
                                        String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                                        if (ownerUUID.equals(uuid)) {
                                            if (trusted != null) {
                                                if (trusted.contains(targetUUID)) {

                                                    String sTrusted;
                                                    List<UUID> newTrusted = new ArrayList<>();
                                                    for (UUID tPlayer : trusted)
                                                        if (!tPlayer.equals(targetUUID)) newTrusted.add(tPlayer);
                                                    sTrusted = StringUtils.join(newTrusted, ",");

                                                    container.set(trustedKey, PersistentDataType.STRING, sTrusted);
                                                    state.update();

                                                    player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_TRUST_SUCCESSFUL.getString(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())}));
                                                    return true;
                                                } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_REMOVE_NOT_TRUSTED.getString(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())}));
                                            } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_REMOVE_NOT_TRUSTED.getString(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())}));
                                        } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_TRUST_REMOVE_NOT_OWNER.getString(new String[]{ownerName}));
                                    } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCK_SIGN_NOT_FOUND.getString(null));
                                }
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

                for (String line : lines) player.sendMessage(plugin.getPU().format(line));

            }
        }
        return true;
    }
}
