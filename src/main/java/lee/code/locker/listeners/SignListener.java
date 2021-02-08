package lee.code.locker.listeners;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.database.SQLite;
import lee.code.locker.lists.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class SignListener implements Listener {

    @EventHandler
    public void onSignEditEvent(SignChangeEvent e) {

        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        SQLite SQL = plugin.getSqLite();

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Location location = e.getBlock().getLocation();
        String lockSign = plugin.getPluginUtility().formatLockLocation(location);
        Block block = e.getBlock();
        BlockData data = block.getBlockData();

        if (e.getLine(0).equals("[lock]")) {

            if (data instanceof Directional) {

                Directional directional = (Directional) data;
                Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                if (plugin.getPluginUtility().getSupportedBlocks().contains(blockBehind.getType().name())) {

                    if (getLockSign(blockBehind).equals("n")) {
                        e.setLine(0, plugin.getPluginUtility().format("&6[&cLocked&6]"));
                        e.setLine(1, plugin.getPluginUtility().format("&e" + e.getPlayer().getName()));

                        SQL.createLock(lockSign, uuid, "n");
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPluginUtility().formatBlockName(blockBehind.getType().name()) }));
                    } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_BLOCK_ALREADY_HAS_LOCK.getString(null));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        SQLite SQL = plugin.getSqLite();

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Action action = e.getAction();
        Block block = e.getClickedBlock();

        if (e.hasBlock()) {
            if (action.equals(Action.RIGHT_CLICK_BLOCK)) {

                if (plugin.getPluginUtility().getSupportedBlocks().contains(block.getType().name())) {

                    String lockSign = getLockSign(block);

                    if (SQL.isLocked(lockSign)) {
                        if (!SQL.isLockOwner(lockSign, uuid)) {
                            player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[] { plugin.getPluginUtility().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName() }));
                            e.setCancelled(true);
                        }
                    }
                } else if (block.getState().getBlockData() instanceof WallSign) {

                    Sign sign = (Sign) block.getState();
                    String lockSign = plugin.getPluginUtility().formatLockLocation(sign.getLocation());
                    if (SQL.isLocked(lockSign)) {

                        String owner = Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName();
                        String trusted = String.join(", ", SQL.getTrustedToLock(lockSign));

                        player.sendMessage(Lang.SIGN_INFO_HEADER.getString(null));
                        player.sendMessage("");
                        player.sendMessage(Lang.SIGN_INFO_OWNER.getString(new String[] { owner }));
                        player.sendMessage("");
                        player.sendMessage(Lang.SIGN_INFO_TRUSTED.getString(new String[] { trusted }));
                        player.sendMessage("");
                        player.sendMessage(Lang.SIGN_INFO_FOOTER.getString(null));

                        if (SQL.isLockOwner(lockSign, uuid)) {
                            nameSignCheck(sign, uuid);
                        } else e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        SQLite SQL = plugin.getSqLite();

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = e.getBlock();

        if (plugin.getPluginUtility().getSupportedBlocks().contains(block.getType().name())) {

            String lockSign = getLockSign(block);

            if (SQL.isLocked(lockSign)) {
                if (!SQL.isLockOwner(lockSign, uuid)) {
                    player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[] { plugin.getPluginUtility().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName() }));
                    e.setCancelled(true);
                } else {
                    if (blockHasSign(block)) {
                        SQL.removeLock(lockSign);
                        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPluginUtility().formatBlockName(block.getType().name()) }));
                    }
                }
            }
        } else if (block.getState().getBlockData() instanceof WallSign) {

            Sign sign = (Sign) block.getState();

            BlockData data = block.getBlockData();
            Directional directional = (Directional) data;
            Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

            if (sign.getLine(0).equals(plugin.getPluginUtility().format("&6[&cLocked&6]"))) {

                String lockSign = plugin.getPluginUtility().formatLockLocation(sign.getLocation());

                if (SQL.isLocked(lockSign)) {
                    if (!SQL.isLockOwner(lockSign, uuid)) {
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[] { plugin.getPluginUtility().formatBlockName(blockBehind.getType().name()), Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName() }));
                        e.setCancelled(true);
                    } else {

                        SQL.removeLock(lockSign);
                        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);

                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPluginUtility().formatBlockName(blockBehind.getType().name()) }));
                    }
                }
            }
        }
    }

    private boolean blockHasSign(Block block) {
        String[] faces = {"EAST", "NORTH", "SOUTH", "WEST"};

        for (String face : faces) {

            if (block.getRelative(BlockFace.valueOf(face)).getState().getBlockData() instanceof WallSign) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPistonMove(BlockPistonExtendEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        SQLite SQL = plugin.getSqLite();

        for (Block block : new ArrayList<Block>(e.getBlocks())) {
            if (plugin.getPluginUtility().getSupportedBlocks().contains(block.getType().name())) {
                String lockSign = getLockSign(block);
                if (SQL.isLocked(lockSign)) {
                    e.setCancelled(true);
                }
            } else if (block.getState().getBlockData() instanceof WallSign) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equals(plugin.getPluginUtility().format("&6[&cLocked&6]"))) {
                    String lockSign = plugin.getPluginUtility().formatLockLocation(sign.getLocation());
                    if (SQL.isLocked(lockSign)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExplodeEvent(EntityExplodeEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        SQLite SQL = plugin.getSqLite();

        for (Block block : new ArrayList<Block>(e.blockList())) {
            if (plugin.getPluginUtility().getSupportedBlocks().contains(block.getType().name())) {
                String lockSign = getLockSign(block);
                if (SQL.isLocked(lockSign)) {
                    e.blockList().remove(block);
                }
            } else if (block.getState().getBlockData() instanceof WallSign) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equals(plugin.getPluginUtility().format("&6[&cLocked&6]"))) {
                    String lockSign = plugin.getPluginUtility().formatLockLocation(sign.getLocation());
                    if (SQL.isLocked(lockSign)) {
                        e.blockList().remove(block);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHopperMoveEvent(InventoryMoveItemEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        SQLite SQL = plugin.getSqLite();

        if (plugin.getPluginUtility().getSupportedBlocks().contains(e.getSource().getLocation().getBlock().getType().name()) && e.getSource().getType() != InventoryType.HOPPER) {
            Block block = e.getSource().getLocation().getBlock();
            String lockSign = getLockSign(block);
            if (SQL.isLocked(lockSign)) {
                e.setCancelled(true);
            }
        }
    }

    private String getLockSign(Block block) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        String[] faces = {"EAST", "NORTH", "SOUTH", "WEST"};

        for (String face : faces) {
            if (block.getRelative(BlockFace.valueOf(face)).getState() instanceof Chest && block.getState() instanceof Chest) {

                Chest linkedChestState = (Chest) block.getRelative(BlockFace.valueOf(face)).getState();
                Inventory linkedChestInventory = linkedChestState.getInventory();

                Chest chestState = (Chest) block.getState();
                Inventory chestInventory = chestState.getInventory();

                if (linkedChestInventory instanceof DoubleChestInventory && chestInventory instanceof DoubleChestInventory) {

                    DoubleChest linkedDoubleChest = (DoubleChest) linkedChestInventory.getHolder();
                    DoubleChest doubleChest = (DoubleChest) chestInventory.getHolder();

                    if (linkedDoubleChest.getLocation().equals(doubleChest.getLocation())) {
                        Block chest = block.getRelative(BlockFace.valueOf(face));
                        for (String face2 : faces) {
                            if (chest.getRelative(BlockFace.valueOf(face2)).getState().getBlockData() instanceof WallSign) {

                                Sign sign = (Sign) chest.getRelative(BlockFace.valueOf(face2)).getState();

                                Directional directional = (Directional) sign.getBlockData();
                                Block blockBehind = chest.getRelative(BlockFace.valueOf(face2)).getRelative(directional.getFacing().getOppositeFace());

                                if (blockBehind.equals(chest)) {
                                    if (sign.getLine(0).equals(plugin.getPluginUtility().format("&6[&cLocked&6]"))) {
                                        return plugin.getPluginUtility().formatLockLocation(sign.getLocation());
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (block.getRelative(BlockFace.valueOf(face)).getState().getBlockData() instanceof WallSign) {
                Sign sign = (Sign) block.getRelative(BlockFace.valueOf(face)).getState();

                Directional directional = (Directional) sign.getBlockData();
                Block blockBehind = block.getRelative(BlockFace.valueOf(face)).getRelative(directional.getFacing().getOppositeFace());

                if (blockBehind.equals(block)) {
                    if (sign.getLine(0).equals(plugin.getPluginUtility().format("&6[&cLocked&6]"))) {
                        return plugin.getPluginUtility().formatLockLocation(sign.getLocation());
                    }
                }
            }
        }
        return "n";
    }

    private void nameSignCheck(Sign sign, UUID owner) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (!sign.getLine(1).equals(Bukkit.getOfflinePlayer(owner).getName())) {
            sign.setLine(1, plugin.getPluginUtility().format("&e" + Bukkit.getOfflinePlayer(owner).getName()));
        }
        sign.update();
    }

    private void removeSign(Block block) {
        Location location = block.getLocation();
        location.getWorld().dropItemNaturally(location, new ItemStack(block.getType()));
        block.setType(Material.AIR);
    }
}
