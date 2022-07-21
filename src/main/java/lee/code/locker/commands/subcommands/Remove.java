package lee.code.locker.commands.subcommands;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.commands.SubCommand;
import lee.code.locker.lists.Lang;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Remove extends SubCommand {

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes a trusted player from a lock sign you're looking at.";
    }

    @Override
    public String getSyntax() {
        return "/lock remove &f<player>";
    }

    @Override
    public String getPermission() {
        return "lock.command.remove";
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
                        Directional directional = (Directional) block.getState().getBlockData();
                        Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                        UUID ownerUUID = plugin.getPU().getLockOwner(state);
                        String targetName = args[1];
                        List<UUID> trusted = plugin.getPU().getLockTrusted(state);

                        if (ownerUUID != null) {
                            String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                            if (ownerUUID.equals(uuid)) {
                                if (trusted != null) {
                                    if (trusted.contains(targetUUID)) {

                                        String sTrusted;
                                        List<UUID> newTrusted = new ArrayList<>();
                                        for (UUID tPlayer : trusted) if (!tPlayer.equals(targetUUID)) newTrusted.add(tPlayer);
                                        sTrusted = StringUtils.join(newTrusted, ",");
                                        if (sTrusted == null) sTrusted = "";

                                        container.set(trustedKey, PersistentDataType.STRING, sTrusted);
                                        state.update();

                                        player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.MESSAGE_REMOVE_TRUST_SUCCESSFUL.getComponent(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())})));
                                    } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_REMOVE_NOT_TRUSTED.getComponent(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())})));
                                } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_REMOVE_NOT_TRUSTED.getComponent(new String[]{targetName, plugin.getPU().formatBlockName(blockBehind.getType().name())})));
                            } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_REMOVE_NOT_OWNER.getComponent(new String[]{ownerName})));
                        } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCK_SIGN_NOT_FOUND.getComponent(null)));
                    } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCK_SIGN_NOT_FOUND.getComponent(null)));
                } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCK_SIGN_NOT_FOUND.getComponent(null)));
            }  else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_PLAYER_NOT_FOUND.getComponent(new String[]{ args[1] })));
        } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_TRUST_REMOVE_TARGET_PLAYER.getComponent(null)));
    }

    @Override
    public void performConsole(CommandSender console, String[] args) {
        console.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_NOT_A_CONSOLE_COMMAND.getComponent(null)));
    }
}
