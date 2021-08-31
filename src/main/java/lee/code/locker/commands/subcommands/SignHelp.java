package lee.code.locker.commands.subcommands;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.commands.SubCommand;
import lee.code.locker.lists.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SignHelp extends SubCommand {

    @Override
    public String getName() {
        return "signhelp";
    }

    @Override
    public String getDescription() {
        return "How to create a lock with a sign.";
    }

    @Override
    public String getSyntax() {
        return "/lock signhelp";
    }

    @Override
    public String getPermission() {
        return "lock.command.signhelp";
    }

    @Override
    public void perform(Player player, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        List<Component> lines = new ArrayList<>();

        lines.add(Lang.MESSAGE_HELP_DIVIDER.getComponent(null));
        lines.add(Component.text(""));
        lines.add(plugin.getPU().formatC("           &2&l&nHow To Lock A Block With A Sign"));
        lines.add(Component.text(""));
        lines.add(plugin.getPU().formatC("&6Step 1&7: &ePlace down a supported block, all container blocks should be supported."));
        lines.add(Component.text(""));
        lines.add(plugin.getPU().formatC("&6Step 2&7: &ePlace a sign on the block and type the following:"));
        lines.add(Component.text(""));
        lines.add(plugin.getPU().formatC("&6Line 1&7: &f[lock]"));
        lines.add(Component.text(""));
        lines.add(plugin.getPU().formatC("&eCongratulations! If you followed this guide correctly you should have locked the block you placed the sign on! Now only you and trusted players can interact with that block and only you will be able to break the block."));
        lines.add(Component.text(""));
        lines.add(Lang.MESSAGE_HELP_DIVIDER.getComponent(null));

        for (Component line : lines) player.sendMessage(line);
    }

    @Override
    public void performConsole(CommandSender console, String[] args) {
        console.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_NOT_A_CONSOLE_COMMAND.getString(null));
    }
}
