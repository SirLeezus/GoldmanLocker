package lee.code.locker.commands.subcommands;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.commands.SubCommand;
import lee.code.locker.lists.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Admin extends SubCommand {

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Administrator plugin operations.";
    }

    @Override
    public String getSyntax() {
        return "/lock admin";
    }

    @Override
    public String getPermission() {
        return "lock.command.admin";
    }

    @Override
    public void perform(Player player, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (args.length > 1) {

            String command = args[1];
            UUID uuid = player.getUniqueId();

            switch (command) {
                case "bypass":
                    if (plugin.getData().hasAdminBypass(uuid)) {
                        plugin.getData().removeAdminBypass(uuid);
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.COMMAND_ADMIN_BYPASS_DISABLED.getString(null));
                    } else {
                        plugin.getData().addAdminBypass(uuid);
                        player.sendMessage(Lang.PREFIX.getString(null) + Lang.COMMAND_ADMIN_BYPASS_ENABLED.getString(null));
                    }
                    break;
            }
        } else player.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_COMMAND_ADMIN_ARGS.getString(null));
    }

    @Override
    public void performConsole(CommandSender console, String[] args) {

    }
}
