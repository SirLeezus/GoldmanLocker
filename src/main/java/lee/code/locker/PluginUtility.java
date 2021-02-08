package lee.code.locker;

import lee.code.locker.lists.SupportedBlocks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class PluginUtility {

    public String format(String format) {
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    public String formatLockLocation(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    public List<String> getSupportedBlocks() {
        return EnumSet.allOf(SupportedBlocks.class).stream().map(SupportedBlocks::name).collect(Collectors.toList());
    }

    public String formatBlockName(String name) {
        return name.replaceAll("_", " ").toLowerCase();
    }

    public List<String> getOnlinePlayers(Player playerAsking) {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getName().equals(playerAsking.getName())) players.add(player.getName());
        }
        return players;
    }
}
