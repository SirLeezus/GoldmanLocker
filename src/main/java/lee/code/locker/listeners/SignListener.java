package lee.code.locker.listeners;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.lists.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SignListener implements Listener {

    @EventHandler
    public void onSignEditEvent(SignChangeEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = e.getBlock();
        BlockData data = block.getBlockData();
        String line1 = plugin.getPU().unFormatC(e.line(0));

        if (line1 != null && line1.equals("[lock]")) {
            if (data instanceof Directional directional) {
                Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());
                if (plugin.getPU().getSupportedBlocks().contains(blockBehind.getType().name())) {

                    if (getLockSign(blockBehind) == null) {
                        e.line(0, plugin.getPU().formatC("&6[&cLocked&6]"));
                        e.line(1, plugin.getPU().formatC("&e" + e.getPlayer().getName()));
                        e.line(2, Component.text(""));
                        e.line(3, Component.text(""));

                        TileState state = (TileState) block.getState();
                        PersistentDataContainer container = state.getPersistentDataContainer();
                        NamespacedKey owner = new NamespacedKey(plugin, "lock-owner");
                        NamespacedKey trusted = new NamespacedKey(plugin, "lock-trusted");
                        container.set(owner, PersistentDataType.STRING, uuid.toString());
                        container.set(trusted, PersistentDataType.STRING, "");
                        state.update();

                        player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.MESSAGE_LOCK_SUCCESSFUL.getComponent(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()) })));
                    } else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_BLOCK_ALREADY_HAS_LOCK.getComponent(null)));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Action action = e.getAction();
        Block block = e.getClickedBlock();
        boolean hasAdminBypass = plugin.getData().hasAdminBypass(uuid);

        if (block != null) {
            if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
                    TileState lockSign = getLockSign(block);
                    if (lockSign != null) {

                        if (plugin.getData().hasPlayerClickDelay(uuid)) {
                            e.setCancelled(true); return;
                        } else plugin.getPU().addPlayerClickDelay(uuid);

                        UUID owner =  plugin.getPU().getLockOwner(lockSign);
                        List<UUID> trusted = plugin.getPU().getLockTrusted(lockSign);
                        if (trusted == null || !trusted.contains(uuid)) {
                            if (owner != null && !owner.equals(uuid) && !hasAdminBypass) {
                                player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCKED.getComponent(new String[]{plugin.getPU().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(owner).getName()})));
                                e.setCancelled(true);
                            }
                        }
                    }
                } else if (block.getState().getBlockData() instanceof WallSign) {

                    TileState state = (TileState) block.getState();
                    PersistentDataContainer container = state.getPersistentDataContainer();
                    NamespacedKey key = new NamespacedKey(plugin, "lock-owner");

                    if (container.has(key, PersistentDataType.STRING)) {

                        if (plugin.getData().hasPlayerClickDelay(uuid)) {
                            e.setCancelled(true); return;
                        } else plugin.getPU().addPlayerClickDelay(uuid);

                        Sign sign = (Sign) block.getState();
                        TileState lockSign = (TileState) block.getState();

                        UUID owner =  plugin.getPU().getLockOwner(lockSign);
                        List<UUID> trusted = plugin.getPU().getLockTrusted(lockSign);
                        String trustedNames = "";

                        if (owner != null) {
                            if (trusted != null) trustedNames = plugin.getPU().getTrustedString(trusted);
                            if (owner.equals(uuid)) nameSignCheck(sign, uuid);
                            List<Component> lines = new ArrayList<>();

                            lines.add(Lang.SIGN_INFO_HEADER.getComponent(null));
                            lines.add(Component.text(""));
                            lines.add(Lang.SIGN_INFO_OWNER.getComponent(new String[]{Bukkit.getOfflinePlayer(owner).getName()}));
                            lines.add(Lang.SIGN_INFO_TRUSTED.getComponent(new String[]{trustedNames}));
                            lines.add(Component.text(""));
                            lines.add(Lang.SIGN_INFO_FOOTER.getComponent(null));

                            for (Component line : lines) player.sendMessage(line);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = e.getBlock();
        boolean hasAdminBypass = plugin.getData().hasAdminBypass(uuid);

        if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
            TileState lockSign = getLockSign(block);
            if (lockSign != null) {
                UUID owner =  plugin.getPU().getLockOwner(lockSign);
                if (owner != null && !owner.equals(uuid) && !hasAdminBypass) {
                    player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCKED.getComponent(new String[] { plugin.getPU().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(owner).getName() })));
                    e.setCancelled(true);
                } else {
                    if (blockHasSign(block)) {
                        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                        player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getComponent(new String[] { plugin.getPU().formatBlockName(block.getType().name()) })));
                    }
                }
            }
        } else if (block.getState().getBlockData() instanceof WallSign) {

            BlockData data = block.getBlockData();
            Directional directional = (Directional) data;
            Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

            TileState state = (TileState) block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "lock-owner");

            if (container.has(key, PersistentDataType.STRING)) {
                UUID owner = plugin.getPU().getLockOwner(state);
                if (owner != null && !owner.equals(uuid) && !hasAdminBypass) {
                    player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_LOCKED.getComponent(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()), Bukkit.getOfflinePlayer(owner).getName() })));
                    e.setCancelled(true);
                } else {
                    block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                    player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getComponent(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()) })));
                }
            }
        }
    }

    @EventHandler
    public void onPistonMove(BlockPistonExtendEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        for (Block block : new ArrayList<>(e.getBlocks())) {
            if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
                TileState lockSign = getLockSign(block);
                if (lockSign != null) e.setCancelled(true);
            } else if (block.getState().getBlockData() instanceof WallSign) {
                TileState state = (TileState) block.getState();
                PersistentDataContainer container = state.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(plugin, "lock-owner");
                if (container.has(key, PersistentDataType.STRING)) e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplodeEvent(EntityExplodeEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        for (Block block : new ArrayList<>(e.blockList())) {
            if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
                TileState lockSign = getLockSign(block);
                if (lockSign != null) e.blockList().remove(block);
            } else if (block.getState().getBlockData() instanceof WallSign) {
                TileState state = (TileState) block.getState();
                PersistentDataContainer container = state.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(plugin, "lock-owner");
                if (container.has(key, PersistentDataType.STRING)) e.blockList().remove(block);
            }
        }
    }

    @EventHandler
    public void onHopperMoveEvent(InventoryMoveItemEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        if (e.getSource().getLocation() != null) {
            Block block = e.getSource().getLocation().getBlock();
            if (plugin.getPU().getSupportedBlocks().contains(block.getType().name()) && e.getSource().getType() != InventoryType.HOPPER) {
                TileState lockSign = getLockSign(block);
                if (lockSign != null) e.setCancelled(true);
            }
        }
    }

    private boolean blockHasSign(Block block) {
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : faces) if (block.getRelative(face).getState().getBlockData() instanceof WallSign) return true;
        return false;
    }

    private TileState getLockSign(Block block) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        BlockState blockState = block.getState();
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : faces) {

            Block relativeBlock = block.getRelative(face);
            BlockState relativeBlockState = relativeBlock.getState();

            if (blockState instanceof Chest chest && relativeBlockState instanceof Chest relativeChest) {

                InventoryHolder inventoryHolder = chest.getInventory().getHolder();
                InventoryHolder relativeInventoryHolder = relativeChest.getInventory().getHolder();

                if (inventoryHolder instanceof DoubleChest && relativeInventoryHolder instanceof DoubleChest) {

                    DoubleChestInventory inventory = (DoubleChestInventory) inventoryHolder.getInventory();
                    DoubleChestInventory relativeInventory = (DoubleChestInventory) relativeInventoryHolder.getInventory();

                    Location location = inventory.getLocation();
                    Location relativeLocation = relativeInventory.getLocation();

                    if (location != null && location.equals(relativeLocation)) {

                        for (BlockFace relativeFace : faces) {
                            Block relative = relativeBlock.getRelative(relativeFace);
                            BlockState relativeState = relative.getState();

                            if (relativeState.getBlockData() instanceof WallSign) {

                                Sign sign = (Sign) relativeState;
                                Directional signDirectional = (Directional) sign.getBlockData();
                                Block relativeBlockBehind = relative.getRelative(signDirectional.getFacing().getOppositeFace());

                                if (relativeBlockBehind.equals(relativeBlock)) {
                                    TileState state = (TileState) relativeState;
                                    PersistentDataContainer container = state.getPersistentDataContainer();
                                    NamespacedKey owner = new NamespacedKey(plugin, "lock-owner");
                                    if (container.has(owner, PersistentDataType.STRING)) return state;
                                }
                            }
                        }
                    }
                }
            } else if (relativeBlockState.getBlockData() instanceof WallSign) {
                Sign sign = (Sign) relativeBlockState;

                Directional signDirectional = (Directional) sign.getBlockData();
                Block relativeBlockBehind = relativeBlock.getRelative(signDirectional.getFacing().getOppositeFace());

                if (relativeBlockBehind.equals(block)) {
                    TileState state = (TileState) relativeBlockState;
                    PersistentDataContainer container = state.getPersistentDataContainer();
                    NamespacedKey owner = new NamespacedKey(plugin, "lock-owner");
                    if (container.has(owner, PersistentDataType.STRING)) return state;
                }
            }
        }
        return null;
    }

    private void nameSignCheck(Sign sign, UUID owner) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (!plugin.getPU().unFormatC(sign.line(1)).equals(Bukkit.getOfflinePlayer(owner).getName())) {
            sign.line(1, plugin.getPU().formatC("&e" + Bukkit.getOfflinePlayer(owner).getName()));
            sign.update();
        }
    }
}
