package lee.code.locker.listeners;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.lists.Lang;
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
        String line1 = e.getLine(0);

        if (line1 != null && line1.equals("[lock]")) {

            if (data instanceof Directional) {

                Directional directional = (Directional) data;
                Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                if (plugin.getPU().getSupportedBlocks().contains(blockBehind.getType().name())) {

                    if (getLockSign(blockBehind) == null) {
                        e.setLine(0, plugin.getPU().format("&6[&cLocked&6]"));
                        e.setLine(1, plugin.getPU().format("&e" + e.getPlayer().getName()));

                        TileState state = (TileState) block.getState();
                        PersistentDataContainer container = state.getPersistentDataContainer();
                        NamespacedKey owner = new NamespacedKey(plugin, "lock-owner");
                        NamespacedKey trusted = new NamespacedKey(plugin, "lock-trusted");
                        container.set(owner, PersistentDataType.STRING, uuid.toString());
                        container.set(trusted, PersistentDataType.STRING, "");
                        state.update();

                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()) }));
                    } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_BLOCK_ALREADY_HAS_LOCK.getString(null));
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

        if (block != null) {
            if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
                    TileState lockSign = getLockSign(block);
                    if (lockSign != null) {
                        UUID owner =  plugin.getPU().getLockOwner(lockSign);
                        List<UUID> trusted = plugin.getPU().getLockTrusted(lockSign);
                        if (trusted == null || !trusted.contains(uuid)) {
                            if (owner != null && !owner.equals(uuid)) {
                                player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[]{plugin.getPU().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(owner).getName()}));
                                e.setCancelled(true);
                            }
                        }
                    }
                } else if (block.getState().getBlockData() instanceof WallSign) {

                    Sign sign = (Sign) block.getState();

                    TileState state = (TileState) block.getState();
                    PersistentDataContainer container = state.getPersistentDataContainer();
                    NamespacedKey key = new NamespacedKey(plugin, "lock-owner");

                    if (container.has(key, PersistentDataType.STRING)) {
                        UUID owner =  plugin.getPU().getLockOwner(state);
                        List<UUID> trusted = plugin.getPU().getLockTrusted(state);
                        String trustedNames = "";
                        if (trusted != null) trustedNames = plugin.getPU().getTrustedString(trusted);
                        if (owner != null) {
                            if (owner.equals(uuid)) nameSignCheck(sign, uuid);
                            player.sendMessage(Lang.SIGN_INFO_HEADER.getString(null));
                            player.sendMessage("");
                            player.sendMessage(Lang.SIGN_INFO_OWNER.getString(new String[]{Bukkit.getOfflinePlayer(owner).getName()}));
                            player.sendMessage(Lang.SIGN_INFO_TRUSTED.getString(new String[]{trustedNames}));
                            player.sendMessage("");
                            player.sendMessage(Lang.SIGN_INFO_FOOTER.getString(null));
                            e.setCancelled(true);
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

        if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
            TileState lockSign = getLockSign(block);
            if (lockSign != null) {
                UUID owner =  plugin.getPU().getLockOwner(lockSign);
                if (owner != null && !owner.equals(uuid)) {
                    player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[] { plugin.getPU().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(owner).getName() }));
                    e.setCancelled(true);
                } else {
                    if (blockHasSign(block)) {
                        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPU().formatBlockName(block.getType().name()) }));
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
                if (owner != null && !owner.equals(uuid)) {
                    player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()), Bukkit.getOfflinePlayer(owner).getName() }));
                    e.setCancelled(true);
                } else {
                    block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                    player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()) }));

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
        for (BlockFace face : faces) {
            if (block.getRelative(face).getState().getBlockData() instanceof WallSign) {
                return true;
            }
        }
        return false;
    }

    private TileState getLockSign(Block block) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        BlockState blockState = block.getState();
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : faces) {

            Block relativeBlock = block.getRelative(face);
            BlockState relativeBlockState = relativeBlock.getState();

            if (blockState instanceof Chest && relativeBlockState instanceof Chest) {

                Chest chest = (Chest) blockState;
                Chest relativeChest = (Chest) relativeBlockState;

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

                                    if (sign.getLine(0).equals(plugin.getPU().format("&6[&cLocked&6]"))) {
                                        TileState state = (TileState) relativeState;
                                        PersistentDataContainer container = state.getPersistentDataContainer();
                                        NamespacedKey owner = new NamespacedKey(plugin, "lock-owner");
                                        if (container.has(owner, PersistentDataType.STRING)) return state;
                                    }
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
                    if (sign.getLine(0).equals(plugin.getPU().format("&6[&cLocked&6]"))) {
                        TileState state = (TileState) relativeBlockState;
                        PersistentDataContainer container = state.getPersistentDataContainer();
                        NamespacedKey owner = new NamespacedKey(plugin, "lock-owner");
                        if (container.has(owner, PersistentDataType.STRING)) return state;
                    }
                }
            }
        }
        return null;
    }

    private void nameSignCheck(Sign sign, UUID owner) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (!sign.getLine(1).equals(Bukkit.getOfflinePlayer(owner).getName())) {
            sign.setLine(1, plugin.getPU().format("&e" + Bukkit.getOfflinePlayer(owner).getName()));
        }
        sign.update();
    }
}
