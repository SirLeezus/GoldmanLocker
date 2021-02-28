package lee.code.locker.commands;

import lee.code.locker.GoldmanLocker;
import lee.code.locker.commands.subcommands.Add;
import lee.code.locker.commands.subcommands.Admin;
import lee.code.locker.commands.subcommands.Remove;
import lee.code.locker.lists.Lang;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {
    @Getter private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager() {
        subCommands.add(new Add());
        subCommands.add(new Remove());
        subCommands.add(new Admin());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();

        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length > 0) {
                for (int i = 0; i < getSubCommands().size(); i++) {
                    if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())){
                        if (p.hasPermission(getSubCommands().get(i).getPermission())) getSubCommands().get(i).perform(p, args);
                        else p.sendMessage(Lang.PREFIX.getString(null) + Lang.ERROR_NO_PERMISSION.getString(null));
                        return true;
                    }
                }
            }

            int number = 1;
            List<String> lines = new ArrayList<>();
            lines.add(Lang.MESSAGE_HELP_DIVIDER.getString(null));
            lines.add(Lang.MESSAGE_HELP_TITLE.getString(null));
            lines.add("&r");

            for (int i = 0; i < getSubCommands().size(); i++) {
                if (p.hasPermission(getSubCommands().get(i).getPermission())) {
                    lines.add(Lang.MESSAGE_HELP_SUB_COMMAND.getString(new String [] { String.valueOf(number), getSubCommands().get(i).getSyntax(), getSubCommands().get(i).getDescription() }));
                    number++;
                }
            }
            lines.add("&r");
            lines.add(Lang.MESSAGE_HELP_DIVIDER.getString(null));
            for (String line : lines) p.sendMessage(plugin.getPU().format(line));
            return true;
        }

        if (args.length > 0) {
            for (int i = 0; i < getSubCommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    getSubCommands().get(i).performConsole(sender, args);
                    return true;
                }
            }
        }
        return true;
    }
}