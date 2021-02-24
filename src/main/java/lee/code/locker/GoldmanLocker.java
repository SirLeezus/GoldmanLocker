package lee.code.locker;

import lee.code.locker.commands.LockCMD;
import lee.code.locker.commands.TabCompletion;
import lee.code.locker.listeners.SignListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class GoldmanLocker extends JavaPlugin {

    @Getter private PU pU;

    @Override
    public void onEnable() {
        this.pU = new PU();

        registerListeners();
        registerCommands();
    }

    private void registerCommands() {
        getCommand("lock").setExecutor(new LockCMD());
        getCommand("lock").setTabCompleter(new TabCompletion());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new SignListener(), this);
    }

    public static GoldmanLocker getPlugin() {
        return GoldmanLocker.getPlugin(GoldmanLocker.class);
    }
}
