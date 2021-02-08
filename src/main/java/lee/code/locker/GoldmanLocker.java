package lee.code.locker;

import lee.code.locker.commands.cmds.LockCMD;
import lee.code.locker.commands.tabs.LockTab;
import lee.code.locker.database.SQLite;
import lee.code.locker.listeners.SignListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class GoldmanLocker extends JavaPlugin {

    @Getter private PluginUtility pluginUtility;
    @Getter private SQLite sqLite;

    @Override
    public void onEnable() {
        this.pluginUtility = new PluginUtility();
        this.sqLite = new SQLite();

        sqLite.connect();
        sqLite.loadTables();

        registerListeners();
        registerCommands();

    }

    @Override
    public void onDisable() {
        sqLite.disconnect();
    }

    private void registerCommands() {

        //cmds
        getCommand("lock").setExecutor(new LockCMD());
        getCommand("lock").setTabCompleter(new LockTab());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new SignListener(), this);
    }

    public static GoldmanLocker getPlugin() {
        return GoldmanLocker.getPlugin(GoldmanLocker.class);
    }
}
