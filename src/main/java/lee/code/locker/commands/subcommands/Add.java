package lee.code.locker.commands.subcommands;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.commands.SubCommand;
import lee.code.locker.lists.Lang;
import lee.code.locker.lists.Values;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class Add extends SubCommand {

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Adds a trusted player to a lock sign you're looking at.";
    }

    @Override
    public String getSyntax() {
        return "/lock add &f<player>";
    }

    @Override
    public String getPermission() {
        return "lock.command.add";
    }

    @Override
    public void perform(Player player, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        UUID uuid = player.getUniqueId();

        if (args.length > 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
            if (target != null) {
                UUID targetUUID = target.getUniqueId();
                Block block = player.getTargetBlock(null, 5);
                if (block.getState().getBlockData() instanceof WallSign) {
                    TileState state = (TileState) block.getState();

                    PersistentDataContainer container = state.getPersistentDataContainer();
                    NamespacedKey key = new NamespacedKey(plugin, "lock-owner");
                    NamespacedKey trustedKey = new NamespacedKey(plugin, "lock-trusted");

                    if (container.has(key, PersistentDataType.STRING)) {

                        UUID ownerUUID = plugin.getPU().getLockOwner(state);
                        String targetName = args[1];
                        List<UUID> trusted = plugin.getPU().getLockTrusted(state);

                        if (ownerUUID != null) {
                            String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                            if (ownerUUID.equals(uuid)) {
                                if (!ownerUUID.equals(targetUUID)) {
                                    String sTrusted = targetUUID.toString();
                                    if (trusted != null) {
                                        if (!trusted.contains(targetUUID)) {
                                            if (trusted.size() >= Values.MAX_TRUSTED.getValue()) {
                                                player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUSTED_MAX.getComponent(new String[] { String.valueOf(Values.MAX_TRUSTED.getValue()) })));
                                                return;
                                            } else sTrusted = StringUtils.join(trusted, ",") + "," + targetUUID;
                                        } else {
                                            player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_ADD_ALREADY_ADDED.getComponent(new String[]{targetName})));
                                            return;
                                        }
                                    }
                                    Directional directional = (Directional) block.getState().getBlockData();
                                    Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                                    container.set(trustedKey, PersistentDataType.STRING, sTrusted);
                                    state.update();

                                    player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.MESSAGE_ADD_TRUST_SUCCESSFUL.getComponent(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())})));
                                } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_ADD_OWNER.getComponent(null)));
                            } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_ADD_NOT_OWNER.getComponent(new String[]{ownerName})));
                        } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCK_SIGN_NOT_FOUND.getComponent(null)));
                    } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCK_SIGN_NOT_FOUND.getComponent(null)));
                } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCK_SIGN_NOT_FOUND.getComponent(null)));
            } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_PLAYER_NOT_FOUND.getComponent(new String[]{args[1]})));
        } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_ADD_TARGET_PLAYER.getComponent(null)));
    }
    @Override
    public void performConsole(CommandSender console, String[] args) {
        console.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_NOT_A_CONSOLE_COMMAND.getComponent(null)));
    }
}
