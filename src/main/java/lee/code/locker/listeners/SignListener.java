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
import org.bukkit.inventory.InventoryHolder;
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
        String lockSign = plugin.getPU().formatLockLocation(location);
        Block block = e.getBlock();
        BlockData data = block.getBlockData();
        String line1 = e.getLine(0);

        if (line1 != null && line1.equals("[lock]")) {

            if (data instanceof Directional) {

                Directional directional = (Directional) data;
                Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

                if (plugin.getPU().getSupportedBlocks().contains(blockBehind.getType().name())) {

                    if (getLockSign(blockBehind).equals("n")) {
                        e.setLine(0, plugin.getPU().format("&6[&cLocked&6]"));
                        e.setLine(1, plugin.getPU().format("&e" + e.getPlayer().getName()));

                        SQL.createLock(lockSign, uuid, "n");
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()) }));
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

        if (block != null) {
            if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
                    String lockSign = getLockSign(block);

                    if (SQL.isLocked(lockSign)) {
                        if (!SQL.isLockOwner(lockSign, uuid) && !SQL.isLockTrusted(lockSign, uuid)) {
                            player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[]{plugin.getPU().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName()}));
                            e.setCancelled(true);
                        }
                    }
                } else if (block.getState().getBlockData() instanceof WallSign) {

                    Sign sign = (Sign) block.getState();
                    String lockSign = plugin.getPU().formatLockLocation(sign.getLocation());

                    if (SQL.isLocked(lockSign)) {

                        String owner = Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName();
                        String trusted = String.join(", ", SQL.getTrustedToLock(lockSign));

                        player.sendMessage(Lang.SIGN_INFO_HEADER.getString(null));
                        player.sendMessage("");
                        player.sendMessage(Lang.SIGN_INFO_OWNER.getString(new String[]{owner}));
                        player.sendMessage(Lang.SIGN_INFO_TRUSTED.getString(new String[]{trusted}));
                        player.sendMessage("");
                        player.sendMessage(Lang.SIGN_INFO_FOOTER.getString(null));

                        if (SQL.isLockOwner(lockSign, uuid)) {
                            nameSignCheck(sign, uuid);
                        } else e.setCancelled(true);
                    } else {
                        if (sign.getLine(0).equals(plugin.getPU().format("&6[&cLocked&6]"))) {

                            Location location = block.getLocation();
                            ItemStack dropSign = new ItemStack(Material.valueOf(block.getType().name().replace("_WALL", "")));

                            if (location.getWorld() != null) {
                                location.getWorld().dropItemNaturally(location, dropSign);
                                block.setType(Material.AIR);
                                block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                            }
                        }
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

        if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {

            String lockSign = getLockSign(block);

            if (SQL.isLocked(lockSign)) {
                if (!SQL.isLockOwner(lockSign, uuid)) {
                    player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[] { plugin.getPU().formatBlockName(block.getType().name()), Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName() }));
                    e.setCancelled(true);
                } else {
                    if (blockHasSign(block)) {
                        SQL.removeLock(lockSign);
                        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPU().formatBlockName(block.getType().name()) }));
                    }
                }
            }
        } else if (block.getState().getBlockData() instanceof WallSign) {

            Sign sign = (Sign) block.getState();

            BlockData data = block.getBlockData();
            Directional directional = (Directional) data;
            Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());

            if (sign.getLine(0).equals(plugin.getPU().format("&6[&cLocked&6]"))) {

                String lockSign = plugin.getPU().formatLockLocation(sign.getLocation());

                if (SQL.isLocked(lockSign)) {
                    if (!SQL.isLockOwner(lockSign, uuid)) {
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_LOCKED.getString(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()), Bukkit.getOfflinePlayer(SQL.getLockOwner(lockSign)).getName() }));
                        e.setCancelled(true);
                    } else {
                        SQL.removeLock(lockSign);
                        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.MESSAGE_REMOVE_LOCK_SUCCESSFUL.getString(new String[] { plugin.getPU().formatBlockName(blockBehind.getType().name()) }));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPistonMove(BlockPistonExtendEvent e) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        SQLite SQL = plugin.getSqLite();

        for (Block block : new ArrayList<>(e.getBlocks())) {
            if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
                String lockSign = getLockSign(block);
                if (SQL.isLocked(lockSign)) {
                    e.setCancelled(true);
                }
            } else if (block.getState().getBlockData() instanceof WallSign) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equals(plugin.getPU().format("&6[&cLocked&6]"))) {
                    String lockSign = plugin.getPU().formatLockLocation(sign.getLocation());
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

        for (Block block : new ArrayList<>(e.blockList())) {
            if (plugin.getPU().getSupportedBlocks().contains(block.getType().name())) {
                String lockSign = getLockSign(block);
                if (SQL.isLocked(lockSign)) {
                    e.blockList().remove(block);
                }
            } else if (block.getState().getBlockData() instanceof WallSign) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equals(plugin.getPU().format("&6[&cLocked&6]"))) {
                    String lockSign = plugin.getPU().formatLockLocation(sign.getLocation());
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

        if (e.getSource().getLocation() != null) {
            Block block = e.getSource().getLocation().getBlock();
            if (plugin.getPU().getSupportedBlocks().contains(block.getType().name()) && e.getSource().getType() != InventoryType.HOPPER) {
                String lockSign = getLockSign(block);
                if (SQL.isLocked(lockSign)) {
                    e.setCancelled(true);
                }
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

    private String getLockSign(Block block) {
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
                                        return plugin.getPU().formatLockLocation(sign.getLocation());
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
                        return plugin.getPU().formatLockLocation(sign.getLocation());
                    }
                }
            }
        }
        return "n";
    }

    private void nameSignCheck(Sign sign, UUID owner) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (!sign.getLine(1).equals(Bukkit.getOfflinePlayer(owner).getName())) {
            sign.setLine(1, plugin.getPU().format("&e" + Bukkit.getOfflinePlayer(owner).getName()));
        }
        sign.update();
    }
}
