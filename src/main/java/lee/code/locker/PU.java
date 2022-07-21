package lee.code.locker;

import lee.code.locker.lists.SupportedBlocks;
import lee.code.locker.lists.Values;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PU {

    public String format(String format) {
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    public Component formatC(String message) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        return Component.empty().decoration(TextDecoration.ITALIC, false).append(serializer.deserialize(message));
    }

    public String unFormatC(Component message) {
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        return serializer.serialize(message);
    }

    public List<String> getSupportedBlocks() {
        return EnumSet.allOf(SupportedBlocks.class).stream().map(SupportedBlocks::name).collect(Collectors.toList());
    }

    public String formatBlockName(String name) {
        return name.replaceAll("_", " ").toLowerCase();
    }

    public List<String> getOnlinePlayers() {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) players.add(player.getName());
        return players;
    }

    public UUID getLockOwner(TileState tileState) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        PersistentDataContainer container = tileState.getPersistentDataContainer();
        NamespacedKey lockKey = new NamespacedKey(plugin, "lock-owner");
        String owner = container.get(lockKey, PersistentDataType.STRING);
        if (owner != null) return UUID.fromString(owner);
        else return null;
    }

    public List<UUID> getLockTrusted(TileState tileState) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        PersistentDataContainer container = tileState.getPersistentDataContainer();
        NamespacedKey lockKey = new NamespacedKey(plugin, "lock-trusted");
        String trusted = container.get(lockKey, PersistentDataType.STRING);
        if (trusted != null && !trusted.equals("n")) {
            List<UUID> players = new ArrayList<>();
            String[] split = StringUtils.split(trusted, ',');
            for (String player : split) players.add(UUID.fromString(player));
            return players;
        } else return null;
    }

    public String getTrustedString(List<UUID> trusted) {
        List<String> names = new ArrayList<>();
        for (UUID tPlayer : trusted) names.add(Bukkit.getOfflinePlayer(tPlayer).getName());
        return StringUtils.join(names, ", ");
    }

    public void addPlayerClickDelay(UUID uuid) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        plugin.getData().addPlayerClickDelay(uuid);
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.runTaskLater(plugin, () -> plugin.getData().removePlayerClickDelay(uuid), Values.CLICK_DELAY.getValue());
    }
}
