package lee.code.locker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Data {

    private final List<UUID> adminBypassList = new ArrayList<>();
    private final List<UUID> playerClickDelay = new ArrayList<>();

    public void addAdminBypass(UUID uuid) {
        adminBypassList.add(uuid);
    }
    public void removeAdminBypass(UUID uuid) {
        adminBypassList.remove(uuid);
    }
    public boolean hasAdminBypass(UUID uuid) {
        return adminBypassList.contains(uuid);
    }
    public boolean hasPlayerClickDelay(UUID uuid) {
        return playerClickDelay.contains(uuid);
    }
    public void addPlayerClickDelay(UUID uuid) {
        playerClickDelay.add(uuid);
    }
    public void removePlayerClickDelay(UUID uuid) {
        playerClickDelay.remove(uuid);
    }
}
